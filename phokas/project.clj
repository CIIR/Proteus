(defproject ciir/phokas "1.0.0-SNAPSHOT"
  :description "Process Internet Archive books"
  :jvm-opts ^:replace ["-Dfile.encoding=UTF-8" "-Xmx4g"]
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/data.xml "0.0.7"]
                 [org.clojure/data.zip "0.1.1"]
		 [ciir/utils "1.0.0-SNAPSHOT"]
                 [enlive "1.0.0"]
                 [edu.stanford.nlp/stanford-corenlp "3.3.0"]
                 [edu.stanford.nlp/stanford-corenlp "3.3.0" :classifier "models"]
		 [dparser "2011-01-18"]]
  :plugins [[lein-bin "0.3.4"]]
  :aot :all
  :main ciir.phokas)
