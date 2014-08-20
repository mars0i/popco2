(ns popco.io.gexf
  (:import [it.uniroma1.dis.wsngroup.gexf4j.core
            EdgeType
            Gexf
            Graph
            Mode
            Node
            data.Attribute
            data.AttributeClass
            data.AttributeList
            data.AttributeType
            impl.GexfImpl
            impl.StaxGraphWriter
            impl.data.AttributeListImpl
            viz.NodeShape]))

;; make GEXF object in which to store data before writing
(def gexf (GexfImpl.))
;; get references to some of its components:
(def metadata (.getMetadata gexf))
(def graph (.getGraph gexf))
;; and other structures we'll use:
(def attr-list (AttributeListImpl. AttributeClass/NODE))
;; examples from StaticGexfGraph.java - not what I really need ...:
(def attr-url (.createAttribute attr-list "0" AttributeType/STRING "url"))
(def attr-indegree (.createAttribute attr-list "1" AttributeType/FLOAT "indegree"))
(def attr-frog (.createAttribute attr-list "2" AttributeType/BOOLEAN "frog"))
;(.setDefaultValue attr-frog true)

;; store metadata into the GEXF object
(-> metadata
    (.setLastModified (java.util.Date.))
    (.setCreator "Marshall Abrams")
    (.setDescription "POPCO within-person network graph") )

(-> graph 
    (.setDefaultEdgeType EdgeType/UNDIRECTED)
    (.setMode Mode/STATIC))
(.add (.getAttributeLists graph) attr-list)

gexf
