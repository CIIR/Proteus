(ns phokas.core
  ^{:author "David Smith"}
  (:require [clojure.string :as s]
	    [clojure.java.io :as jio]
	    [clojure.contrib.zip-filter :as zf]
	    [clojure.zip :as zip]
	    [clojure.xml :as xml])
  (:use [clojure.contrib.command-line]
	[clojure.contrib.lazy-xml :only (parse-seq parse-trim)]
	[clojure.contrib.zip-filter.xml :as zx]
	[ciir.utils])
  (:import (java.io File BufferedInputStream InputStreamReader
		    BufferedReader PushbackReader)
	   (java.util ArrayList Properties)
	   (java.util.zip GZIPInputStream GZIPOutputStream)
	   (edu.stanford.nlp.pipeline StanfordCoreNLP Annotation DefaultPaths)
	   (edu.stanford.nlp.util ArrayCoreMap)
	   (edu.stanford.nlp.ling CoreLabel
				  CoreAnnotations$TokensAnnotation
				  CoreAnnotations$SentencesAnnotation
				  CoreAnnotations$PartOfSpeechAnnotation
				  CoreAnnotations$NamedEntityTagAnnotation
				  CoreAnnotations$LemmaAnnotation
				  CoreAnnotations$TextAnnotation
				  CoreAnnotations$BeforeAnnotation
				  CoreAnnotations$AfterAnnotation
				  CoreAnnotations$CurrentAnnotation
				  CoreAnnotations$CharacterOffsetBeginAnnotation
				  CoreAnnotations$CharacterOffsetEndAnnotation))
  (:gen-class))

(defn gzresource
  [rname]
  (-> rname
      ClassLoader/getSystemResourceAsStream
      BufferedInputStream. GZIPInputStream. InputStreamReader. BufferedReader.))

(def *clobber?* true)

(def *language* "eng")

(def *languages* #{"eng" "fre" "ger" "ita" "lat"})

(def *lang-map* {"English" "eng",
                 "French" "fre", "Français" "fre", "fra" "fre",
                 "German" "ger",
		 "Latin" "lat", "Italian" "ita", "Spanish" "spa"})

(def *dict* (-> "dict/words.eng.gz"
                gzresource
		line-seq set))

(defn read-stopwords
  [s]
  (let [lines (-> s
		  java.io.InputStreamReader. java.io.BufferedReader. line-seq)]
    (reduce #(let [[lang w p] (s/split %2 #"\t")]
	       (assoc-in %1 [lang w] (Double/parseDouble p)))
	    {} lines)))

(def *stopwords* (-> "langid/stopwords.tab" ClassLoader/getSystemResourceAsStream read-stopwords))

(def *lang-annotators*)
(def *annotators*)

;; TODO: PTBLexer includes americanize in the ptb3Escaping
;; options. Annoyingly, it processes options in a sorted set order, so
;; we have to include all the ptb3Escaping options piecemeal without
;; americanize.

(def *ssplitter*
     (edu.stanford.nlp.pipeline.WordsToSentencesAnnotator. false))

(def *lemmatizer* (edu.stanford.nlp.pipeline.MorphaAnnotator. false))

(def *name-keepers* #{"PERSON" "LOCATION" "ORGANIZATION" "MISC" "MONEY"})

;; Maybe we should just use some other unicode character for backslashes.
(def *escapes* {\< "&lt;", \> "&gt;", \& "&amp;", \" "&quot;", \' "&apos;"
		;;\\ "backslashesareinescapable"}
		\\ "\u2216"		; set minus
		})

(defn dj-pages
  [events]
  (partition-when #(and (= (% :name) :MAP)
			(= (% :type) :end-element))
		  events))

