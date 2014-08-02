(defproject popco2 "0.0.2-SNAPSHOT"
  :url "https://github.com/mars0i/popco2"
  :license {:name "to be filled in"
            :url "to be filled in"}
  :dependencies [[org.clojure/clojure "1.6.0"]]
  :main popco.core.popco ; will be run automatically in the repl
  :profiles {:dev {:dependencies [;[slingshot "0.10.3"][org.jblas/jblas "1.2.3"][mars0i/clatrix "0.4.0-SNAPSHOT"]
                                  ;[net.mikera/core.matrix "0.22.1-OTHERZEROS"] ; my hack version that allows mmul to return numbers of the same kind as input
                                  [net.mikera/core.matrix "0.27.2-SNAPSHOT"]
                                  [net.mikera/vectorz-clj "0.20.1-SNAPSHOT"]
                                  [org.clojure/algo.generic "0.1.1"]
                                  [org.clojure/data.csv "0.1.2"]
                                  ;[org.clojure/math.combinatorics "0.0.7"]
                                  ;[incanter/incanter "1.5.5"]
                                  [incanter/incanter-core "1.5.5"]
                                  [org.clojure/data.generators "0.1.2"]
                                  [bigml/sampling "2.1.1"]
                                  [criterium/criterium "0.4.3"]]
                   :source-paths ["src"] ; where load will look for source files
                   :java-source-paths ["src/java"]
                   }}
   :jvm-opts ["-Xmx1g" "-Dclojure.compiler.disable-locals-clearing=true" "-Djava.awt.headless=true"]
   ; "-Djava.awt.headless=true" Keep Incanter's Swing libs from opening
   ;:jvm-opts ["-Dclojure.compiler.disable-locals-clearing=true"] ; FASTER, and may be useful to debuggers. see https://groups.google.com/forum/#!msg/clojure/8a1FjNvh-ZQ/DzqDz4oKMj0J
   ;:jvm-opts ["-Xmx2g"]
   ;:jvm-opts ["-XX:+TieredCompilation" "-XX:TieredStopAtLevel=1"] ; setting this to 1 will produce faster startup but will disable extra optimization of long-running processes
   ;:jvm-opts ["-XX:TieredStopAtLevel=4"] ; more optimization (?)
   ;:jvm-opts ["-server"] ; more optimization, slower startup, but supposed to be on by default except in 32-bit Windows machines
)
