## Compiling

    mvn package

## Running the server

    java -jar target/homer-*.jar proteus server-conf.json

## Show me an example!

Inside of ``src/test/resources/toktei`` we have stashed copies of Romeo and Juliet (1920) and Macbeth (1897) in the TOKTEI format.

To build a page and book index of this, run:

    ./scripts/tiny-corpus/build.sh

If that doesn't work, compile first: ``mvn package``.

To run the Proteus demonstration server (in its current state) on these books, run:

    ./scripts/tiny-corpus/run.sh

