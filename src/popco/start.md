Starting POPCO 2
=======

*Normal initialization*

**In the model's propn file:**

   * Make two sets of propositions.

   * Make sem-relations specs

**In the model's specification file(s):**

   * Make analogy network from the propositions and special constants and sem-relations.

   * Make model propn network from the propositions and sem-relations.

   * Make persons using a (perhaps improper) subset all of the propositions using make-person.

**make-person does the following, among other things:**

  * Make fresh copy of propn network that's passed in.

  * Store the analogy network that's passed in as is. It will not be modified.

  * Create and fill proposition and analogy masks.

  * Make (zeroed) proposition and analogy activation vectors.

**Fill the population:**

  * Put the persons into the sequence of persons in the population
    (normally this is folks)
    
  * The population should normally have tick = 0 at this point.

**This is done by function 'popco.core.main/init':**

  * Do any initial settling of the analogy network that's desired.

  * Other initialization.
