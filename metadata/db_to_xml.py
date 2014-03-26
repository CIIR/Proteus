#!/usr/bin/python
import sys, sqlite3

#write the contents of a metadata database to a trectext file
#arguments: <database> <output-file>
def main():

    #open output file
    file_writer = open(sys.argv[2],'w')

    conn = sqlite3.connect(sys.argv[1])
    all_meta = conn.execute('''SELECT identifier, raw_creator, raw_title, raw_date, raw_language, raw_publisher, raw_volume, date FROM metadata''')
    counter = 0
    for m in all_meta:
        trecDoc = "<DOC>\n<DOCNO>" + str(counter) + "</DOCNO>\n<TEXT>\n"         
        trecDoc = trecDoc + "<identifier>%s</identifier>\n" % (m[0])
        trecDoc = trecDoc + "<raw_creator>%s</raw_creator>\n" % (m[1])
        trecDoc = trecDoc + "<raw_title>%s</raw_title>\n" % (m[2])
        trecDoc = trecDoc + "<raw_date>%s</raw_date>\n" % (m[3])
        trecDoc = trecDoc + "<raw_language>%s</raw_language>\n" % (m[4])
        trecDoc = trecDoc + "<raw_publisher>%s</raw_publisher>\n" % (m[5])
        trecDoc = trecDoc + "<raw_volume>%s</raw_volume>\n" % (m[6])
        trecDoc = trecDoc + "<date>%s</date>\n" % (m[7])
        command = '''SELECT author_identifier FROM authorship WHERE work_identifier = '%s' ''' % (m[0].replace("'","''").strip())
        cursor = conn.execute(command)
        authors = cursor.fetchall()
        print "WORK: " + m[0]
        for author in authors:
            print author[0].encode('utf8')
            command = '''SELECT name FROM alias WHERE author_identifier = '%s' ''' % (author[0].replace("'","''"))
            print command.encode('utf8')
            cursor = conn.execute(command)
            names = cursor.fetchall()
            for name in names:
                trecDoc = trecDoc + "<author>%s</author>\n" % (name[0])
        trecDoc = trecDoc + "</TEXT>\n</DOC>\n"
        file_writer.write(trecDoc.encode('utf8'))
        counter += 1
    file_writer.close()

main()
