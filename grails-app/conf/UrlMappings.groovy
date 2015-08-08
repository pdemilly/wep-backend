class UrlMappings {

        def restMapping=[GET:"index", PUT:"replaceAll", DELETE:"deleteAll", POST:"create"]
        def restMappingWithId=[GET:"show", PUT:"update", DELETE:"delete"]

	static mappings = {

                // Generic Rest Mapping 
                "/api/$namespace/$controller(.$format)?" (parseRequest: true) {
                        action = restMapping
                        constraints {
                                println "Generic rest mapping 1: $controllerName $actionName"
                        }
                }

                "/api/$namespace/$controller/$id?(.$format)?" (parseRequest: true) {
                        action = restMappingWithId
                        constraints {
                                println "Generic rest mapping 2: $controllerName $actionName $id"
                        }
                }

                "/api/$namespace/$controller/$action/$id(.$format)?" (parseRequest: true) {
                        constraints {
                                println "Generic rest mapping 3: $controllerName $actionName $id"
                        }
                }


		"/$controller/$action?/$id?(.$format)?"{
		    constraints {
			// apply constraints here
		    }
		}

		"/"(view:"/index")
		"500"(view:'/error')
	}
}
