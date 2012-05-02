(defproject phokas "1.0.0-SNAPSHOT"
  :description "Process Internet Archive books"
  :jvm-opts ["-Xmx2g"]
  :shell-wrapper {:bin "bin/phokas"}
  :dependencies [[org.clojure/clojure "1.2.1"]
                 [org.clojure/clojure-contrib "1.2.0"]
		 [ciir/utils "1.0.0-SNAPSHOT"]
                 [jgraph "5.13.0.0"]
                 [jgrapht "0.7.3"]
                 [xom "1.2.5"]
		 [dparser "2011-01-18"]
		 [stanford-corenlp "2010-11-12"]
		 [ciir/nlp-models "2011-07-12"]]
  :main phokas.core)
