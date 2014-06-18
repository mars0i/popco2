(intern 'popco.core.person session-id 1400966655770)
(println "Session id/seed:" popco.core.person/session-id)
(intern 'popco.core.person 'initial-rng (utils.random/make-rng popco.core.person/session-id))