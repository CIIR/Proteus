(ns pontos.core
  (:require [clojure.string :as s]
            [clojure.java.io :as jio]
            [clojure.data.xml :refer [source-seq]]
            [ciir.utils :refer :all])
  (:import (java.io File BufferedInputStream InputStreamReader
		    BufferedReader PushbackReader)
           (java.util.zip GZIPInputStream GZIPOutputStream))
  (:gen-class))

(set! *warn-on-reflection* true)

(def lang-map {"English" "eng",
               "French" "fre", "Français" "fre", "fra" "fre",
               "German" "ger",
               "Latin" "lat", "Italian" "ita", "Spanish" "spa"})

;; Maybe we should just use some other unicode character for backslashes.
(def escapes {\< "&lt;", \> "&gt;", \& "&amp;", \" "&quot;", \' "&apos;"
              ;;\\ "backslashesareinescapable"}
              \\ "\u2216"		; set minus
              })

(defn dj-pages
  [events]
  (partition-when #(and (= (:name %) :MAP)
                          (= (:type %) :end-element))
                    events))

(defn dj-tags
  [s]
  (let [freqs (->> s
		   (filter #(= (:type %) :characters))
		   (map :str)
		   (frequencies))
	googles (or (freqs "Google") 0)]
    (if (>= googles 3)
      (let [page-start (first (filter #(and (= (:type %) :start-element)
					    (= (:name %) :OBJECT))
				      s))]
	(str "<pb n=\""
	     (nth (re-find #"_0*(\d+).djvu$" (:usemap (:attrs page-start))) 1)
	     "\" />"))
      (apply
       str
       (filter
	not-empty
	(map
	 #(case (:type %)
		:start-element
		(case (:name %)
		      :OBJECT (str "<pb n=\""
				   (nth (re-find #"_0*(\d+).djvu$" ((:attrs %) :usemap)) 1)
				   "\" />")
		      :PAGECOLUMN "<cb />"
		      :LINE "<lb />"
		      :PARAGRAPH "<p>"
		      :WORD (str "<w coords=\"" ((:attrs %) :coords) "\">")
		      nil)
		:end-element
		(case (:name %)
		      :LINE "\n"
		      :PARAGRAPH "</p>\n"
		      :WORD "</w> "
		      nil)
		:characters (s/escape (:str %) escapes))
	 s))))))

;; Before rearranging lines, e.g. headers and footers, we should
;; probably number the lines.
(defn proc-page
  "Process one page. Tag headers (and footers) and move them
  after (before) the next (previous) paragraph in sequence."
  [^String p]
  (-> p
      (s/replace #"[ ]+\n" "\n")
      (s/replace #"\n</p>" "</p>")
      ;; Match up to 51 lines in the next (previous paragraph).
      ;; Without these hard limits, the poor regex engine does a stack overflow.
      (s/replace
       #"^((?:<[^>]+>)*)<p>(.*)</p>\n(<p>(?:.*\n){0,50}?.*</p>)(\n<p>)"
       "$1$3\n<fw place=\"top\">$2</fw>$4")
      (s/replace
       #"(</p>\n)(<p>(?:.*\n){0,50}?.*</p>\n)<p>(.*)</p>\n*$"
       "$1<fw place=\"bottom\">$3</fw>\n$2")))

(defn splice-page
  "Given a triple of the previous, current, and next pages, figure out
  whether the first and last paragraphs of the current page extend
  beyond page boundaries."
  [[prev cur next]]
  (->> cur
       ;; I guess we should make a conditional replace macro
       (#(if (re-find #"[a-z][,:;\-]?</w></p>\n$" prev)
	   (s/replace % #"^((?:<[^>]+>)*)<p>((?:<[^>]+>)*<w [^>]+>[a-z])" "$1$2")
	   %))
       (#(if (re-find #"^(?:<[^>]+>)*<p>(?:<[^>]+>)*<w [^>]+>[a-z]" next)
	   (s/replace % #"([a-z][,:;\-]?</w>)</p>\n$" "$1\n")
	   %))))

(defn dj-paras
  "Convert a DjVuXML file into a lazy sequence of XML paragraphs with
  tagged words."
  [s]
  (->> s
      source-seq
      dj-pages
      (map dj-tags)
      (map proc-page)
      (lazy-cat [""])			; add leading empty page
      (partition 3 1 [""])		; and trailing empty page
      (map splice-page)))		; cat to string

(defn ocrml-sections
  [events]
  (partition-when #(and (= (:name %) :section)
                          (= (:type %) :end-element))
                    events))

(defn ocrml-tags
  [s]
  (let [label (:label (:attrs (first (filter #(and (= (:type %) :start-element) (= (:name %) :section)) s))))]
    (apply
     str
     (filter
      not-empty
      (map
       #(case (:type %)
	      :start-element
	      (let [attrs (:attrs %)]
		(case (:name %)
		      :section (case label
				     "SEC_HEADER" "<fw place=\"top\">"
				     "SEC_FOOTER" "<fw place=\"bottom\">"
				     "<p>")
		      :page (str "<pb n=\"" (:key attrs) "\" />")
		      :line "<lb />"
		      :word (str "<w coords=\"" (s/join "," (map (partial get attrs) [:l :t :w :h])) "\">" (s/escape (:val attrs) escapes))
		      :marker (str "<marker" (map-str (fn [x] (str " " (name (first x)) "=\"" (second x) "\"")) attrs) " />")
		      nil))
	      :end-element
	      (case (:name %)
		    :section (case label
				   "SEC_HEADER" "</fw>"
				   "SEC_FOOTER" "</fw>"
				   "</p>\n")
		    :line "\n"
		    :word "</w> "
		    :page "<pb />"
		    ;; :marker "</marker>"
		    nil)
	      :characters (s/escape (:str %) escapes))
       s)))))

(defn ocrml-page
  [s]
  (-> s
      (s/replace #"[ ]+\n" "\n")
      (s/replace #"\n</p>" "</p>")
      (s/replace #"\n</fw>" "</fw>")
      (s/replace #"(<fw [^>]+>.*</fw>)(<p>(?:.*\n){0,50}?.*</p>)(\n<p>)" "$2\n$1$3")
  ))

(defn ocrml-paras
  "Transform OCRML markup into a sequence of TEI paragraphs."
  [s]
  (->> s
       source-seq
       ocrml-sections
       (map-str ocrml-tags)
       (#(s/split % #"<pb />"))
       (lazy-seq)
       (map ocrml-page)
       (lazy-cat [""])			; add leading empty page
       (partition 3 1 [""])		; and trailing empty page
       (map splice-page)))		; cat to string

(defn gut-forms
  "Tag whitespace separated words in a Gutenberg file, ignoring
  underbars used for italics."
  [paridx text]
  (str
   "<p>"
   (-> text
       (s/escape escapes)
       (s/replace #"\$" "&dollar;")
       (partition-str #"[ \n_]+")
       (#(map-indexed (fn [idx w]
                        (if (re-find #"[^ \n_]" w)
                          (str "<w coords=\"0,0," paridx "," (/ idx 2) "\">" w "</w>")
                          w)) %))
       (#(apply str %))
       ;;(s/replace #" _" " <hi>")
       ;;(s/replace #"_ " "</hi> ")
       (s/replace #"&dollar;" "\\$"))
   "</p>\n"))

(defn gut-paras
  "Turn a Gutenberg file into a lazy sequence of XML paragraphs with
  tagged words."
  [s]
  (->> s
       line-seq
       (partition-by #(= % ""))
       (map #(s/join "\n" %))
       ;;(drop-while #(empty? (re-find #"START OF THE PROJECT GUTENBERG EBOOK" %)))
       ;;(drop 1)
       (drop-while #(re-find #"^\n" %))
       ;;(take-while #(empty? (re-find #"END OF THE PROJECT GUTENBERG EBOOK" %)))
       (map-indexed gut-forms)
       (map-indexed #(if (re-find #"^<p>\n+</p>$" %2) (str "<pb n=\"" %1 "\" />") %2))
       ))

(defn ^String dc-fix
  [dc-string]
  (-> dc-string
      (s/replace
       #"<language>([^<]+)</language>"
       #(str "<language>"
	     (if-let [lcode (lang-map (s/trim (% 1)))] lcode (% 1))
	     "</language>"))))

(defn raw-file
  [para-seq ^String mpath ^String ipath ^String opath]
  (println opath)
  (let [metadata (-> mpath bzreader slurp (s/replace #"^<\?xml [^>]+\?>\n*" "") dc-fix)
        encoding (if (re-find #"-8\." ipath) "ISO-8859-1" "UTF-8")
        file-read #(jio/reader (if (re-find #"\.bz2$" %) (bzreader %) %) :encoding encoding)]
    (with-open [in ^BufferedReader (file-read ipath)
                out (-> opath jio/input-stream GZIPOutputStream.
                        (jio/writer :encoding "UTF-8"))]
      (.write out "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TEI>\n")
      (.write out metadata)
      (.write out "<text>\n")
      (doseq [para (para-seq in)] (.write out para))
      (.write out "\n</text></TEI>\n")
      opath)))

(defn convert-file
  [fpath]
  (let [ipath (s/trim fpath)
        ifile (File. ipath)
        idir (.getParent ifile)
        bid (.getName (File. idir))
        mpath (jio/file idir (str bid "_meta.xml.bz2"))
        raw-path (.getPath (jio/file idir (str bid ".rawtei.gz")))
        [call opath]
        (cond
         (re-find #"gut$" bid)
         [(partial raw-file gut-paras mpath) raw-path]
         (re-find #"_ocrml.xml$" ipath)
         [(partial raw-file ocrml-paras mpath) raw-path]
         (re-find #"_djvu.xml.bz2" ipath)
         [(partial raw-file dj-paras mpath) raw-path])]
    (try
      (call ipath opath)
      (catch Exception e
        (println "# Error with " ifile ":" e)
        (if opath
          (let [ofile (File. opath)]
            (if (and (.exists ofile) (.canWrite ofile))
              (do (.delete opath)
                  ""))))))))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  ;; work around dangerous default behaviour in Clojure
  (alter-var-root #'*read-eval* (constantly false))
  (println "Hello, World!"))
