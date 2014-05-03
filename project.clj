(defproject popco-x "0.0.1-SNAPSHOT"
  :url "https://github.com/mars0i/popco-x"
  :license {:name "to be filled in"
            :url "to be filled in"}
  :dependencies [[org.clojure/clojure "1.6.0"]]
  :main popco.core.popco ; this is what lein run will look for -main in
  :profiles {:dev {:dependencies [;[slingshot "0.10.3"][org.jblas/jblas "1.2.3"][mars0i/clatrix "0.4.0-SNAPSHOT"]
                                  [net.mikera/core.matrix "0.22.1-SNAPSHOT"]
                                  [net.mikera/vectorz-clj "0.20.1-SNAPSHOT"]
                                  [org.clojure/algo.generic "0.1.1"]
                                  [org.clojure/data.csv "0.1.2"]
                                  ;[org.clojure/math.combinatorics "0.0.7"]
                                  [criterium/criterium "0.4.3"]
                                 ]
                   :source-paths ["src"] ; where load will look for source files
                   }}
   :jvm-opts ["-Dclojure.compiler.disable-locals-clearing=true"] ; FASTER, and may be useful to debuggers. see https://groups.google.com/forum/#!msg/clojure/8a1FjNvh-ZQ/DzqDz4oKMj0J
   ;:jvm-opts ["-Dclojure.compiler.disable-locals-clearing=true" "-Xmx2m"] ; doesn't work
   ;:jvm-opts ["-Xmx2m"]
   ;:jvm-opts ["-XX:+TieredCompilation" "-XX:TieredStopAtLevel=1"] ; setting this to 1 will produce faster startup but will disable extra optimization of long-running processes
   ;:jvm-opts ["-XX:TieredStopAtLevel=4"] ; more optimization (?)
   ;:jvm-opts ["-server"] ; more optimization, slower startup, but supposed to be on by default except in 32-bit Windows machines
)
