import json
import time
import os
import mysql.connector
from decimal import Decimal
import requests
import traceback
from datetime import timezone, datetime

# Database connection
db_config = {
    "host": "ls-a6927155ebc8223b62e0da94714b39337fdb981a.c9yg6ks8shtr.ap-south-1.rds.amazonaws.com",
    "user": "admin",
    "password": "adminpass",
    "database": "hit11"
}

db_connection = mysql.connector.connect(**db_config)

# Zeus API configuration
ZEUS_API_ENDPOINT = os.getenv("ZEUS_API_ENDPOINT", "http://localhost:8080/api/events/scorecardV2")


# ZEUS_API_KEY = "your_api_key_here"

def get_or_create_match(match_header):
    conn = db_connection
    cursor = db_connection.cursor()

    # Try to get existing match
    query = "SELECT id FROM matches WHERE cricbuzz_match_id = %s"
    cursor.execute(query, (match_header['matchId'],))
    result = cursor.fetchone()
    print("inserted match data")
    print(result)

    if result:
        match_id = result[0]
        # Update existing match
        update_match(cursor, match_header, match_id)
    else:
        # Create new match
        match_id = create_match(cursor, match_header)

    conn.commit()
    # cursor.close()
    # conn.close()
    return match_id


def create_match(cursor, match_header):
    team1_id = get_or_create_internal_team_id(match_header['team1']['id'], match_header['team1']['name'],
                                              match_header['team1']['shortName'])
    team2_id = get_or_create_internal_team_id(match_header['team2']['id'], match_header['team2']['name'],
                                              match_header['team2']['shortName'])

    new_match = {
        'cricbuzz_match_id': match_header['matchId'],
        'team1': match_header['team1']['name'],
        'team2': match_header['team2']['name'],
        'matchGroup': match_header.get('seriesName'),
        'startDate': datetime.fromtimestamp(match_header['matchStartTimestamp'] / 1000, timezone.utc),
        'endDate': datetime.fromtimestamp(match_header['matchCompleteTimestamp'] / 1000, timezone.utc),
        'city': None,  # Not available in the provided data
        'stadium': None,  # Not available in the provided data
        'country': None,  # Not available in the provided data
        'status': map_match_status(match_header['state']),
        'tournamentName': match_header.get('seriesName'),
        'matchType': match_header['matchFormat'],
        'matchLink': None,  # Not available in the provided data
        'team1_id': team1_id,
        'team2_id': team2_id,
        'created_at': datetime.now(),
        'updated_at': datetime.now()
    }

    insert_query = """
    INSERT INTO matches (cricbuzz_match_id, team1, team2, match_group, start_date, end_date, city, stadium, country, status, tournament_name, match_type, match_link, team1_id, team2_id, created_at, updated_at)
    VALUES (%(cricbuzz_match_id)s, %(team1)s, %(team2)s, %(matchGroup)s, %(startDate)s, %(endDate)s, %(city)s, %(stadium)s, %(country)s, %(status)s, %(tournamentName)s, %(matchType)s, %(matchLink)s, %(team1_id)s, %(team2_id)s, %(created_at)s, %(updated_at)s)
    """
    cursor.execute(insert_query, new_match)
    id = cursor.lastrowid
    print(f"Created new match: {id}")
    return id


def update_match(cursor, match_header, match_id):
    update_data = {
        'status': map_match_status(match_header['state']),
        'updated_at': datetime.now(),
        'id': match_id
    }

    update_query = """
    UPDATE matches
    SET status = %(status)s, updated_at = %(updated_at)s
    WHERE id = %(id)s
    """
    cursor.execute(update_query, update_data)
    print(f"Updated match: {match_id}")


def map_match_status(cricbuzz_state):
    # Map Cricbuzz match state to your MatchStatus enum
    status_map = {
        'In Progress': 'Live',
        'Complete': 'Completed',
        # Add more mappings as needed
    }
    return status_map.get(cricbuzz_state, 'Scheduled')


