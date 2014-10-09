initialnts3.md 
====

	Is-royal [:ob-r]
	Is-state [:negara]
	Prays [:ob-r] :royal-prays
	Ordered [:negara] :ordered-negara
	Causal-if [:royal-prays :ordered-negara]

	Is-subak [:ob-s]
	Is-field [:ob-rice-fields]
	Prays [:ob-s] :subak-prays
	Ordered [:ob-rice-fields] :ordered-rice-fields
	Causal-if [:subak-prays :ordered-rice-fields]

	Is-pest [:ob-rats]
	In [:ob-rats :ob-rice-fields] :rats-in-rice
	Disordered [:rats-in-rice]

I'm using Ordered and Disordered differently above.  I applied Ordered
to objects, and Disordered to a situation.  How about, instead, this:

	Disordered [:ob-rice-field] :disorderd-rice
	Causal-if [:rats-in-rice :disordered-rice]


(def conceptual-relats [[-1.0 :Causal-if :Preventative-if]
                        [-1.0 :Ordered :Disorderd]])


