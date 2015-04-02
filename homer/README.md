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

## FAQ

### Why do I get some sort of URLException while trying to set up the database?

There appears to be some issues with hostname resolution with JDK7 on a Mac. Adding a workaround to your ``/etc/hosts`` seems to be the [solution](https://groups.google.com/forum/#!topic/h2-database/DuIlTLN5KOo). TL;DR - you need to explain to your computer that your hostname is the same as 127.0.0.1.


## Run examples of searching acm digital library articles

	Step 1: recompile the whole package if you haven't updated changes of ACMArticleParser.java in 2014/04/23
	
	mvn package
	
	Step 2: build an index 
	
	./example_acmdl/build.sh
	
	Step 3: run the server
	
	./example_acmdl/run.sh
	


