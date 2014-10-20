(intern 'popco.core.constants 'session-id 1413538443039)
(println "Session id/seed:" popco.core.constants/session-id)
(intern 'popco.core.constants 'initial-rng (utils.random/make-rng popco.core.constants/session-id))