(defn dj-tags
  [s]
  (let [freqs (->> s
		   (filter #(= (% :type) :characters))
		   (map #(% :str))
		   (frequencies))
	googles (or (freqs "Google") 0)]
    (if (>= googles 3)
      (let [page-start (first (filter #(and (= (:type %) :start-element)
					    (= (:name %) :OBJECT))
				      s))]
	(str "<pb n=\""
	     (nth (re-find #"_0*(\d+).djvu$" ((page-start :attrs) :usemap)) 1)
	     "\" />"))
      (apply
       str
       (filter
	not-empty
	(map
	 #(case (% :type)
		:start-element
		(case (% :name)
		      :OBJECT (str "<pb n=\""
				   (nth (re-find #"_0*(\d+).djvu$" ((% :attrs) :usemap)) 1)
				   "\" />")
		      :PAGECOLUMN "<cb />"
		      :LINE "<lb />"
		      :PARAGRAPH "<p>"
		      :WORD (str "<w coords=\"" ((% :attrs) :coords) "\">")
		      nil)
		:end-element
		(case (% :name)
		      :LINE "\n"
		      :PARAGRAPH "</p>\n"
		      :WORD "</w> "
		      nil)
		:characters (s/escape (% :str) *escapes*))
	 s))))))

(defn no-tags
  [^String s]
  (-> s
      (s/replace #"</?[A-Za-z][^>]*>" "")
      (s/replace #"\"" "&quot;")))

(defn fix-word
  [g]
  (str "<w form=\"" (no-tags (nth g 2)) "\" " (nth g 1) (nth g 2) "</w>"))

(defn trim-punc
  [^String s]
  (s/replace s #"[\.,:;!?'\"]+$" ""))

(defn dehyph
  [a b]
  (let [hyph (str a "-" b)
	nohyph (str a b)]
    (cond (*dict* (trim-punc hyph)) hyph
	  (*dict* (trim-punc nohyph)) nohyph
	  (*dict* (s/lower-case (trim-punc nohyph))) nohyph
	  true hyph)))

(defn word-forms
  "Reassemble hyphenated words across lines and pages. Put normalized
  form the in the form attribute, but keep the original in tag
  content."
  [^String para]
  (-> para
      ;; Seriously!??? A dollar sign $6 in the input gets resolved as
      ;; a backref by Java's regexes?!!!
      (s/replace #"\$" "&dollar;")
      (s/replace #"<w ([^>]+>)([ ]*)</w>" "$2") ; remove empty words
      (s/replace #"<w ([^>]+>)([^<]+)</w>" fix-word)
      (s/replace
       #"<w form=\"([^\"]*[A-Za-z])-\" coords=\"([^\"]+)\"([^>]*>[^<]+)</w>(\n?(?:<(?:lb|pb)[^>]+>)*?)<w form=\"([^\"]+)\" coords=\"([^\"]+)\"([^>]*)>"
       ;; NB: we merge form and coords, but keep extra attributes from
       ;; both tags; could be overkill
       #(str "<w form=\"" (dehyph (% 1) (% 5)) "\" coords=\"" (% 2) ";" (% 6) "\"" (% 7) (% 3) (% 4)))
      (s/replace #"&dollar;" "\\$")))

(defn- ssplit-body
  [para ann]
  (let [[pref & wspans] (s/split para #"<w ")]
    (if (empty? wspans) para
	(loop [res pref
	       scounts (map #(count (.get % CoreAnnotations$TokensAnnotation))
			    (.get ann CoreAnnotations$SentencesAnnotation))
	       spans wspans]
	  (if (and scounts spans)
	    (recur (apply str res "<s>"
			  (map-str #(str "<w " %) (take (first scounts) spans))
			  "</s>")
		   (next scounts)
		   (drop (first scounts) spans))
	    res)))))

;; TODO: fix bug when tokenizer reduces the number of tokens:
;; e.g., . . . -> ...
;; This is tolerable at present since all the tokens we want show up
;; in form attributes.
(defn tokenize-para
  [^String para]
  (let [[pref ptag body] (partition-str para #"<p(?: [^>]*)?>")]
    (if (empty? body) (str para (when (re-find #"<p(?: [^>]*)?>$" para) "</p>"))
	(let [wsegs (s/split body #"</w>")
	      wspans (map #(re-find #"^((?:.|\n)*)<w form=\"([^\"]+)\" coords=\"([^\"]+)\"((?:.|\n)+)$" %)
			  wsegs)
	      words (map #(nth % 2) wspans)
	      ann (Annotation. (s/join " " words))]
	  (.annotate (*annotators* :tokenizer) ann)
	  (.annotate *ssplitter* ann)
	  (->>
	   (.get ann CoreAnnotations$TokensAnnotation)
	   (partition-all 2 1)
	   (partition-when #(or (< (count %) 2)
				(< (.endPosition (nth % 0))
				   (- (.beginPosition (nth % 1))
				      ;; Account for untokenizable characters (other than space)
				      (count (s/trim (.before (nth % 1))))))))
	   (map (fn [x] (map #(.word (nth % 0)) x)))
	   (map (fn [o n] (apply str (nth o 1) "<w form=\"" (first n) "\""
				 " coords=\"" (nth o 3) "\"" (nth o 4) "</w>"
				 (map-indexed #(str "<w form=\"" %2 "\""
						    " coords=\"" (nth o 3) "+" (inc %1) "\"></w>")
					      (rest n))))
		wspans)
	   (#(str pref ptag (ssplit-body (apply str %) ann) "</p>")))))))

;;; Some ugly private functions to interface with Stanford Core NLP
;;; Java annotation objections.

(defn- make-core-labels
  [x]
  (doto (Annotation. (s/join " " x))
    (.set CoreAnnotations$TokensAnnotation
	  (ArrayList. (map #(doto (CoreLabel.) (.setWord %)) x)))))

(defn- make-annotated-sentences
  [x]
  (loop [res (ArrayList.)
	 sens x]
    (if sens
      (recur (doto res (.add (doto (Annotation. (s/join " " (first sens)))
			       (.set CoreAnnotations$TokensAnnotation
				     (.get
				      (make-core-labels (first sens))
				      CoreAnnotations$TokensAnnotation)))))
	     (next sens))
      (doto (Annotation. (s/join " " (flatten x)))
	(.set CoreAnnotations$SentencesAnnotation res)
	(.set CoreAnnotations$TokensAnnotation
	      (ArrayList.
	       (mapcat #(vec (.get % CoreAnnotations$TokensAnnotation)) res)))))))

(defn- make-tree-labels
  [sen]
  (let [a (map #(:attrs %) (xml-> sen :w zip/node))
	stext (s/join " " (map #(% :form) a))
	toks
	(ArrayList. [(doto (Annotation. stext)
		       (.set CoreAnnotations$TokensAnnotation
			     (map #(doto (CoreLabel.)
				     (.setDocID (% :coords))
				     (.setWord (% :form))
				     (.setTag (% :type))
				     (.setLemma (% :lemma)))
				  a)))])]
    (doto (Annotation. stext)
      (.set CoreAnnotations$SentencesAnnotation toks)
      (.set
       CoreAnnotations$TokensAnnotation
       (ArrayList. (mapcat #(vec (.get % CoreAnnotations$TokensAnnotation)) toks))))))

(defn get-phrase
  [id words kids root?]
  (let [cur (words id)
	form (cur :form)
	k (kids id)]
    ;;(when (not= (:deprel cur) "P")	; cut recursion at punctuation?
    (when (or root? ((*annotators* :name-deps) (:deprel cur)))
      (filter not-empty
	      (flatten
	       (vector (map #(get-phrase % words kids false) (filter #(< % id) k))
		       cur
		       (map #(get-phrase % words kids false) (filter #(> % id) k))))))))

(defn ner-sen
  [sen]
  (let [words (->> (xml-> sen :w zip/node)
		   (map :attrs)
		   (map-indexed #(assoc %2
				   :head (-> %2 :head Integer/parseInt dec)
				   :id %1))
		   vec)
	;; Use reverse so that kids get conj'd onto the front in correct order
	kids (reduce #(assoc %1 (:head %2) (conj (%1 (:head %2)) (:id %2))) {} (reverse words))
	props (filter #(and ((*annotators* :name-pos) (:type %))
			    (re-find #"^[A-Z][a-z]*\.?$" (:form %))
			    (not ((*annotators* :name-deps) (:deprel %)))) words) ; already non-recursive name?
	names (map #(get-phrase (:id %) words kids true) props)]
    (map-str
     #(str "<rs type=\"MISC\""
	   " name=\"" (s/escape (s/join " " (map :form %)) *escapes*) "\""
	   " coords=\"" (s/join "|" (map :coords %)) "\"></rs>")
     names)))

(defn ner-para
  [para]
  (let [tags (->
	      ;; Wrap in dummy tags to allow stuff before or after main <p>.
	      (str "<text>" para "</text>")
	      java.io.StringReader.
	      parse-trim
	      zip/xml-zip
	      (xml-> :p :s)
	      (#(map ner-sen %)))]
    (if (= "" (s/join tags))
      para
      (s/join "</s>" (map str (s/split para #"</s>") (concat tags [""]))))))

(defn xmlck-para
  [para]
  (let [tags (->
	      (str "<text>" para "</text>")
	      java.io.StringReader.
	      parse-trim
	      zip/xml-zip
	      (xml-> :p :s))]
    para))

(defn dc-fix
  [dc-string]
  (-> dc-string
      (s/replace
       #"<language>([^<]+)</language>"
       #(str "<language>"
	     (if-let [lcode (*lang-map* (s/trim (% 1)))] lcode (% 1))
	     "</language>"))))

(defn dc-language
  [dc-string]
  (let [mt (zip/xml-zip (parse-trim (java.io.StringReader. dc-string)))]
    (xml1-> mt :language text)))

(defn ^String local-language
  [otag]
  (when-let [m (re-find #" lang=\"([^\"]+)\"" (str otag))]
    (nth m 1)))

(defn- sentence-tokens
  [^String para]
  (->> (s/split para #"</s>")
       (map #(re-seq #"<w form=\"([^\"]+)\"" %))
       (map #(map (fn [x] (nth x 1)) %))
       (filter not-empty)))

(defn- sentence-tagged-tokens
  [^String para]
  (->> (s/split para #"</s>")
       (map #(re-seq #"<w type=\"([^\"]+)\" lemma=\"([^\"]+)\" form=\"([^\"]+)\"" %))
       (map #(map (fn [x] (vector (nth x 3) (nth x 2) (nth x 1))) %))
       (filter not-empty)))

(defn- make-input-dtree
  [sen]
  (dependency.DTree. (into-array (map first sen)) (into-array (map #(nth % 2) sen))))

(defn tag-line
  [line]
  (let [s (re-seq #"([^ ]+)/([^/ ]+)" line)]
    (map #(vector (second %) "" (nth % 2)) s)))

(defn time-parser
  [line]
  (let [start (System/nanoTime)
	tree (.parse (*annotators* :parser) (make-input-dtree (tag-line line)))
	heads (vec (.heads tree))
	stop (System/nanoTime)
	msecs (/ (- stop start) 100000.0)]
    (list msecs (/ msecs (count heads)) heads)))

(defn time-sens
  [infile outfile]
  (with-open [in (gzreader infile)
	      out (jio/writer outfile)]
    (with-bindings {#'*out* out}
      (doseq [sen (line-seq in)]
	(println (s/join "\t" (time-parser sen)))))))

(defn tag-para
  [^String para]
  (let [[pref ptag body] (partition-str para #"<p(?: [^>]*)?>")]
    (if (empty? body) para
	(let [wsegs (s/split body #"</w>")
	      wspans (map #(re-find #"^((?:.|\n)*)<w form=\"([^\"]+)(\"(?:.|\n)+)$" %) wsegs)
	      ann (->> body sentence-tokens
		       (map #(map (fn [s] (s/replace s #"^&amp;$" "&")) %)) ; PTB expects bare amp
		       make-annotated-sentences)]
	  (.annotate (*annotators* :tagger) ann)
	  (.annotate *lemmatizer* ann)
	  (str pref ptag
	       (apply str		; because map-str doesn't work on multiple colls
		      (map (fn [orig tok]
			     (str (nth orig 1)
				  "<w"
				  " type=\"" (.tag tok) "\""
				  " lemma=\"" (s/replace  (.lemma tok) #"^&$" "&amp;") "\""
				  " form=\"" (nth orig 2)
				  (nth orig 3) "</w>"))
			   wspans (.get ann CoreAnnotations$TokensAnnotation)))
	       (last wsegs))))))

(defn lemmatize-word
  [w]
  (if (re-find #"[A-Za-z]" w)
    (let [lw (s/lower-case w)]
      (get (*annotators* :lem-dict) lw lw))
    w))

(defn lemmatize-para
  [^String para]
  (let [[pref ptag body] (partition-str para #"<p(?: [^>]*)?>")]
    (if (empty? body) para
	(let [wsegs (s/split body #"</w>")
	      wspans (map #(re-find #"^((?:.|\n)*)<w form=\"([^\"]+)(\"(?:.|\n)+)$" %) wsegs)
	      toks (filter not-empty (map #(nth % 2) wspans))
	      lems (map lemmatize-word toks)]
	  (str pref ptag
	       (apply str
		      (map (fn [orig lemma]
			     (str (nth orig 1)
				  "<w"
				  " lemma=\"" lemma "\""
				  " form=\"" (nth orig 2)
				  (nth orig 3) "</w>"))
			   wspans lems))
	       (last wsegs))))))

;; (defn lemmatize-para
;;   [^String para]
;;   (let [[pref ptag body] (partition-str para #"<p(?: [^>]*)?>")]
;;     (if (empty? body) para
;; 	(let [wsegs (s/split body #"</w>")
;; 	      wspans (map #(re-find #"^((?:.|\n)*)<w ((?:.|\n)+)$" %) wsegs)
;; 	      sens (apply concat (sentence-tagged-tokens body))
;;               lems (map #(lemmatize-word (first %) (nth % 2)) sens)]
;; 	  (str pref ptag
;;                (apply str
;;                       (map (fn [orig lemma]
;;                              (str (second orig)
;;                                   "<w "
;;                                   (s/replace (nth orig 2)
;;                                              #" lemma=\"[^\"]+\" "
;;                                              (str " lemma=\""
;;                                                   (s/escape lemma *escapes*)
;;                                                   "\" "))
;;                                   "</w>"))
;;                            wspans lems))
;; 	       (last wsegs))))))

(defn parse-para
  [^String para]
  (let [[pref ptag body] (partition-str para #"<p(?: [^>]*)?>")]
    (if (empty? body) para
	(let [wsegs (s/split body #"</w>")
	      wspans (map #(re-find #"^((?:.|\n)*)<w ((?:.|\n)+)$" %) wsegs)
	      sens (sentence-tagged-tokens body)
	      trees (map #(.parse (*annotators* :parser) (make-input-dtree %)) sens)
	      heads (flatten (map #(vec (.heads %)) trees))
	      deprels (flatten (map #(vec (.deprels %)) trees))]
	  (str pref ptag
	       (apply str		; because map-str doesn't work on multiple colls
		      (map (fn [orig head deprel]
			     (str (nth orig 1)
				  "<w"
				  " head=\"" head "\""
				  " deprel=\"" (s/escape deprel *escapes*) "\" "
				  (nth orig 2) "</w>"))
			   wspans heads deprels))
	       (last wsegs))))))

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

(defn dj-words
  [s]
  (->> s
       parse-seq
       (filter #(= (% :type) :characters))
       (map #(% :str))
       (frequencies)))

(defn dj-paras
  "Convert a DjVuXML file into a lazy sequence of XML paragraphs with
  tagged words."
  [s]
  (->> s
      parse-seq
      dj-pages
      (map dj-tags)
      (map proc-page)
      (lazy-cat [""])			; add leading empty page
      (partition 3 1 [""])		; and trailing empty page
      (map-str splice-page)		; cat to string
      (#(s/split % #"</p>\n"))
      (lazy-seq)
      (map word-forms)))

(defn ocrml-words
  [s]
  (->> s
       parse-seq
       (filter #(and (= (:name %) :word)
		     (= (:type %) :start-element)))
       (map #(:val (:attrs %)))
       (frequencies)))

(defn ocrml-sections
  [events]
  (partition-when #(and (= (% :name) :section)
			(= (% :type) :end-element))
		  events))

(defn ocrml-tags
  [s]
  (let [label (:label (:attrs (first (filter #(and (= (:type %) :start-element) (= (:name %) :section)) s))))]
    (apply
     str
     (filter
      not-empty
      (map
       #(case (% :type)
	      :start-element
	      (let [attrs (:attrs %)]
		(case (% :name)
		      :section (case label
				     "SEC_HEADER" "<fw place=\"top\">"
				     "SEC_FOOTER" "<fw place=\"bottom\">"
				     "<p>")
		      :page (str "<pb n=\"" (:key attrs) "\" />")
		      :line "<lb />"
		      :word (str "<w coords=\"" (s/join "," (map (partial get attrs) [:l :t :w :h])) "\">" (s/escape (:val attrs) *escapes*))
		      :marker (str "<marker" (map-str (fn [x] (str " " (name (first x)) "=\"" (second x) "\"")) attrs) " />")
		      nil))
	      :end-element
	      (case (% :name)
		    :section (case label
				   "SEC_HEADER" "</fw>"
				   "SEC_FOOTER" "</fw>"
				   "</p>\n")
		    :line "\n"
		    :word "</w> "
		    :page "<pb />"
		    ;; :marker "</marker>"
		    nil)
	      :characters (s/escape (% :str) *escapes*))
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
       parse-seq
       ocrml-sections
       (map-str ocrml-tags)
       (#(s/split % #"<pb />"))
       (lazy-seq)
       (map ocrml-page)
       (lazy-cat [""])			; add leading empty page
       (partition 3 1 [""])		; and trailing empty page
       (map-str splice-page)		; cat to string
       (#(s/split % #"</p>\n"))
       (lazy-seq)
       (map word-forms)))

(defn gut-forms
  "Tag whitespace separated words in a Gutenberg file, ignoring
  underbars used for italics."
  [paridx text]
  (str "<p>"
       (-> text
	   (s/escape *escapes*)
	   (s/replace #"\$" "&dollar;")
	   (partition-str #"[ \n_]+")
	   (#(map-indexed (fn [idx w]
			    (if (re-find #"[^ \n_]" w)
			      (str "<w form=\"" w "\" coords=\"0,0," paridx "," (/ idx 2) "\">" w "</w>")
			      w)) %))
	   (#(apply str %))
	   ;;(s/replace #" _" " <hi>")
	   ;;(s/replace #"_ " "</hi> ")
	   (s/replace #"&dollar;" "\\$"))))

(defn gut-paras
  "Turn a Gutenberg file into a lazy sequence of XML paragraphs with
  tagged words."
  [s]
  (->> s
       line-seq
       (partition-when #(= % ""))
       (map #(s/join "\n" %))
       ;;(drop-while #(empty? (re-find #"START OF THE PROJECT GUTENBERG EBOOK" %)))
       ;;(drop 1)
       (drop-while #(= "" %))
       ;;(take-while #(empty? (re-find #"END OF THE PROJECT GUTENBERG EBOOK" %)))
       (map-indexed gut-forms)))

(defn gut-words
  [s]
  (->> s
       gut-paras
       (map #(map trim-punc (map second (re-seq #"<w [^>]+>([^<]+)</w>" %))))
       (flatten)
       (frequencies)))

(defn annotate-para
  "Perform linguistic annotation on a paragraph."
  [para]
  (let [lang (or (local-language (re-find #"<p(?: [^>]+)?>" para)) *language*)
	ann (*lang-annotators* lang)]
    (with-bindings {#'*language* lang
		    #'*annotators* ann}
      ((*annotators* :annotate) para))))

(defn top-lemma
  [[form rec]]
  (vector
   form
   (->> rec
	vals
	(apply merge-with +)
	(apply max-key second)
	first)))
       

(defn init-annotators
  [lang]
  (condp = lang
      "eng" {:annotate
	     #(-> % tokenize-para tag-para parse-para ner-para)
	     :tokenizer
	     (edu.stanford.nlp.pipeline.PTBTokenizerAnnotator.
	      false "invertible,americanize=false,normalizeAmpersandEntity=false,ptb3Escaping=true,untokenizable=noneDelete")
	     :tagger
	     (edu.stanford.nlp.pipeline.POSTaggerAnnotator.
	      DefaultPaths/DEFAULT_POS_MODEL false 500)
	     :parser
	     (parser.Parser/pretrained)
	     :name-deps #{"NAME" "TITLE"}
	     :name-pos #{"NNP" "NNPS"}}
      "fre" {:annotate
	     #(-> % tokenize-para
		  (s/replace #"-LRB-" "(")
		  (s/replace #"-RRB-" ")")
		  tag-para parse-para ner-para)
	     :tokenizer
	     (edu.stanford.nlp.pipeline.PTBTokenizerAnnotator.
	      false "invertible,americanize=false,normalizeAmpersandEntity=false,ptb3Escaping=true,untokenizable=noneDelete")
	     :tagger
	     (edu.stanford.nlp.pipeline.POSTaggerAnnotator.
	      "ciir/models/fre.pos" false 500)
	     :parser
	     (parser.Parser/read "/home2/dasmith/src/iabooks/fre.dep")
	     ;; (parser.Parser/read (.toString (ClassLoader/getSystemResource "ciir/models/fre.dep")))
	     :name-deps #{"mod"}
	     :name-pos #{"NPP"}}
      "ger" {:annotate
	     #(-> % tokenize-para
		  (s/replace #"-([LR])RB-" "*$1RB*")
		  tag-para parse-para ner-para)
	     :tokenizer
	     (edu.stanford.nlp.pipeline.PTBTokenizerAnnotator.
	      false "invertible,americanize=false,normalizeAmpersandEntity=false,ptb3Escaping=true,untokenizable=noneDelete")
	     :tagger
	     (edu.stanford.nlp.pipeline.POSTaggerAnnotator.
	      "ciir/models/ger.pos" false 500)
	     :parser
	     (parser.Parser/read "/home2/dasmith/src/iabooks/ger.dep")
	     :name-deps #{"PNC"}
	     :name-pos #{"NE"}}
      "ita" {:annotate
	     #(-> % tokenize-para
		  (s/replace #"-LRB-" "(")
		  (s/replace #"-RRB-" ")")
		  tag-para parse-para ner-para)
	     :tokenizer
	     (edu.stanford.nlp.pipeline.PTBTokenizerAnnotator.
	      false "invertible,americanize=false,normalizeAmpersandEntity=false,ptb3Escaping=true,untokenizable=noneDelete")
	     :tagger
	     (edu.stanford.nlp.pipeline.POSTaggerAnnotator.
	      "ciir/models/ita.pos" false 500)
	     :parser
	     (parser.Parser/read "/home2/dasmith/src/iabooks/ita.dep")
	     :name-deps #{"mod"}
	     :name-pos #{"SP"}}
      "lat" {:annotate
	     #(-> % tokenize-para
		  (s/replace #"-LRB-" "(")
		  (s/replace #"-RRB-" ")")
		  lemmatize-para)
	     :tokenizer
	     (edu.stanford.nlp.pipeline.PTBTokenizerAnnotator.
	      false "invertible,americanize=false,normalizeAmpersandEntity=false,ptb3Escaping=true,untokenizable=noneDelete")
	     ;; :tagger
	     ;; (edu.stanford.nlp.pipeline.POSTaggerAnnotator.
	     ;;  "ciir/models/lat.pos" false 500)
             :lem-dict
	     (into {}
		   (->> "dict/lat-lems.clj.gz" gzresource PushbackReader. read
			(map top-lemma)))
	     ;; :parser
	     ;; (parser.Parser/read "/home2/dasmith/src/iabooks/lat.dep")
	     :name-deps #{"ATR"}
             :name-pos (set (for [n ["s" "p"]
                                  g ["m" "f" "n"]
                                  c ["-" "n" "g" "d" "a" "b" "v" "l"]]
                              (str "n-" n g c)))}
      {:annotate
       #(tokenize-para %)
       :tokenizer
       (edu.stanford.nlp.pipeline.PTBTokenizerAnnotator.
	false "invertible,americanize=false,normalizeAmpersandEntity=false,ptb3Escaping=true,untokenizable=noneDelete")}))

(defn stopword-langid
  [counts]
  (let [toks (reduce + (vals counts))
	props
	(into
	 {}
	 (for [[lang stops] *stopwords*]
	   [lang
	    (/ (/ (reduce + 0.0 (map #(get counts % 0) (keys stops)))
		  (reduce + 0.0 (vals stops)))
	       toks)]))]
    (assoc props "unk" (- 1 (reduce + (vals props))))))

(defn ocrml-convert-file
  "Convert the given book transcription to the Megabooks project's TEI
  XML.  Inline the metadata from the _meta.xml.bz2 file in the same
  directory.  Put the output in the source directory with the
  extension _mbtei.xml.gz.

  Try to detect if it's a Project Gutenberg book and perform the
  initial stages of conversion differently -- e.g. by removing
  Gutenberg header and footer.

  Return the path to the output file.  If an exception is encountered,
  remove the output file and return the empty string."

  [ipath]
  (let [ifile (File. ipath)
	idir (.getParent ifile)
	bid (second (re-find #"^(.*)_ocrml.xml$" (.getName ifile)))
	ofile (jio/file idir (str bid "_mbtei.xml.gz"))
	mfile (jio/file idir (str bid "_meta.xml.bz2"))
	opath (.getPath ofile)]
    (println opath)
    (if (and (not *clobber?*) (.exists ofile))
      ""
      (try
	(let [metadata (-> mfile bzreader slurp (s/replace #"^<\?xml [^>]+\?>\n*" "") dc-fix)
	      file-read jio/reader
	      para-seq ocrml-paras
	      raw-counts (with-open [in (file-read ipath)] (ocrml-words in))
	      lang *language*]
	  (with-bindings {#'*lang-annotators* {lang (init-annotators lang)}
			  #'*dict* (clojure.set/union
				    *dict*
				    (set (filter #(re-find #"^[A-Za-z]+$" %) (keys raw-counts))))}
	    (with-open [in (file-read ipath)
			out (-> ofile java.io.FileOutputStream. GZIPOutputStream. jio/writer)]
	      (.write out "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TEI>\n")
	      (.write out metadata)
	      (.write out (str "<text lang=\"" *language* "\">\n"))
	      (doseq [para (para-seq in)] (.write out (annotate-para para)))
	      (.write out "\n</text></TEI>\n")
	      opath)))
	(catch Exception e
	  (println "# Error with " ifile ":" e)
	  (if (and (.exists ofile) (.canWrite ofile))
	    (do (.delete ofile)
		"")))))))

(defn dj-convert-file
  "Convert the given book transcription to the Megabooks project's TEI
  XML.  Inline the metadata from the _meta.xml.bz2 file in the same
  directory.  Put the output in the source directory with the
  extension _mbtei.xml.gz.

  Try to detect if it's a Project Gutenberg book and perform the
  initial stages of conversion differently -- e.g. by removing
  Gutenberg header and footer.

  Return the path to the output file.  If an exception is encountered,
  remove the output file and return the empty string."

  [ipath]
  (let [ifile (File. ipath)
	idir (.getParent ifile)
	bid (.getName (File. idir))
	ofile (jio/file idir (str bid "_mbtei.xml.gz"))
	mfile (jio/file idir (str bid "_meta.xml.bz2"))
	opath (.getPath ofile)]
    (println opath)
    (if (and (not *clobber?*) (.exists ofile))
      ""
      (try
	(let [metadata (-> mfile bzreader slurp (s/replace #"^<\?xml [^>]+\?>\n*" "") dc-fix)
	      encoding (if (re-find #"-8\." (.getName ifile)) "ISO-8859-1" "UTF-8")
	      file-read #(jio/reader (if (re-find #"\.bz2$" %) (bzreader %) %) :encoding encoding)
	      para-seq (if (re-find #"gut$" bid) gut-paras dj-paras)
	      raw-counts (with-open [in (file-read ipath)]
	      		   (if (re-find #"gut$" bid)
	      		     (gut-words in)
	      		     (dj-words in)))
	      langs (sort-by second > (stopword-langid raw-counts))
	      top-lang (first langs)
	      meta-lang (dc-language metadata)
	      lang (if (> (second top-lang) 0.5)
	       	     (first top-lang)
	       	     "unk")]
	  (println "# meta-lang:" meta-lang)
	  (println "# sw-lang:" langs)
	  (if (and (not-empty *languages*) (not (*languages* lang)))
	    (do (println "# unprocessed language: " lang) "")
	    (with-bindings {#'*language* lang
			    #'*lang-annotators* {lang (init-annotators lang)}
			    #'*dict* (clojure.set/union
				      *dict*
				      (set (filter #(re-find #"^[A-Za-z]+$" %) (keys raw-counts))))}
	      (with-open [in (file-read ipath)
			  out (-> ofile java.io.FileOutputStream. GZIPOutputStream. (jio/writer :encoding "UTF-8"))]
		(.write out "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<TEI>\n")
		(.write out metadata)
		(.write out (str "<text lang=\"" *language* "\">\n"))
		(doseq [para (para-seq in)] (.write out (annotate-para para)))
		(.write out "\n</text></TEI>\n")
		opath))))
	(catch Exception e
	  (println "# Error with " ifile ":" e)
	  (if (and (.exists ofile) (.canWrite ofile))
	    (do (.delete ofile)
		"")))))))

(defn dj-convert-file-list
  [fname-seq]
  (doseq [fname fname-seq]
    (let [tname (s/trim fname)]
      (if (re-find #"_ocrml.xml$" tname)
	(ocrml-convert-file tname)
	(dj-convert-file tname)))))

(defn -main [& args]
  (with-command-line args
    "IA book converter"
    [[clobber? c? "Clobber existing files" false]
     remaining]
    (binding [*clobber?* clobber?]
      (if (empty? remaining) (dj-convert-file-list (-> System/in java.io.InputStreamReader. java.io.BufferedReader. line-seq))
          (doseq [ff remaining]
            (with-open [in (gzreader ff)]
              (dj-convert-file-list (line-seq in))))))))
