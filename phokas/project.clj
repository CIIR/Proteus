(defproject phokas "1.0.0-SNAPSHOT"
  :description "Process Internet Archive books"
  :jvm-opts ["-Dfile.encoding=UTF-8"]
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [org.clojure/data.xml "0.0.3"]
                 [org.clojure/data.zip "0.1.1"]
                 [org.clojure/tools.cli "0.2.1"]
                 [org.clojure/clojure-contrib "1.2.0"]
		 [ciir/utils "1.0.0-SNAPSHOT"]
                 [xom "1.2.5"]
		 [dparser "2011-01-18"]
		 [stanford-corenlp "2010-11-12"]
		 [ciir/nlp-models "2011-07-12"]]
  :main phokas.core)
