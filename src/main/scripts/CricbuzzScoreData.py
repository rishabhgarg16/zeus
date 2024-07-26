import json
import mysql.connector
from decimal import Decimal
import requests
import traceback
import datetime

def get_data():
  return {
      "team_id": None,
  }


# Zeus API configuration
ZEUS_API_ENDPOINT = "http://localhost:8080/api/events/scorecard"
# ZEUS_API_KEY = "your_api_key_here"


def call_cricbuzz_commentry_api(match_id) :
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
            'created_at': datetime.datetime.now(),
            'updated_at': datetime.datetime.now()
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
        # "Authorization": f"Bearer {ZEUS_API_KEY}"
    }

    try:
        response = requests.post(ZEUS_API_ENDPOINT, data = hit11_scorecard, headers=headers)
        response.raise_for_status()  # Raises an HTTPError for bad responses (4xx or 5xx)
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

    hit11_scorecard = {
        'matchId': match_header['matchId'],
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
      internal_winning_team_id = get_or_create_internal_team_id(winTeamId, winTeamDetails['name'], winTeamDetails['shortName'])
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
    innings = []

    #get bowling team
    batting_team, bowling_team = get_teams(miniscore, matchHeader)
    miniscore['bowlTeam'] = bowling_team

    batting_team=convert_team(batting_team)
    bowlingTeam= convert_team(bowling_team)

    batting_performance = convert_batting_performances(miniscore)
    bowling_performance = convert_bowling_performances(miniscore, bowling_team)

    current_innings = {
        'inningsId': miniscore['inningsId'],
        'battingTeam': batting_team,
        'bowlingTeam': bowling_team,
        'totalRuns': miniscore['batTeam']['teamScore'],
        'wickets': miniscore['batTeam']['teamWkts'],
        'totalExtras': 0,  # We don't have this information in the given data
        'overs': Decimal(str(miniscore['overs'])),
        'runRate': float(miniscore['currentRunRate']),
        'battingPerformances': batting_performance,
        'bowlingPerformances': bowling_performance,
        'fallOfWickets': [],  # We don't have this information in the given data
        'partnerships': [],  # We don't have this information in the given data
        'ballByBallEvents': convert_ball_events(batting_performance, bowling_performance, commentary_list)
    }
    innings.append(current_innings)
    return innings

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
    print('bowling_performances' )
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
            'economy': Decimal(str(bowler['bowlEcon'])),
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

# Example usage
# with open('cricbuzz_data.json', 'r') as f:
#     cricbuzz_data = f.read()

# hit11_scorecard = convert_cricbuzz_to_hit11(cricbuzz_data)
# print(json.dumps(hit11_scorecard, indent=2))

def process_cricbuzz_data(cricbuzz_data):
    try:
        hit11_scorecard = convert_cricbuzz_to_hit11(cricbuzz_data)
        print(hit11_scorecard)
        # if send_to_zeus(hit11_scorecard):
        #     print("Data processed and sent to Zeus successfully.")
        # else:
        #     print("Failed to send data to Zeus.")
    except Exception as e:
        print(f"Error processing Cricbuzz data: {e}")
        print(traceback.format_exc())

try:
    cricbuzz_data = call_cricbuzz_commentry_api(91992)
    process_cricbuzz_data(cricbuzz_data)
except FileNotFoundError:
    print("Error: cricbuzz_data.json file not found.")
except json.JSONDecodeError:
    print("Error: Invalid JSON data in cricbuzz_data.json.")
except Exception as e:
    print(f"An unexpected error occurred: {e}")

