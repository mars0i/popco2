semantic-iffs
=======

"Semantic iffs" specify bidirectional link weights in the proposition
network in addition to link weights caused by propn-map-unit activations
in the analogy network, and in addition to link weights caused by
communication.  Semantic iff specifications have the form illustrated here:

````clojure
(def semantic-iffs [[-0.1 :CB-vpp :V-ipa]
                     [-0.1 :CV-rpa :B-abp]])
````
