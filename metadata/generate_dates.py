import sys, re, time, sqlite3

#takes the raw_date and converts it into a year to be stored in date
def main():
    start_time = time.localtime()
    conn = sqlite3.connect(sys.argv[1])
    all_meta = conn.execute("SELECT identifier, raw_date FROM metadata")
    for m in all_meta:
        r = re.search(".*([1-2][0-9][0-9][0-9]).*", m[1])
        if not (r is None):
            date = r.group(1)
        else:
            date = ""
        command = "UPDATE metadata set date = '" + date + "' WHERE identifier ='" + m[0] +"'"
        conn.execute(command)
        
    conn.commit()
    end_time = time.localtime()
    print start_time
    print end_time

main()
