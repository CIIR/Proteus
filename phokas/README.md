# phokas

Tokenize and do basic NLP annotations on books in the `.rawtei.gz`
format.

## Installation

First, it's currently necessary to install the following jar in your
repository by hand:

http://books.cs.umass.edu/downloads/dparser-2011-01-18.jar

Then, to compile, run:

    $ lein bin

This should produce an executable `target/phokas-1.0.0-SNAPSHOT`.

## Usage

To convert the `rawtei.gz` files, run as follows:

    $ cat <list of .toktei.gz output files> | phokas-1.0.0-SNAPSHOT

or

    $ phokas-1.0.0-SNAPSHOT <list of .toktei.gz output files>.gz

Yes, this is a bit weird: you give the program a list of the output
files you want it to produce, not a list of the input files.  I'll get
around to fixing this soon.  To produce a `.toktei.gz` file, it reads
the corresponding `.rawtei.gz` input file.

To produce the `.mbtei.gz` files with tagging and parsing info, you
run the same phokas command as follows:

    $ cat <list of .mbtei.gz output files> | phokas-1.0.0-SNAPSHOT

or

    $ phokas-1.0.0-SNAPSHOT <list of .mbtei.gz output files>.gz

These files should also be usable as input to KbBridge entity linking
code.
