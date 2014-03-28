# pontos

<<<<<<< HEAD
Convert djvu, gut, ocrml to toktei

## Usage

    $ java -jar pontos-0.1.0-standalone.jar [args]

=======
Convert DjVU, Project Gutenberg, or OCRML book inputs to a basic
`.rawtei.gz` common file format.  This step also includes any
`_meta.xml.bz2` metadata as a header rather than keeping it in a separate
file.

## Installation

To compile, run:

    $ lein bin

This should produce an executable `target/pontos-0.1.0-SNAPSHOT`.

## Usage

You run it as follows:

    $ cat <list of djvu or other input files> | pontos-0.1.0-SNAPSHOT [options]

or

    $ pontos-0.1.0-SNAPSHOT [options] <list of djvu or input files>.gz

This will produce files with the `.rawtei.gz` suffix.  By default,
they will be in the same directory as the `_djvu.xml.bz2` files, but
that can get messy.  You can therefore write them out to a parallel
directory structure using the `--input` and `--output` options.  The
`--input` option tells pontos how much of the file path prefix to delete
and `--output` provides a new prefix.
>>>>>>> origin/tokidx
