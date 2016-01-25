(defproject popco2 "1.0.0"
  :url "https://github.com/mars0i/popco2"
  :license {:name "Gnu General Public License version 3.0"
            :url "http://www.gnu.org/copyleft/gpl.html"}
  :source-paths ["src"]
  :java-source-paths ["src/java"]
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [net.mikera/core.matrix "0.49.0"]
                 [net.mikera/vectorz-clj "0.41.0"]
                 [org.clojure/algo.generic "0.1.2"]
                 [org.clojure/math.numeric-tower "0.0.4"]
                 [org.clojure/data.csv "0.1.3"]
                 [org.clojure/data.xml "0.0.8"]
                 [org.clojure/tools.cli "0.3.3"]]
  :plugins [[lein-exec "0.3.4"]] ; allows passing expressions to eval on commandline with -e, etc. see lein help exec.
  :main popco.core.popco
  :aot [popco.core.popco]
  :jvm-opts ["-Xmx2g" "-XX:-UseConcMarkSweepGC" "-Dclojure.compiler.disable-locals-clearing=true" "-Djava.awt.headless=true"] ; consider adding:  -XX:-UseGCOverheadLimit 
  ;; The :dev profile is merged into the top-level dependencies unless 'with-profile' is used:
  :profiles {:dev {:repl-options {:nrepl-middleware [io.aviso.nrepl/pretty-middleware]}
                   :dependencies [[criterium/criterium "0.4.3"]
                                  ;[incanter/incanter-core "1.5.6"]
                                  [io.aviso/pretty "0.1.18"]]}
             :bali-netlogo ^:leaky {:aot [sims.bali.netlogo]}}
             ;; re "leaky": http://librelist.com/browser//leiningen/2014/9/25/wrong-clojure-release-when-compiling-if-with-profile-and-uberjar-is-used/#db9a114b3b07b9ad6d4c291a9f0cb8d6
             ;; IS IT NEEDED?
)




;; old options:

;"-Djava.awt.headless=true" Keep Incanter's Swing libs from opening
;:jvm-opts ["-Dclojure.compiler.disable-locals-clearing=true"] ; FASTER, and may be useful to debuggers. see https://groups.google.com/forum/#!msg/clojure/8a1FjNvh-ZQ/DzqDz4oKMj0J
;:jvm-opts ["-XX:+TieredCompilation" "-XX:TieredStopAtLevel=1"] ; setting this to 1 will produce faster startup but will disable extra optimization of long-running processes
;:jvm-opts ["-XX:TieredStopAtLevel=4"] ; more optimization (?)
;:jvm-opts ["-server"] ; more optimization, slower startup, but supposed to be on by default except in 32-bit Windows machines
;[net.mikera/core.matrix "0.22.1-OTHERZEROS"] ; my hack version that allows mmul to return numbers of the same kind as input
;[bigml/sampling "2.1.1"]
;[it.uniroma1.dis.wsngroup.gexf4j/gexf4j "0.4.4-BETA"] ; Java GEXF format library
;[gexf "0.1.0-SNAPSHOT"] ; Clojure GEXF format library

;; OLD bali-netlogo options:
;             :bali-netlogo ^:leaky {:aot [sims.bali.netlogo]
;                                    ;:javac-options ["-source" "1.8" "-target" "1.6"] ; because NetLogo 5.2 was compiled with java 1.6
;                                    ;:exclusions [ec.util.MersenneTwisterFast]
;                                    ;:uberjar-exclusions [#"(?:^|/)java/ec/util/MersenneTwisterFast*"]
;                                    ;:uberjar-exclusions [#"ec/util/MersenneTwisterFast.class"]
;                                    ;:dependencies [;[net.mikera/clojure-utils "0.5.0"]
;                                                   ;[net.mikera/core.matrix "0.49.0"]
;                                                   ;[net.mikera/vectorz-clj "0.41.0"]
;                                                   ;[incanter/incanter-core "1.5.6"]
;                                                   ;[org.clojure/algo.generic "0.1.2"]
;                                                   ;[org.clojure/data.csv "0.1.3"]
;                                                   ;[org.clojure/data.xml "0.0.8"]
;                                                   ;[org.clojure/tools.cli "0.3.3"]]
;                                    }
 
