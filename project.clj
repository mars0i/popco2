(defproject popco-x "0.0.1-SNAPSHOT"
  :url "https://github.com/mars0i/popco-x"
  :license {:name "to be filled in"
            :url "to be filled in"}
  :dependencies [[org.clojure/clojure "1.5.1"]]
  :main popco.core.popco ; this is what lein run will look for -main in
  :profiles {:dev {:dependencies [;[criterium/criterium "0.4.2"]
                                  ;; NEXT LINE has to be fixed because clatrix 0.3.0 in clojars has 
                                  ;; a dep on core.matrix 0.7.2, which lacks important things, e.g. 
                                  ;; causes mmul to be not found.  ('lein deps :tree' is useful)
                                  ;[slingshot "0.10.3"][org.jblas/jblas "1.2.3"][clatrix "0.3.0"]
                                  [org.clojure/tools.macro "0.1.5"]
                                  ;[net.mikera/core.matrix "0.15.0"]
                                  ;[net.mikera/vectorz-clj "0.17.0"]
                                  [mars0i/core.matrix "0.16.1-SNAPSHOT"]
                                  [mars0i/vectorz-clj "0.17.1-SNAPSHOT"]
                                 ]
                   :source-paths ["src"] ; where load will look for source files
                   }}
   :jvm-opts ["-Dclojure.compiler.disable-locals-clearing=true"]
   ;:jvm-opts ["-XX:TieredStopAtLevel=4"]
   ;:jvm-opts ["-server"]
)
