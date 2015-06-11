Automatos provides scripts for automating the use of the Proteus system.

To compile the modules, run
python3 setup.py

update_books.py allows for the processing and indexing of djvu files in several
different modes. By default, the input is assumed to be a directory and
the files to be indexed are assumed to be any djvu files that have been added
or modified since the previous time that update_books was run. The metadata file
for each book is assumed to be in the same directory as the djvu file.

USAGE

python3 update_books.py [OPTIONS] [djvu directory path] [output directory path]

OPTIONS

-all    assumes input file is a directory and adds all djvu files regardless of
        modification time.

-list   assumes input file is a list of books and adds only djvu files in that 
        list
