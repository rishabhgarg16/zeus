# /usr/bin/python3 /Users/rishabhgarg/IdeaProjects/zeus/src/main/scripts/espn_scrape.py

import requests
from bs4 import BeautifulSoup
import pandas as pd
import json

# Function to convert ordinal numbers to integers
def ordinal_to_int(ordinal):
    ordinals = {
        '1st': 1, '2nd': 2, '3rd': 3, '4th': 4, '5th': 5, '6th': 6,
        '7th': 7, '8th': 8, '9th': 9, '10th': 10, '11th': 11, '12th': 12,
        '13th': 13, '14th': 14, '15th': 15, '16th': 16, '17th': 17,
        '18th': 18, '19th': 19, '20th': 20, '21st': 21, '22nd': 22,
        '23rd': 23, '24th': 24, '25th': 25, '26th': 26, '27th': 27,
        '28th': 28, '29th': 29, '30th': 30, '31st': 31, '32nd': 32,
        '33rd': 33, '34th': 34, '35th': 35, '36th': 36, '37th': 37,
        '38th': 38, '39th': 39, '40th': 40, '41st': 41, '42nd': 42,
        '43rd': 43, '44th': 44, '45th': 45, '46th': 46, '47th': 47,
        '48th': 48, '49th': 49, '50th': 50, '51st': 51, '52nd': 52,
        '53rd': 53, '54th': 54, '55th' : 55, '56th': 56, '57th': 57
    }
    return ordinals.get(ordinal, ordinal)


# URL to scrape fixtures
fixtures_url = "https://www.espncricinfo.com/series/icc-men-s-t20-world-cup-2024-1411166/match-schedule-fixtures"
response_fixtures = requests.get(fixtures_url)
soup_fixtures = BeautifulSoup(response_fixtures.content, 'html.parser')

# Possible div classes to look for fixture data
div_classes = [
    'ds-p-4 hover:ds-bg-ui-fill-translucent',
    'ds-p-4 hover:ds-bg-ui-fill-translucent ds-border-t ds-border-line',
    'ds-p-4 hover:ds-bg-ui-fill-translucent ds-bg-ui-fill-translucent-hover'
    'ds-p-4 hover:ds-bg-ui-fill-translucent ds-border-none ds-border-t ds-border-line ds-bg-ui-fill-translucent-hover'
]

fixtures_data = []
previous_date = 'N/A'

for div_class in div_classes:
    fixtures_section = soup_fixtures.find_all('div', class_=div_class)
    print(f" fixtures section {fixtures_section}")
    for fixture in fixtures_section:
        # Extract match date
        date_tag = fixture.find('div', class_='ds-text-compact-xs ds-font-bold ds-w-24')
        match_date = date_tag.text.strip() if date_tag else previous_date

        # Extract match link and info
        match_link_tag = fixture.find('a', class_='ds-no-tap-higlight')
        match_link = f"https://www.espncricinfo.com{match_link_tag['href']}" if match_link_tag else 'N/A'

        # Extract match info
        match_info_tag = fixture.find('span', class_='ds-text-tight-s ds-font-medium ds-text-typo')
        if match_info_tag:
            match_info_text = match_info_tag.text.strip()
            match_info_parts = match_info_text.split(',')
            if len(match_info_parts) >= 2:
                match_number_ordinal = match_info_parts[0].strip().split(' ')[0]
                match_number = ordinal_to_int(match_number_ordinal)
                match_group = match_info_parts[1].strip().split(' ')[1]
                city = match_info_parts[1].strip().split(' ')[-1]
                tournament_type = "T20"
            else:
                match_number = match_group = city = tournament_type = 'N/A'
        else:
            match_number = match_group = city = tournament_type = 'N/A'

        # Extract JSON data
        script_tag = fixture.find('script', type='application/ld+json')
        if script_tag:
            json_data = json.loads(script_tag.string)
            start_date = json_data.get('startDate', 'N/A')
            competitors = json_data.get('broadcastOfEvent', {}).get('competitor', [])
            team_a = competitors[0]['name'] if len(competitors) > 0 else 'N/A'
            team_b = competitors[1]['name'] if len(competitors) > 1 else 'N/A'
            location = json_data.get('location', {}).get('address', {}).get('addressLocality', 'N/A')
            stadium = json_data.get('location', {}).get('name', 'N/A')
            country = json_data.get('location', {}).get('address', {}).get('addressCountry', 'N/A')
            tournament_name = json_data.get('broadcastOfEvent', {}).get('organizer', {}).get('name', 'N/A')
        else:
            start_date = location = stadium = country = tournament_name = 'N/A'

        # Extract time details
        time_details_tag = fixture.find('span', class_='ds-text-tight-xs')
        time_details = time_details_tag.text.strip() if time_details_tag else 'N/A'
        gmt_time = time_details.split('|')[0].strip() if '|' in time_details else 'N/A'

        # Extract match status
        match_status_tag = fixture.find('p', class_='ds-text-tight-s ds-font-medium ds-line-clamp-2 ds-text-typo')
        match_status = match_status_tag.text.strip() if match_status_tag else 'N/A'

        # Extract team image URLs
        # Extract team image URLs by checking the alt attribute
        team_a_img_url = 'N/A'
        team_b_img_url = 'N/A'

        team_img_tags = fixture.find_all('img')
        for img_tag in team_img_tags:
            alt_text = img_tag.get('alt', '')
            img_src = img_tag.get('src', '')
            if team_a in alt_text and 'lazyimage-transparent' not in img_src:
                team_a_img_url = img_tag['src']
            elif team_b in alt_text and 'lazyimage-transparent' not in img_src:
                team_b_img_url = img_tag['src']

        fixtures_data.append({
            'Match Number': match_number,
            'Match Group': match_group,
            'Team A': team_a,
            'Team A Image URL': team_a_img_url,
            'Team B': team_b,
            'Team B Image URL': team_b_img_url,
            'Time (GMT)': gmt_time,
            'City': city,
            'Stadium': stadium,
            'Country': country,
            'Tournament Name': tournament_name,
            'Tournament Type': tournament_type,
            'Match Status': match_status,
            'Match Link': match_link,
            'Start Date': start_date
        })

        # Update the previous_date if date_tag was present
        if date_tag:
            previous_date = match_date
        # break

# Convert to DataFrame
fixtures_df = pd.DataFrame(fixtures_data)

# Save fixtures data to CSV
fixtures_df.to_csv('fixtures.csv', index=False)

print("Scraping completed! Data saved to fixtures.csv.")
