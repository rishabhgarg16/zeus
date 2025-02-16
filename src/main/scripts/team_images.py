import logging
import mysql.connector
from contextlib import contextmanager
from datetime import datetime
from typing import Optional

import requests

logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s'
)

# Database configuration
db_config = {
    "host": "ls-a6927155ebc8223b62e0da94714b39337fdb981a.c9yg6ks8shtr.ap-south-1.rds.amazonaws.com",
    "user": "admin",
    "password": "adminpass",
    "database": "hit11"
}

# Base URLs for different leagues
ICC_BASE_URL = "https://assets-icc.sportz.io/static-assets/buildv3-stg/images/teams"
IPL_BASE_URL = "https://scores.iplt20.com/ipl/teamlogos"

# Team mappings with their respective CDNs
TEAM_URLS = {
    # International Teams (ICC)
    "IND": f"{ICC_BASE_URL}/4.png",   # India
    "AUS": f"{ICC_BASE_URL}/1.png",   # Australia
    "ENG": f"{ICC_BASE_URL}/3.png",   # England
    "PAK": f"{ICC_BASE_URL}/6.png",   # Pakistan
    "SA":  f"{ICC_BASE_URL}/7.png",   # South Africa
    "NZ":  f"{ICC_BASE_URL}/5.png",   # New Zealand
    "WI":  f"{ICC_BASE_URL}/9.png",   # West Indies
    "SL":  f"{ICC_BASE_URL}/8.png",   # Sri Lanka
    "BAN": f"{ICC_BASE_URL}/2.png",   # Bangladesh
    # "AFG": f"{ICC_BASE_URL}/96.png",  # Afghanistan
    "ZIM": f"{ICC_BASE_URL}/10.png",  # Zimbabwe
    "CAN": f"{ICC_BASE_URL}/12.png",  # Canada
    "IRE": f"{ICC_BASE_URL}/13.png",  # Ireland
    "KEN": f"{ICC_BASE_URL}/14.png",  # KENYA
    "NED": f"{ICC_BASE_URL}/15.png",  # NETHERLANDS
    "SCO": f"{ICC_BASE_URL}/16.png",  # SCOTLAND
    "NAM": f"{ICC_BASE_URL}/20.png",  # NAMIBIA
    "USA": f"{ICC_BASE_URL}/22.png",  # USA
    # "NEP": f"{ICC_BASE_URL}/,.png",   # Nepal
    "OMA": f"{ICC_BASE_URL}/28.png",  # Oman
    "UGA": f"{ICC_BASE_URL}/29.png",  # Uganda
    # "PNG": "",  # Papua New Guinea

    # IPL Teams (IPL CDN)
    "MI": f"{IPL_BASE_URL}/MI.png",
    "CSK": f"{IPL_BASE_URL}/CSK.png",
    "RCB": f"{IPL_BASE_URL}/RCB.png",
    "KKR": f"{IPL_BASE_URL}/KKR.png",
    "DC": f"{IPL_BASE_URL}/DC.png",
    "PBKS": f"{IPL_BASE_URL}/PBKS.png",
    "RR": f"{IPL_BASE_URL}/RR.png",
    "SRH": f"{IPL_BASE_URL}/SRH.png",
    "LSG": f"{IPL_BASE_URL}/LSG.png",
    "GT": f"{IPL_BASE_URL}/GT.png"
}

@contextmanager
def get_db_connection():
    conn = None
    try:
        conn = mysql.connector.connect(**db_config)
        yield conn
    finally:
        if conn and conn.is_connected():
            conn.close()


@contextmanager
def get_cursor(conn):
    cursor = None
    try:
        cursor = conn.cursor(dictionary=True)
        yield cursor
    finally:
        if cursor:
            cursor.close()

def normalize_team_code(team_short_name: str) -> str:
    """
    Normalize team codes by removing suffixes like W, U19, etc.
    and handling variations in naming
    """
    # Remove common suffixes
    base_name = team_short_name.replace('W', '').replace('U19', '')

    # Handle known variations
    variations = {
        'RSA': 'SA',     # South Africa variations
        'BRSAL': 'BAN',  # Barishal/Bangladesh
        'DCW': 'DC',     # Delhi Capitals Women
        'MIW': 'MI',     # Mumbai Indians Women
        'RCBW': 'RCB',   # RCB Women
        'GGTW': 'GT',    # Gujarat Giants Women
        'UPW': 'PBKS',   # UP Warriors (Punjab connection)
        'INDW': 'IND',   # India Women
        'AUSW': 'AUS',   # Australia Women
        'ENGW': 'ENG',   # England Women
        'PAKW': 'PAK',   # Pakistan Women
        'NZW': 'NZ',     # New Zealand Women
        'BANW': 'BAN',   # Bangladesh Women
        'WIW': 'WI',     # West Indies Women
        'IREW': 'IRE',   # Ireland Women
        'NEDW': 'NED',   # Netherlands Women
    }

    return variations.get(base_name, base_name)

