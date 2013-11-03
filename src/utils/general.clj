(ns utils.general ; Utility functions handy for any Clojure program
  (:require [clojure.pprint :only [*print-right-margin*]]))

(defn pathify [s]
  (clojure.string/replace s \. \/))

(defn nsify [nms]
  (clojure.string/replace (str nms) \/ \.))

(defn unlocknload [path-or-ns]
  (load-file (str "src/" (pathify path-or-ns)))
  (use (nsify path-or-ns)))

(defn set-pprint-width 
  "Sets width for pretty-printing with pprint and pp."
  [cols] 
  (alter-var-root 
    #'clojure.pprint/*print-right-margin* 
    (constantly cols)))

(defmacro add-to-docstr
  "Appends string addlstr onto end of existing docstring for symbol sym.
  (Tip: Consider beginning addlstr with \"\\n  \".)"
  [sym addlstr] 
  `(alter-meta! #'~sym update-in [:doc] str ~addlstr))
