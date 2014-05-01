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
                                  ;[criterium/criterium "0.4.3"]
                                 ]
                   :source-paths ["src"] ; where load will look for source files
                   }}
   :jvm-opts ["-Dclojure.compiler.disable-locals-clearing=true"]
   ;:jvm-opts ["-XX:TieredStopAtLevel=4"]
   ;:jvm-opts ["-server"]
)
