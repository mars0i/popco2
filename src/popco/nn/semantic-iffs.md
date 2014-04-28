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
The first element of each specification is a link weight.  The other two
are ids of propositions.  Their order doesn't matter.

These link weights sum with weights caused by other processes. 
However, link weights are not allowed to exceed the min (-1) and max
(1) weights.  

(Note that traditionally, I have specified a lower max weight
popco.nn.analogy/+analogy-max-wt+ = 0.5 for analogy net links.  This
reduces extreme cyclic activation value fluctuations in some models.
This limit doesn't apply to proposition networks, however.)

**Summary of what needs to be done with semantic-iffs**

* When first initializing a person, create a link weight in linger-wt-mat
  that's equal to the semantic-iff's weight.

* When updating the propn net, add the current weights in linger-wt-mat to whatever weight
  is specified by the analogy net activations.
