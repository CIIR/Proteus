import sys, sqlite3

def main():
    conn = sqlite3.connect(sys.argv[1])
    cursor = conn.execute('''CREATE TABLE metadata(
        "identifier" varchar(40) NOT NULL PRIMARY KEY,
        "raw_creator" varchar(40),
        "raw_title" varchar(50),
        "raw_date" varchar(10),
        "raw_language" varchar(20),
        "raw_publisher" varchar(50),
        "raw_volume" varchar(10),
        "fulltext" varchar(500),
        "date" varchar(10));''')
    cursor = conn.execute('''CREATE TABLE person(
                    "id" VARCHAR(50) PRIMARY KEY NOT NULL,
                    "wiki_entry" VARCHAR(100));''')
    cursor = conn.execute('''CREATE TABLE authorship(
                    "author_identifier" VARCHAR(50) NOT NULL,
                    "work_identifier" VARCHAR(100) NOT NULL,
                    FOREIGN KEY ("author_identifier") REFERENCES person(id),
                    FOREIGN KEY ("work_identifier") REFERENCES person(metadata_metadata),
                    PRIMARY KEY ("author_identifier", "work_identifier"));''')
    cursor = conn.execute('''CREATE TABLE alias(
                    "author_identifier" VARCHAR(50) NOT NULL,
                    "name" VARCHAR(50) NOT NULL,
                    FOREIGN KEY ("author_identifier") REFERENCES person(id),
                    PRIMARY KEY ("author_identifier", "name"));''')
    conn.close()



main()
