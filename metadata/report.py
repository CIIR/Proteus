import sys, sqlite3

#reports tag frequencies
def main():
    conn = sqlite3.connect(sys.argv[1])
    all_meta = conn.execute("SELECT identifier, raw_creator, raw_title, raw_date, raw_language, raw_publisher, raw_volume, date FROM metadata")
    frequencies = {"<identifier>":0,"<creator>":0,"<title>":0,"<date>":0,"<language>":0,"<publisher>":0,"<volume>":0,"date":0}
    for m in all_meta:
        if  not m[0] == "":
            frequencies["<identifier>"] = frequencies["<identifier>"] + 1
        if  not m[1] == "":
            frequencies["<creator>"] = frequencies["<creator>"] + 1
        if  not m[2] == "":
            frequencies["<title>"] = frequencies["<title>"] + 1
        if  not m[3] == "":
            frequencies["<date>"] = frequencies["<date>"] + 1
        if  not m[4] == "":
            frequencies["<language>"] = frequencies["<language>"] + 1
        if  not m[5] == "":
            frequencies["<publisher>"] = frequencies["<publisher>"] + 1
        if  not m[6] == "":
            frequencies["<volume>"] = frequencies["<volume>"] + 1
        if  not m[7] == "":
            frequencies["date"] = frequencies["date"] + 1

    print "frequencies"
    for key in frequencies:
        print key +": " + str(frequencies[key])

main()