def call_cricbuzz_commentry_api(match_id):
    url = f"https://cricbuzz-cricket.p.rapidapi.com/mcenter/v1/{match_id}/comm"
    headers = {
        "x-rapidapi-key": "cf1c48d00fmshcf81b48d77b26b8p1e23f0jsn7bf53d9ff8d9",
        "x-rapidapi-host": "cricbuzz-cricket.p.rapidapi.com"
    }

    try:
        response = requests.get(url, headers=headers)
        response.raise_for_status()  # Raises an HTTPError for bad responses (4xx or 5xx)
        # print(f"Data fetched successfully from Cricbuzz. Response: {response.text}")
        return response.json()
    except requests.exceptions.RequestException as e:
        print(f"Error getting data from Cricbuzz: {e}")
        return False


def get_internal_team_id(external_id):
    cursor = db_connection.cursor()
    query = "SELECT id FROM teams WHERE cricbuzz_team_id = %s"
    cursor.execute(query, (external_id,))
    result = cursor.fetchone()
    # cursor.close()
    return result[0] if result else None


def get_or_create_internal_player_id(external_id, player_name, external_team_id):
    conn = db_connection
    cursor = db_connection.cursor()

    # Try to get existing player
    query = "SELECT id FROM players WHERE cricbuzz_player_id = %s"
    cursor.execute(query, (external_id,))
    result = cursor.fetchone()

    if result:
        internal_id = result[0]
    else:
        # Player not found, create new entry in Zeus
        internal_team_id = get_internal_team_id(external_team_id)
        new_player = {
            'name': player_name,
            'age': None,  # We don't have this info from Cricbuzz
            'runsScored': 0,
            'runsConceded': 0,
            'credits': 0,  # Set a default value
            'iconUrl': None,
            'country': None,  # We don't have this info from Cricbuzz
            'teamId': internal_team_id,
            'cricbuzzPlayerId': external_id
        }

        # Insert new player into Zeus
        insert_query = """
        INSERT INTO players (name, age, runs_scored, runs_conceded, credits, icon_url, country, team_id, cricbuzz_player_id)
        VALUES (%(name)s, %(age)s, %(runsScored)s, %(runsConceded)s, %(credits)s, %(iconUrl)s, %(country)s, %(teamId)s, %(cricbuzzPlayerId)s)
        """
        cursor.execute(insert_query, new_player)
        internal_id = cursor.lastrowid

        conn.commit()
        print(f"Created new player: {player_name} with internal ID: {internal_id}")

    # cursor.close()
    return internal_id


def get_or_create_internal_team_id(external_id, team_name, team_short_name):
    conn = db_connection
    cursor = db_connection.cursor()

    # Try to get existing team
    query = "SELECT id as internal_id FROM teams WHERE cricbuzz_team_id = %s"
    cursor.execute(query, (external_id,))
    result = cursor.fetchone()

    if result:
        internal_id = result[0]
    else:
        # Team not found, create new entry in Zeus
        new_team = {
            'cricbuzz_team_id': external_id,
            'team_name': team_name,
            'team_short_name': team_short_name,
            'team_image_url': None,  # We don't have this info from Cricbuzz
            'created_at': datetime.now(),
            'updated_at': datetime.now()
        }

        # Insert new team into Zeus
        insert_query = """
        INSERT INTO teams (team_name, team_short_name, team_image_url, cricbuzz_team_id, created_at, updated_at)
        VALUES (%(team_name)s, %(team_short_name)s, %(team_image_url)s, %(cricbuzz_team_id)s, %(created_at)s, %(updated_at)s)
        """
        print(new_team)
        cursor.execute(insert_query, new_team)
        internal_id = cursor.lastrowid

        conn.commit()
        print(f"Created new team: {team_name} with internal ID: {internal_id}")

    # cursor.close()
    return internal_id


