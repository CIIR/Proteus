Project overview:
===

Morpheus - New front-end web service using the Scalatra lightweight router, and Scalate for templating.
Aura - New proteus service using finagle/thrift
Chronos - Code for temporal language modeling
Galago For Proteus - A roughly version 3.3 Galago with the components (now extracted to ../homer) mixed in.

Morpheus and Aura:
These two subprojects compose a single SBT subproject.  All dependencies and settings for each are
contained in the root project directory, in ProteusBuild.scala (if you'd like to "t" search it).
To compile the thrift code:
from root directory enter 
$ sbt
>> project aura
>> scrooge-gen
>> run <configuration file in json>
To start webapp:
>> project morpheus
>> container:start


DEPENDENCIES
====
- MongoDB (for persistence storage)
- Thrift 0.9.0, Finagle (for RPCs)
- Scala 2.9.2 (Updating to 2.10 is infeasible due to scala library collisions).

GOTCHAS
====
- Unfortunately, configuration for the servers is spread over several scopes.
  This is particularly true for morpheus, which must itself serve data, and speak
  to at least two other data sources (an Aura installation and a persistence layer):

  To configure what port the web server itself serves from, see morpheus/build.sbt.
  To configure what hosts/ports the ProteusServlet inside morpheus listens to, look
  at morpheus/src/main/scala/Scalatra.scala.

