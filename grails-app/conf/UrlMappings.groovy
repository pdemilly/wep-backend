class UrlMappings {

	static mappings = {

                // Generic Rest Mapping 
                "/api/$namespace/$collection(.$format)?" (parseRequest: true) {
			controller = 'mongoRestful'
                        action = [GET:'index', PUT:'replaceAll', DELETE:'deleteAll', POST:'create']
                        constraints {
                                println "Generic rest mapping 1: $controllerName $actionName"
                        }
                }

                "/api/$namespace/$collection/$id?(.$format)?" {
			controller = 'mongoRestful'
                        action = [GET:'show', PUT:'update', DELETE:'delete']
                        constraints {
                                println "Generic rest mapping 2: $controllerName $actionName $id"
                        }
                }

                "/api/$namespace/$collection/$action/$id(.$format)?" {
			controller = 'mongoRestful'
                        constraints {
                                // println "Generic rest mapping 3: $controllerName $actionName $id"
                        }
                }


		// accessing Monfodb console

		"/mongo/$dbname?/$colname?" {
			controller = "mviewer"
			action = "dispatchLink"
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
