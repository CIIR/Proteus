import sys, sqlite3, time


def main():
    start_time = time.localtime()
    conn = sqlite3.connect(sys.argv[1])
    all_meta = conn.execute('''SELECT identifier, raw_creator FROM metadata''')
    #for each metadata entry
    for m in all_meta:
        print m[0]
        creators = m[1].split("|")
        if len(creators[0]) == 0:
            creators = []
        #examine its creators
        for c in creators:
            creator = c.replace("'","''").strip()
            command = '''SELECT * FROM alias WHERE name = '%s' ''' % (creator)
            print command.encode('cp850','replace')
            cursor = conn.execute(command)
            rows = cursor.fetchall()
            #if the creator does not have a person with that alias
            #create a new person and alias
            if len(rows) == 0:
                pid = creator + str(time.localtime()[1]) + str(time.localtime()[2]) + str(time.localtime()[3]) + str(time.localtime()[4]) + str(time.localtime()[5])
                pid = pid.replace(' ','')
                command = '''SELECT * FROM person WHERE id = '%s' ''' % (pid)
                cursor = conn.execute(command)
                results = cursor.fetchall()
                if len(results) == 0:
                    command = '''INSERT INTO person (id, wiki_entry) VALUES ('%s', '')''' % (pid)
                    conn.execute(command)
                    conn.commit()
                    command = '''INSERT INTO alias (author_identifier, name) VALUES ('%s', '%s')''' % (pid, creator)
                    conn.execute(command)
                    conn.commit()
            else:
                pid = rows[0][0].replace("'","''")
            #create a new authorship between this book and the person whose alias matches the creator
            try:
                command = '''INSERT INTO authorship (author_identifier, work_identifier) VALUES ('%s','%s')''' % (pid,m[0])
                try:
                    print command
                except UnicodeEncodeError:
                    print "ERROR: Non-ASCII String"
                conn.execute(command)
            except sqlite3.IntegrityError as e:
                print e.args[0]
            conn.commit()

    end_time = time.localtime()
    print start_time
    print end_time

main()
