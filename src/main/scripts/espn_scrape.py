import requests
from bs4 import BeautifulSoup
import pandas as pd

# URL to scrape squads
url = "https://www.espncricinfo.com/series/icc-men-s-t20-world-cup-2024-1411166/squads"

# Send a GET request to the URL
response = requests.get(url)
soup = BeautifulSoup(response.content, 'html.parser')

# Scraping squads and players details
squads_section = soup.find_all('div', class_='ds-px-4 ds-py-3')

squads_data = []

for section in squads_section:
    team_name_tag = section.find('h5')
    if not team_name_tag:
        continue

    team_name = team_name_tag.text.strip()
    players = section.find_all('a', class_='ds-inline-flex ds-items-start ds-leading-tight')

    for player in players:
        player_name = player.text.strip()
        player_link = player['href']

        squads_data.append({
            'Team': team_name,
            'Player Name': player_name,
            'Player Profile Link': f"https://www.espncricinfo.com{player_link}"
        })

# Convert to DataFrame
squads_df = pd.DataFrame(squads_data)

# Save squads data to CSV
squads_df.to_csv('squads.csv', index=False)

# URL to scrape fixtures
fixtures_url = "https://www.espncricinfo.com/series/icc-men-s-t20-world-cup-2024-1411166/match-schedule-fixtures"
response_fixtures = requests.get(fixtures_url)
soup_fixtures = BeautifulSoup(response_fixtures.content, 'html.parser')

fixtures_section = soup_fixtures.find_all('div', class_='ds-p-4 hover:ds-bg-ui-fill-translucent')

fixtures_data = []

for fixture in fixtures_section:
    match_details_tag = fixture.find('a', class_='ds-no-tap-higlight')
    if not match_details_tag:
        continue

    match_link = match_details_tag['href']
    match_summary = match_details_tag.find('div', class_='ds-text-compact-xxs').text.strip()

    team_tags = fixture.find_all('p', class_='ds-text-tight-m ds-font-bold ds-capitalize ds-truncate')
    if len(team_tags) == 2:
        team_a = team_tags[0].text.strip()
        team_b = team_tags[1].text.strip()
    else:
        team_a = team_b = 'N/A'

    date_time_tag = fixture.find('div', class_='ds-text-tight-m ds-font-bold')
    date_time = date_time_tag.text.strip() if date_time_tag else 'N/A'

    time_details_tag = fixture.find('span', class_='ds-text-tight-xs')
    time_details = time_details_tag.text.strip() if time_details_tag else 'N/A'
    gmt_time = time_details.split('|')[0].strip() if '|' in time_details else 'N/A'

    location_tag = fixture.find('div', class_='ds-truncate ds-text-tight-s ds-font-medium ds-text-typo-mid3')
    location = location_tag.text.strip() if location_tag else 'N/A'

    location_parts = location.split(',')
    if len(location_parts) > 1:
        match_info = location_parts[0].split()
        match_number = match_info[0] if match_info else 'N/A'
        match_group = match_info[2] if len(match_info) >= 3 else 'N/A'
    else:
        match_number = match_group = 'N/A'

    match_status_tag = fixture.find('p', class_='ds-text-tight-s ds-font-medium ds-line-clamp-2 ds-text-typo')
    match_status = match_status_tag.text.strip() if match_status_tag else 'N/A'

    fixtures_data.append({
        'Match': match_summary,
        'Team A': team_a,
        'Team B': team_b,
        'Date': date_time.split('T')[0] if 'T' in date_time else date_time,  # Extracting date if present
        'Time (GMT)': gmt_time,
        'Match Number': match_number,
        'Match Group': match_group,
        'Location': location,
        'Match Status': match_status,
        'Match Link': f"https://www.espncricinfo.com{match_link}"
    })

# Convert to DataFrame
fixtures_df = pd.DataFrame(fixtures_data)

# Save fixtures data to CSV
fixtures_df.to_csv('fixtures5.csv', index=False)

print("Scraping completed! Data saved to squads.csv and fixtures.csv.")
