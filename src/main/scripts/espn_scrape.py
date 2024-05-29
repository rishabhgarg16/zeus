import requests
from bs4 import BeautifulSoup
import pandas as pd

# URL to scrape fixtures
fixtures_url = "https://www.espncricinfo.com/series/icc-men-s-t20-world-cup-2024-1411166/match-schedule-fixtures"
response_fixtures = requests.get(fixtures_url)
soup_fixtures = BeautifulSoup(response_fixtures.content, 'html.parser')

# Possible div classes to look for fixture data
div_classes = [
    'ds-p-4 hover:ds-bg-ui-fill-translucent',
    'ds-p-4 hover:ds-bg-ui-fill-translucent ds-border-none ds-border-t ds-border-line ds-bg-ui-fill-translucent-hover'
]

fixtures_data = []
previous_date = 'N/A'

for div_class in div_classes:
    fixtures_section = soup_fixtures.find_all('div', class_=div_class)

    for fixture in fixtures_section:
        # Extract match info
        match_info_tag = fixture.find('div', class_='ds-text-tight-s ds-font-regular ds-truncate ds-text-typo-mid3')
        if match_info_tag:
            match_info_span = match_info_tag.find('span', class_='ds-text-tight-s ds-font-medium ds-text-typo')
            if match_info_span:
                match_info_parts = match_info_span.text.strip().split(',')
                match_number = match_info_parts[0].strip() if len(match_info_parts) > 0 else 'N/A'
                match_group = match_info_parts[1].strip() if len(match_info_parts) > 1 else 'N/A'
                location = match_info_tag.text.split('•')[1].split(',')[0].strip() if '•' in match_info_tag.text else 'N/A'
                tournament_type_tag = match_info_tag.find('span', class_='ds-inline-flex ds-items-start ds-leading-none !ds-inline')
                tournament_type = tournament_type_tag.text.strip() if tournament_type_tag else 'N/A'
            else:
                match_number = match_group = location = tournament_type = 'N/A'
        else:
            match_number = match_group = location = tournament_type = 'N/A'

        # Extract match summary
        match_summary = match_info_tag.text.strip() if match_info_tag else 'N/A'

        date_tag = fixture.find('div', class_='ds-text-compact-xs ds-font-bold ds-w-24')
        print(f"date tag: {date_tag}")
        match_date = date_tag.text.strip() if date_tag else previous_date
        print(f"match date: {match_date}")

        team_tags = fixture.find_all('p', class_='ds-text-tight-m ds-font-bold ds-capitalize ds-truncate')
        if len(team_tags) == 2:
            team_a = team_tags[0].text.strip()
            team_b = team_tags[1].text.strip()
        else:
            team_a = team_b = 'N/A'

        time_details_tag = fixture.find('span', class_='ds-text-tight-xs')
        time_details = time_details_tag.text.strip() if time_details_tag else 'N/A'
        gmt_time = time_details.split('|')[0].strip() if '|' in time_details else 'N/A'

        match_status_tag = fixture.find('p', class_='ds-text-tight-s ds-font-medium ds-line-clamp-2 ds-text-typo')
        match_status = match_status_tag.text.strip() if match_status_tag else 'N/A'

        match_link_tag = fixture.find('a', class_='ds-no-tap-higlight')
        match_link = f"https://www.espncricinfo.com{match_link_tag['href']}" if match_link_tag else 'N/A'

        fixtures_data.append({
            'Match Number': match_number,
            'Match Group': match_group,
            'Match Summary': match_summary,
            'Team A': team_a,
            'Team B': team_b,
            'Date': match_date,
            'Time (GMT)': gmt_time,
            'Location': location,
            'Tournament Type': tournament_type,
            'Match Status': match_status,
            'Match Link': match_link
        })


        # Update the previous_date if date_tag was present
        if date_tag:
            previous_date = match_date

# Convert to DataFrame
fixtures_df = pd.DataFrame(fixtures_data)

# Save fixtures data to CSV
fixtures_df.to_csv('fixtures.csv', index=False)

print("Scraping completed! Data saved to fixtures.csv.")
