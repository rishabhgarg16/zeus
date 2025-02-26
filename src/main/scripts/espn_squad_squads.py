import requests
from bs4 import BeautifulSoup
import pandas as pd

# List of squad URLs
squad_urls = [
    "https://www.espncricinfo.com/series/icc-men-s-t20-world-cup-2024-1411166/afghanistan-squad-1431702/series-squads",
    # "https://www.espncricinfo.com/series/icc-men-s-t20-world-cup-2024-1411166/australia-squad-1431715/series-squads",
    # "https://www.espncricinfo.com/series/icc-men-s-t20-world-cup-2024-1411166/bangladesh-squad-1433740/series-squads",
    # "https://www.espncricinfo.com/series/icc-men-s-t20-world-cup-2024-1411166/canada-squad-1431914/series-squads",
    # "https://www.espncricinfo.com/series/icc-men-s-t20-world-cup-2024-1411166/england-squad-1431604/series-squads",
    # "https://www.espncricinfo.com/series/icc-men-s-t20-world-cup-2024-1411166/india-squad-1431601/series-squads",
    # "https://www.espncricinfo.com/series/icc-men-s-t20-world-cup-2024-1411166/ireland-squad-1432769/series-squads",
    # "https://www.espncricinfo.com/series/icc-men-s-t20-world-cup-2024-1411166/nepal-squad-1431788/series-squads",
    # "https://www.espncricinfo.com/series/icc-men-s-t20-world-cup-2024-1411166/netherlands-squad-1433671/series-squads",
    # "https://www.espncricinfo.com/series/icc-men-s-t20-world-cup-2024-1411166/new-zealand-squad-1431440/series-squads",
    # "https://www.espncricinfo.com/series/icc-men-s-t20-world-cup-2024-1411166/namibia-squad-1433240/series-squads",
    # "https://www.espncricinfo.com/series/icc-men-s-t20-world-cup-2024-1411166/oman-squad-1431785/series-squads",
    # "https://www.espncricinfo.com/series/icc-men-s-t20-world-cup-2024-1411166/pakistan-squad-1435201/series-squads",
    # "https://www.espncricinfo.com/series/icc-men-s-t20-world-cup-2024-1411166/papua-new-guinea-squad-1432846/series-squads",
    # "https://www.espncricinfo.com/series/icc-men-s-t20-world-cup-2024-1411166/scotland-squad-1432589/series-squads",
    # "https://www.espncricinfo.com/series/icc-men-s-t20-world-cup-2024-1411166/south-africa-squad-1431579/series-squads",
    # "https://www.espncricinfo.com/series/icc-men-s-t20-world-cup-2024-1411166/sri-lanka-squad-1433061/series-squads",
    # "https://www.espncricinfo.com/series/icc-men-s-t20-world-cup-2024-1411166/uganda-squad-1432588/series-squads",
    # "https://www.espncricinfo.com/series/icc-men-s-t20-world-cup-2024-1411166/united-states-of-america-squad-1432101/series-squads",
    # "https://www.espncricinfo.com/series/icc-men-s-t20-world-cup-2024-1411166/west-indies-squad-1432115/series-squads"
]

squads_data = []

# Function to scrape squad data from a URL
def scrape_squad(url):
    response = requests.get(url)
    soup = BeautifulSoup(response.content, 'html.parser')

    # Extract team name from URL
    team_name = url.split('/')[-2].replace('-', ' ').title()
    print(f"Scraping team: {team_name}")

    player_sections = soup.find_all('div', class_='ds-relative ds-flex ds-flex-row ds-space-x-4 ds-p-3')
    print(f"player section: {player_sections}")

    for player_section in player_sections:
        # Extract player name
        player_name_tag = player_section.find('span', class_='ds-text-compact-s ds-font-bold ds-text-typo ds-underline ds-decoration-ui-stroke hover:ds-text-typo-primary hover:ds-decoration-ui-stroke-primary ds-block ds-cursor-pointer')
        if not player_name_tag:
            continue
        player_name = player_name_tag.text.strip()
        # print(f"Player Name: {player_name}")

        # Extract player image
        player_image_tag = player_section.find('span', class_='ds-border ds-border-line-default-translucent ds-text-typo ds-bg-ui-fill ds-overflow-hidden ds-flex ds-items-center ds-justify-center ds-w-12 ds-h-12 ds-rounded-full').find('img')
        player_image = player_image_tag['src'] if player_image_tag else 'N/A'

        # Extract player role
        player_role_tag = player_section.find('p', class_='ds-text-tight-s ds-font-regular ds-mb-2 ds-mt-1')
        player_role = player_role_tag.text.strip() if player_role_tag else 'N/A'

        # Extract player age
        age_tags = player_section.find_all('span', class_='ds-text-compact-xxs ds-font-bold')
        player_age = age_tags[0].text.strip() if len(age_tags) > 0 else 'N/A'

        # Initialize batting and bowling styles
        player_batting_style = 'N/A'
        player_bowling_style = 'N/A'

        # Extract batting and bowling styles
        style_tags = player_section.find_all('div', class_='ds-flex ds-items-start ds-space-x-1')

        for tag in style_tags:
            tag_text = tag.find_all('span', class_='ds-text-compact-xxs')
            if len(tag_text) > 1:
                if 'Batting' in tag_text[0].text:
                    player_batting_style = tag_text[1].text.strip()
                elif 'Bowling' in tag_text[0].text:
                    player_bowling_style = tag_text[1].text.strip()

        squads_data.append({
            'Team': team_name,
            'Player Name': player_name,
            'Image': player_image,
            'Role': player_role,
            'Age': player_age,
            'Batting Style': player_batting_style,
            'Bowling Style': player_bowling_style
        })

        break


# Iterate through each squad URL and scrape data
for url in squad_urls:
    scrape_squad(url)

# Convert to DataFrame
squads_df = pd.DataFrame(squads_data)

# Save squads data to CSV
squads_df.to_csv('squads.csv', index=False)

print("Scraping completed! Data saved to squads.csv.")