Metadata Loading and Indexing Instructions
David Wemhoener
UMass CIIR
3-25-14

Instructions
Note: * indicates an optional step

To load a set of xml metadata files into a database
1. Create a new file named <database-name>
2. Run python generate_tables.py <database-name>
3. Run python quick_load.py <database-name> <metadata_file_list>
*4. Run python report.py <database-name>

To generate date and author entries from raw_date and raw_creator
1. Run python generate_dates.py <database-name>
2. Run python generate_authors.py <database-name>

To index the metadata entries
1. Run python db_to_xml.py <database-name> <output-file>
Note: make sure that the output file is in a directory called "xml"
2. Run java -jar ../homer/target/homer-0.3.jar build metadata.conf

