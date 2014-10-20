(intern 'popco.core.constants 'session-id 1412219211214)
(println "Session id/seed:" popco.core.constants/session-id)
(intern 'popco.core.constants 'initial-rng (utils.random/make-rng popco.core.constants/session-id))

