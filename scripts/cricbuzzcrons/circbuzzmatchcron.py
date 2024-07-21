from datetime import datetime
import json
import mysql.connector
import requests

# Database connections
mydb = mysql.connector.connect(
    host="ls-a6927155ebc8223b62e0da94714b39337fdb981a.c9yg6ks8shtr.ap-south-1.rds.amazonaws.com",
    user="admin",
    password="adminpass",
    database="hit11_test"
)
mycursor = mydb.cursor()

url = "https://cricbuzz-cricket.p.rapidapi.com/matches/v1/recent"
headers = {
    'x-rapidapi-key': "1c1096fee2msh8653286051bb74fp116c19jsn80f85e04a65b",
    'x-rapidapi-host': "cricbuzz-cricket.p.rapidapi.com"
}
response = requests.get(url, headers=headers)
recent_matches = response.json()

def update_table(data):
    match = data['matchInfo']
    # Convert timestamps from milliseconds to datetime
    start_date = datetime.fromtimestamp(int(match['startDate']) / 1000).strftime('%Y-%m-%d %H:%M:%S')
    end_date = datetime.fromtimestamp(int(match['endDate']) / 1000).strftime('%Y-%m-%d %H:%M:%S')

    # Create SQL insertion statement
    sql = f"""
  INSERT INTO matches (
      external_id, match_group, team1, team2, city, stadium, status, tournament_name, match_type, 
      start_date, end_date, current_inning_id, team1_id, team2_id
  ) VALUES (
      '{match['matchId']}',
      '{match['seriesName']}',
      '{match['team1']['teamName']}',
      '{match['team2']['teamName']}',
      '{match['venueInfo']['city']}',
      '{match['venueInfo']['ground']}',
      '{match['state']}',
      '{match['seriesName']}',
      '{match['matchFormat']}',
      '{start_date}',
      '{end_date}',
      {1 if 'currBatTeamId' in match else 'NULL'},
      {match['team1']['teamId']},
      {match['team2']['teamId']}
  ) ON DUPLICATE KEY UPDATE
    match_group = VALUES(match_group),
    team1 = VALUES(team1),
    team2 = VALUES(team2),
    city = VALUES(city),
    stadium = VALUES(stadium),
    country = VALUES(country),
    status = VALUES(status),
    tournament_name = VALUES(tournament_name),
    match_type = VALUES(match_type),
    start_date = VALUES(start_date),
    end_date = VALUES(end_date),
    current_inning_id = VALUES(current_inning_id),
    team1_id = VALUES(team1_id),
    team2_id = VALUES(team2_id);
  """

    try:
        mycursor.execute(sql)
        mydb.commit()
        print(mycursor.rowcount, "record inserted.")
    except Exception as e:
        print(f"Error updating {match['matchId']} due to {e}")


for typeMatch in recent_matches['typeMatches']:
    if typeMatch['matchType'] in ['International']:
        for seriesMatch in typeMatch['seriesMatches']:
            if 'seriesAdWrapper' in seriesMatch:
                for amatch in seriesMatch['seriesAdWrapper']['matches']:
                    update_table(amatch)

