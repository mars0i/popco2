semantic-iffs
=======

**Introduction**

"Semantic iffs" specify bidirectional link weights in the proposition
network in addition to link weights caused by propn-map-unit activations
in the analogy network, and in addition to link weights caused by
communication.  Semantic iff specifications have the form illustrated here:
````clojure
(def semantic-iffs [[-0.1 :CB-vpp :V-ipa]
                    [0.2 :CV-rpa :B-abp]])
````
The first element of each specification is a link weight.  
The other two are ids of propositions.  Their order doesn't matter.

These link weights sum with weights caused by other processes. 
However, link weights are not allowed to exceed the min (-1) and max
(1) weights.  

(Note that traditionally, I have specified a lower max weight
popco.nn.analogy/+analogy-max-wt+ = 0.5 for analogy net links.  This
reduces extreme cyclic activation value fluctuations in some models.
This limit doesn't apply to proposition networks, however.)

**How were semantic-iffs handled in popco1?**

* When initializing a person's networks, the following functions were called
on called on each person's semantic-iff specifications.  (Note that their
 syntax and storage was different than in popco2.)

````apply-record-raw-make-symlink-if-units --> record-raw-make-symlink-if-units````
