(defproject phokas "1.0.0-SNAPSHOT"
  :description "Process Internet Archive books"
  :jvm-opts ["-Dfile.encoding=UTF-8" "-Xmx4g"]
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [org.clojure/data.xml "0.0.3"]
                 [org.clojure/data.zip "0.1.1"]
                 [org.clojure/tools.cli "0.2.1"]
		 [ciir/utils "1.0.0-SNAPSHOT"]
                 [enlive "1.0.0"]
                 [edu.stanford.nlp/stanford-corenlp "1.3.3"]
                 [edu.stanford.nlp/stanford-corenlp "1.3.3" :classifier "models"]
		 [dparser "2011-01-18"]]
  :main phokas.core)
