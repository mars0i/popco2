uberjar.md
====

	lein uberjar

	popco -n sims.bali.hermits -r
	'(rp/write-propn-activns-csv-for-R 
	   (take 100 (map rp/ticker (mn/many-times sim/popn))))'

where `popco` executes `java -jar target/<jarfile>`.
