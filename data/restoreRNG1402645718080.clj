(intern 'popco.core.person session-id 1402645718080)
(println "Session id/seed:" popco.core.person/session-id)
(intern 'popco.core.person 'initial-rng (utils.random/make-rng popco.core.person/session-id))