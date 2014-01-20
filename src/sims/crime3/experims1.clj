(ns sims.crime3.experims1
  (:use popco.core.person
        [popco.nn.analogy :only [make-analogy-net]]
        ;popco.nn.nets
        ;[sims.crime3.propns :only [all-propns living-propns virus-propns 
        ;                                beast-propns crime-propns viral-crime-propns 
        ;                                beastly-crime-propns]]
  )
  (:require [sims.crime3.propns :as c3p]
  )
  (:import [popco.core.person Person]
           ;[popco.nn.nets PropnNet AnalogyNet]
  ))



