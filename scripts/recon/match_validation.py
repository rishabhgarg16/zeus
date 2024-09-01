import mysql.connector
import http.client
import json


def init_db():
    db = mysql.connector.connect(
        host="ls-a6927155ebc8223b62e0da94714b39337fdb981a.c9yg6ks8shtr.ap-south-1.rds.amazonaws.com",
        user="admin",
        password="adminpass",
        database="hit11"
    )
    return  db, db.cursor()

db, cursor = init_db()


def check_cricbuzz_match_status(cricbuzz_id):
    conn = http.client.HTTPSConnection("cricbuzz-cricket.p.rapidapi.com")

    headers = {
        'x-rapidapi-key': "cf1c48d00fmshcf81b48d77b26b8p1e23f0jsn7bf53d9ff8d9",
        'x-rapidapi-host': "cricbuzz-cricket.p.rapidapi.com"
    }
    try:
        conn.request("GET", f"/mcenter/v1/{cricbuzz_id}", headers=headers)
        res = conn.getresponse()
        data = json.loads(res.read().decode("utf-8"))
        print(data['matchInfo']['state'])
        return data['matchInfo']['state']
    except Exception as e:
        print(e)
    return None

# Enddate < current && match is live
# Raise warning if enddate reached
# End match if 4 hours passed
def get_live_matches_to_update():
    query = f"""
            SELECT * FROM `matches`
            WHERE end_date<TIMESTAMPADD(MINUTE,240,CURRENT_TIMESTAMP()) and status != "Complete"
        """
    cursor.execute(query)
    myresult = cursor.fetchall()
    for x in myresult:
        print(f"Update: MatchId, {x[0]}, {x[7]}, {x[13]}, {x[18]}")
        if x[18] not in [None, 0, "0"]:
            if check_cricbuzz_match_status(x[18]) == "complete":
                update_match_status(x[0], "Complete")
        else:
            print(f"cricBuzz match id not defined for matchId {x[0]}")

def update_match_status(matchId, status):
    query = f"""
        UPDATE `matches` 
        SET status='{status}'
        where id={matchId}
    """
    res = cursor.execute(query)
    db.commit()
    print(cursor.rowcount, "record(s) affected")

# Raise warning for last 4 hours match
def get_live_matches_to_raise_warning():
    query = f"""
            SELECT * FROM `matches`
            WHERE end_date<TIMESTAMPADD(MINUTE,240,CURRENT_TIMESTAMP())
            and end_date>CURRENT_TIMESTAMP()
            and status != "Complete"
        """
    cursor.execute(query)
    myresult = cursor.fetchall()
    for x in myresult:
        print(f"Warning: MatchId, {x[1]}")

get_live_matches_to_update()
get_live_matches_to_raise_warning()

## Update Questions
def find_obsolete_questions():
    query = """
        SELECT *, mat.status FROM `pulse_questions` as pq 
        left join `matches` as mat on pq.match_id = mat.id 
        where mat.status="Complete" and pq.status!="RESOLVED";
    """
    cursor.execute(query)
    myresult = cursor.fetchall()
    for x in myresult:
        print(f"Error: Question '{x[2]}' is in '{x[10]}' state while matchId '{x[1]}' is in '{x[49]}' state")

find_obsolete_questions()

def find_unresolved_trades():
    query = """
        SELECT `trades`.id, pulse_id, match_id, result, mat.status as match_status
        FROM `trades` 
        left join `matches` as mat
        on `trades`.`match_id`=mat.id
        where `trades`.`result`="ACTIVE" and mat.status = "COMPLETE";
    """
    cursor.execute(query)
    myresult = cursor.fetchall()
    for x in myresult:
        print(f"Error: Trade '{x[0]}' is in '{x[3]}' state while matchId '{x[2]}' is in '{x[4]}' state")

find_unresolved_trades()