def verify_image_url(url: str) -> bool:
    try:
        response = requests.head(url, timeout=5)
        logging.info(f"Checking URL {url}: Status {response.status_code}")
        return response.status_code == 200
    except Exception as e:
        logging.error(f"Error verifying URL {url}: {e}")
        return False


def get_team_image_url(team_short_name: str) -> Optional[str]:
    # First try exact match
    if team_short_name in TEAM_URLS:
        url = TEAM_URLS[team_short_name]
        if verify_image_url(url):
            return url

    # If not found, try normalized version
    normalized_code = normalize_team_code(team_short_name)
    if normalized_code in TEAM_URLS:
        url = TEAM_URLS[normalized_code]
        if verify_image_url(url):
            logging.info(f"Using {normalized_code}'s image for {team_short_name}")
            return url

    # Log which mapping was used
    logging.info(f"Team {team_short_name} normalized to {normalized_code}")

    return None


def update_team_images():
    """Update image URLs for all teams in database"""
    with get_db_connection() as conn:
        with get_cursor(conn) as cursor:
            # Get all teams
            cursor.execute("SELECT id, team_name, team_short_name FROM teams")
            teams = cursor.fetchall()

            results = {
                'updated': [],
                'skipped': [],
                'normalized': []
            }

            for team in teams:
                team_short_name = team['team_short_name']
                logging.info(f"\nProcessing team: {team['team_name']} ({team_short_name})")

                original_url = TEAM_URLS.get(team_short_name)
                image_url = get_team_image_url(team_short_name)

                if image_url:
                    try:
                        cursor.execute("""
                            UPDATE teams 
                            SET team_image_url = %s,
                                updated_at = %s 
                            WHERE id = %s
                        """, (image_url, datetime.now(), team['id']))
                        conn.commit()

                        # Track which type of match was used
                        if original_url == image_url:
                            results['updated'].append(team_short_name)
                        else:
                            results['normalized'].append(
                                f"{team_short_name} -> {normalize_team_code(team_short_name)}"
                            )

                        logging.info(f"Updated image URL for {team_short_name}: {image_url}")
                    except Exception as e:
                        logging.error(f"Error updating team {team_short_name}: {e}")
                        conn.rollback()
                else:
                    results['skipped'].append(team_short_name)
                    logging.warning(f"Skipping team {team_short_name} - no valid image URL found")

            # Print summary
            logging.info(f"\nSummary:")
            logging.info(f"Total teams: {len(teams)}")
            logging.info(f"Direct matches: {len(results['updated'])}")
            logging.info(f"Normalized matches: {len(results['normalized'])}")
            logging.info(f"Skipped: {len(results['skipped'])}")

            logging.info("\nNormalized mappings used:")
            for mapping in results['normalized']:
                logging.info(f"  {mapping}")

            logging.info("\nSkipped teams:")
            for team in results['skipped']:
                logging.info(f"  {team}")


def verify_all_mappings():
    """Verify all mappings in TEAM_URLS"""
    logging.info("Verifying all team image mappings...")
    invalid_mappings = []

    for team, url in TEAM_URLS.items():
        if not verify_image_url(url):
            invalid_mappings.append((team, url))

    if invalid_mappings:
        logging.error("\nInvalid mappings found:")
        for team, url in invalid_mappings:
            logging.error(f"  {team}: {url}")
        return False
    else:
        logging.info("\nAll mappings verified successfully!")
        return True


if __name__ == "__main__":
    # First verify all mappings
    if verify_all_mappings():
        # Ask for confirmation before updating database
        input("\nPress Enter to continue with database updates (Ctrl+C to cancel)...")

        # Update team images in database
        update_team_images()
    else:
        logging.error("\nPlease fix invalid mappings before proceeding with database updates.")
