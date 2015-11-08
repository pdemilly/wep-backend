class UrlMappings {

	static mappings = {

                def restMapping=[GET:"index", PUT:"replaceAll", DELETE:"deleteAll", POST:"create"]
                def restMappingWithId=[GET:"show", PUT:"update", DELETE:"delete"]

                group ('/api') {

			// FIXME: Do something about gridFS - problem1: namespace - problem2: uri
			"/$namespace/fs/$type/$id(.$format)?"			(controller: 'gridFs', action: 'url', method: 'GET')

                        //***************************** Generic Rest Mapping **************************************************//

                        "/$namespace/$collection/count(.$format)?"              (controller: 'mongoRestful', action: 'count')
                        "/$namespace/$collection/aggregate(.$format)?"          (controller: 'mongoAggregator', parseRequest: true)
                        "/$namespace/$collection(.$format)?"                    (controller: 'mongoRestful', action: restMapping, parseRequest: true)
                        "/$namespace/$collection/$id(.$format)?"                (controller: 'mongoRestful', action: restMappingWithId, parseRequest: true)
                        "/$namespace/$collection/$action/$id(.$format)?"        (controller: 'mongoRestful', parseRequest: true)

		}

		// FIXME: do something about namespace
		"/download/$org/$type/$id"	(namespace: 'v1', controller: 'gridFs', action: 'download', method: 'GET', parseRequest: true)
		"/upload/$type(.$format)?"	(namespace: 'v1', controller: 'gridFs', action: 'upload', method: 'POST', parseRequest: true) 

		// accessing Monfodb console

		"/mongo/$dbname?/$colname?" {
			controller = "mviewer"
			action = "dispatchLink"
		}
	}
}