def send_to_zeus(hit11_scorecard):
    headers = {
        "Content-Type": "application/json",
    }
    try:
        # Convert the Python dictionary to a JSON string
        json_data = json.dumps(hit11_scorecard, default=str)

        # Print the JSON data for debugging
        print("JSON data being sent to Zeus:")
        print(json_data)

        response = requests.post(ZEUS_API_ENDPOINT, data=json_data, headers=headers)
        response.raise_for_status()
        print(f"Data sent successfully to Zeus. Response: {response.text}")
        return True
    except requests.exceptions.RequestException as e:
        print(f"Error sending data to Zeus: {e}")
        return False


def convert_cricbuzz_to_hit11(cricbuzz_data):
    data = cricbuzz_data

    match_header = data['matchHeader']
    miniscore = data['miniscore']
    commentary_list = data['commentaryList']

    # Get or create match
    match_id = get_or_create_match(match_header)

    hit11_scorecard = {
        'matchId': match_id,
        'matchDescription': match_header['matchDescription'],
        'matchFormat': match_header['matchFormat'],
        'matchType': match_header['matchType'],
        'startTimestamp': match_header['matchStartTimestamp'],
        'endTimestamp': match_header['matchCompleteTimestamp'],
        'status': match_header['status'],
        'result': convert_result(match_header),
        'team1': convert_team(match_header['team1']),
        'team2': convert_team(match_header['team2']),
        'innings': convert_innings(miniscore, commentary_list, match_header)
    }

    return hit11_scorecard


def convert_result(match_header):
    cricbuzz_result = match_header['result']
    winTeamId = cricbuzz_result.get('winningTeamId', 0)
    if winTeamId in [match_header['team1']['id'], match_header['team2']['id']]:
        winTeamDetails = match_header[winTeamId]
        internal_winning_team_id = get_or_create_internal_team_id(winTeamId, winTeamDetails['name'],
                                                                  winTeamDetails['shortName'])
    else:
        internal_winning_team_id = 0

    return {
        'resultType': cricbuzz_result.get('resultType', ''),
        'winningTeam': cricbuzz_result.get('winningTeam', ''),
        'winningTeamId': internal_winning_team_id,
        'winningMargin': cricbuzz_result.get('winningMargin', 0),
        'winByRuns': cricbuzz_result.get('winByRuns', False),
        'winByInnings': cricbuzz_result.get('winByInnings', False)
    }


def convert_team(cricbuzz_team):
    print(cricbuzz_team)
    name = cricbuzz_team['name']
    shortName = cricbuzz_team['shortName']
    internal_id = get_or_create_internal_team_id(cricbuzz_team['id'], name, shortName)
    return {
        'id': internal_id,
        'name': name,
        'shortName': shortName
    }


def convert_innings(miniscore, commentary_list, matchHeader):

    # get bowling team
    batting_team, bowling_team = get_teams(miniscore, matchHeader)
    miniscore['bowlTeam'] = bowling_team

    batting_team = convert_team(batting_team)
    bowling_team = convert_team(bowling_team)

    batting_performance = convert_batting_performances(miniscore)
    bowling_performance = convert_bowling_performances(miniscore, bowling_team)

    current_innings = {
        'inningsId': miniscore['inningsId'],
        'battingTeam': batting_team,
        'bowlingTeam': bowling_team,
        'totalRuns': miniscore['batTeam']['teamScore'],
        'wickets': miniscore['batTeam']['teamWkts'],
        'totalExtras': 0,  # We don't have this information in the given data
        'overs': float(str(miniscore['overs'])),
        'runRate': float(miniscore['currentRunRate']),
        'battingPerformances': batting_performance,
        'bowlingPerformances': bowling_performance,
        'fallOfWickets': [],  # We don't have this information in the given data
        'partnerships': [],  # We don't have this information in the given data
        'ballByBallEvents': convert_ball_events(batting_performance, bowling_performance, commentary_list)
    }
    return current_innings


