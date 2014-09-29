How to perform a basic sanity check of analogies
====

1. Once you've defined propositions, semantic rules, etc., make a
   population specification file in which there are (for example) four
   persons.  Specify that none communicate at all, that all have all
   of the propositions.  Suppose that in each of the two analogs, there
   are two parts, representing two ways that the analogy could settle.
   This is how I set up the Sanday analogies and the crime analogies.

	Example file: src/sims/crime/hermits.clj.  ("hermits" is a good
	name for this kind of file, since no one communicates.  Each
	person is lost in its own perceptions and thought processes.)

2. Next, create a csv file of proposition activations in a format
   suitble for reading into R.  Here is code that does that with
   the population defined in crime/hermits.clj:

````
(unlocknload 'sims.crime3.hermits)
(rpt/write-propn-activns-csv 
   (take 500 (many-times popn))
   :cooker csv/cook-name-for-R)
````

