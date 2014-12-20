;;; This software is copyright 2013, 2014, 2015 by Marshall Abrams, and
;;; is distributed under the Gnu General Public License version 3.0 as
;;; specified in the file LICENSE.

;; Utility functions having to do with files
(ns utils.file)


(defn file-exists?
  [f]
  (.exists (clojure.java.io/as-file f)))

(defn make-dir
  [f]
  (.mkdir (java.io.File. f)))

(defn make-dir-if-none
  [f]
  (when-not (file-exists? f)
    (make-dir f)))

(defn unlocknload 
  "Given a symbol representing a namespace, converts the symbol
  into the corresponding path + clojure source fileanem, tries to 
  load the file, and then uses (\"unlocks\") the namespace."
  [nssym]
  (load-file 
    (str "src/" 
         (clojure.string/replace (clojure.string/replace nssym \. \/) \- \_) 
         ".clj"))
    (use nssym))
