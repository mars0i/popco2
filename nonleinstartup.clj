(.deleteOnExit (java.io.File. "/private/var/folders/68/d0l7z7p906l07fj6s7j5_ygm0000gq/T/form-init8428349931472014736.clj")) (do (set! *warn-on-reflection* nil) nil (clojure.core/binding [clojure.core/*command-line-args* (quote ("-n" "sims.bali.threegroups2pundits" "-r" "(rp/write-propn-activns-csv-for-R (take 5000 (map rp/ticker (mn/many-times sim/popn))))"))] (clojure.core/let [[ns-flag__10038__auto__ data__10039__auto__] (try (clojure.core/require (quote popco.core.popco)) (clojure.core/let [v__10040__auto__ (clojure.core/resolve (quote popco.core.popco/-main))] (if (clojure.core/ifn? v__10040__auto__) [:var v__10040__auto__] [:not-found])) (catch java.io.FileNotFoundException e__10041__auto__ [:threw e__10041__auto__])) class__10042__auto__ (clojure.core/when-not (clojure.core/= :var ns-flag__10038__auto__) (try (java.lang.Class/forName "popco.core.popco") (catch java.lang.ClassNotFoundException ___10043__auto__)))] (clojure.core/cond (clojure.core/= :var ns-flag__10038__auto__) (data__10039__auto__ "-n" "sims.bali.threegroups2pundits" "-r" "(rp/write-propn-activns-csv-for-R (take 5000 (map rp/ticker (mn/many-times sim/popn))))") class__10042__auto__ (clojure.lang.Reflector/invokeStaticMethod class__10042__auto__ "main" (clojure.core/into-array [(clojure.core/into-array java.lang.String (quote ("-n" "sims.bali.threegroups2pundits" "-r" "(rp/write-propn-activns-csv-for-R (take 5000 (map rp/ticker (mn/many-times sim/popn))))")))])) (clojure.core/= :not-found ns-flag__10038__auto__) (throw (java.lang.Exception. "Cannot find anything to run for: popco.core.popco")) (clojure.core/= :threw ns-flag__10038__auto__) (do (clojure.core/binding [clojure.core/*out* clojure.core/*err*] (clojure.core/println (clojure.core/str "Can't find '" (quote popco.core.popco) "' as .class or .clj for " "lein run: please check the spelling."))) (throw data__10039__auto__))))))