def convert_batting_performances(miniscore):
    performances = []
    criccbuzz_team_id = miniscore['batTeam']['teamId']

    for batsman in [miniscore['batsmanStriker'], miniscore['batsmanNonStriker']]:
        player_name = batsman['batName']
        internal_id = get_or_create_internal_player_id(batsman['batId'], player_name, criccbuzz_team_id)
        performances.append({
            'playerId': internal_id,
            'playerName': player_name,
            'runs': batsman['batRuns'],
            'balls': batsman['batBalls'],
            'fours': batsman['batFours'],
            'sixes': batsman['batSixes'],
            'strikeRate': float(batsman['batStrikeRate']),
            'outDescription': None,
            'wicketTaker': None
        })
    return performances


def get_teams(miniscore, match_header):
    bat_team_id = miniscore['batTeam']['teamId']
    if match_header['team1']['id'] == bat_team_id:
        return match_header['team1'], match_header['team2']
    else:
        return match_header['team2'], match_header['team1']


def convert_bowling_performances(miniscore, bowling_team):
    print('bowling_performances')
    print(miniscore)
    performances = []
    criccbuzz_team_id = bowling_team['id']

    for bowler in [miniscore['bowlerStriker'], miniscore['bowlerNonStriker']]:
        player_name = bowler['bowlName']
        bowler_id = bowler['bowlId']
        if bowler_id == miniscore['bowlerStriker']:
            on_strike = 1
        else:
            on_strike = 0

        internal_id = get_or_create_internal_player_id(bowler['bowlId'], player_name, criccbuzz_team_id)
        performances.append({
            'playerId': internal_id,
            'playerName': bowler['bowlName'],
            'overs': float(bowler['bowlOvs']),
            'maidens': bowler['bowlMaidens'],
            'runs': bowler['bowlRuns'],
            'wickets': bowler['bowlWkts'],
            'economy': float(str(bowler['bowlEcon'])),
            'noBalls': bowler['bowlNoballs'],
            'wides': bowler['bowlWides'],
            'on_strike': on_strike
        })
    return performances


def convert_ball_events(batting_performance, bowling_performance, commentary_list):
    events = []
    for comm in commentary_list:
        if 'ballNbr' in comm and comm['ballNbr'] > 0:
            event = {
                'inningsId': comm['inningsId'],
                'overNumber': int(float(comm['overNumber'])),
                'ballNumber': comm['ballNbr'],
                'batsmanId': 0,  # We don't have this information in the given data
                'bowlerId': 0,  # We don't have this information in the given data
                'runsScored': 0,  # We need to parse this from the commentary text
                'extraType': None,
                'extraRuns': 0,
                'isWicket': comm['event'] == 'WICKET',
                'wicketType': None,
                'playerOutId': None,
                'isWide': False,
                'isNoBall': False,
                'isBye': False,
                'isLegBye': False,
                'isPenalty': False
            }
            events.append(event)
    return events


def process_cricbuzz_data(cricbuzz_data):
    try:
        hit11_scorecard = convert_cricbuzz_to_hit11(cricbuzz_data)
        if send_to_zeus(hit11_scorecard):
            print("Data processed and sent to Zeus successfully.")
        else:
            print("Failed to send data to Zeus.")
    except Exception as e:
        print(f"Error processing Cricbuzz data: {e}")
        print(traceback.format_exc())


while True:
    try:
        cricbuzz_data = call_cricbuzz_commentry_api(91992)
        process_cricbuzz_data(cricbuzz_data)
    except FileNotFoundError:
        print("Error: cricbuzz_data.json file not found.")
    except json.JSONDecodeError:
        print("Error: Invalid JSON data in cricbuzz_data.json.")
    except Exception as e:
        print(f"An unexpected error occurred: {e}")

    if os.getenv("PROD", False):
        time.sleep(3)
    else:
        break