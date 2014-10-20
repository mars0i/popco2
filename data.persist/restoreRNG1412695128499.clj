(intern 'popco.core.constants 'session-id 1412695128499)
(println "Session id/seed:" popco.core.constants/session-id)
(intern 'popco.core.constants 'initial-rng (utils.random/make-rng popco.core.constants/session-id))

