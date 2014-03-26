#!/usr/bin/python
import sys, os
import re
import time
import sqlite3
from subprocess import call, Popen, PIPE

#args: <database> <metadata file list>
def main():
    load(sys.argv[1], sys.argv[2])

def load(db, p):

    start_time = time.localtime()
    conn = sqlite3.connect(db)
    conn.text_factory = str

    #first open the list of the metadata files to be loaded
    path = p
    files = []
    file_list = open(path,"r")
    for line in file_list:
        files.append(line.strip())
    
    metadata_entries = []

    #for each file in the list
    for file in files:
        values = {"<identifier>":"","<creator>":"","<title>":"","<date>":"","<language>":"","<publisher>":"","<volume>":""}
        ft = ""

        #unarchive the file
        print "opening " + file
        gproc = Popen(["bunzip2", "-c",file], stdout=PIPE)
        for line in gproc.stdout:
            #print line.strip()
            #check if line has any of the tags of interest
            #and load lines into appropriate fields
            #compile frequency statistics
            results = []
            results.append(re.search("<identifier>(.*)</identifier>", line))
            results.append(re.search("<creator>(.*)</creator>", line))
            results.append(re.search("<title>(.*)</title>", line))
            results.append(re.search("<date>(.*)</date>", line))
            results.append(re.search("<language>(.*)</language>", line))
            results.append(re.search("<publisher>(.*)</publisher>", line))
            results.append(re.search("<volume>(.*)</volume>", line))
            if results[0]:
                if values["<identifier>"] == "":
                    values["<identifier>"] = results[0].group(1)
                else:
                    values["<identifier>"] = values["<identifier>"] + " | " + results[0].group(1)
            if results[1]:
                if values["<creator>"] == "":
                    values["<creator>"] = results[1].group(1)
                else:
                    values["<creator>"] = values["<creator>"] + " | " + results[1].group(1)
            if results[2]:
                if values["<title>"] == "":
                    values["<title>"] = results[2].group(1)
                else:
                    values["<title>"] = values["<title>"] + " | " + results[2].group(1)
            if results[3]:
                if values["<date>"] == "":
                    values["<date>"] = results[3].group(1)
                else:
                    values["<date>"] = values["<date>"] + " | " + results[3].group(1)
            if results[4]:
                if values["<language>"] == "":
                    values["<language>"] = results[4].group(1)
                else:
                    values["<language>"] = values["<language>"] + " | " + results[4].group(1)
            if results[5]:
                if values["<publisher>"] == "":
                    values["<publisher>"] = results[5].group(1)
                else:
                    values["<publisher>"] = values["<publisher>"] + " | " + results[5].group(1)
            if results[6]:
                if values["<volume>"] == "":
                    values["<volume>"] = results[6].group(1)
                else:
                    values["<volume>"] = values["<volume>"] + " | " + results[6].group(1)
            #store each line for fulltext
            ft = ft + line
            #print ""
        conn.execute('''INSERT INTO metadata
                    ("identifier","raw_creator","raw_title","raw_date",
                    "raw_language","raw_publisher","raw_volume","fulltext","date")
                    VALUES(?,?,?,?,?,?,?,?,?)''', (values["<identifier>"], values["<creator>"], values["<title>"], values["<date>"], values["<language>"], values["<publisher>"], values["<volume>"], ft, ""))

    conn.commit()

    end_time = time.localtime()

    print start_time
    print end_time

main()
