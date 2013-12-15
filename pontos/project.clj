(defproject ciir/pontos "0.1.0-SNAPSHOT"
  :description "Bridge to convert books to rawtei format."
  :jvm-opts ^:replace ["-Dfile.encoding=UTF-8" "-Xmx2g"]
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/data.xml "0.0.7"]
                 [org.clojure/data.zip "0.1.1"]
		 [ciir/utils "1.0.0-SNAPSHOT"]
                 ]
  :plugins [[lein-bin "0.3.4"]]
  :aot :all
  :main ciir.pontos)
