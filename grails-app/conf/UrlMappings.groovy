class UrlMappings {

	static mappings = {

		group ('/api') {

			// Generic Rest Mapping 
			"/$namespace/$collection(.$format)?" (parseRequest: true) {
				controller = 'mongoRestful'
				action = [GET:'index', PUT:'replaceAll', DELETE:'deleteAll', POST:'create']
				constraints {
					println "Generic rest mapping 1: $controllerName $actionName"
				}
			}

			"/$namespace/$collection/$id?(.$format)?" {
				controller = 'mongoRestful'
				action = [GET:'show', PUT:'update', DELETE:'delete']
				constraints {
					println "Generic rest mapping 2: $controllerName $actionName $id"
				}
			}

			"/$namespace/$collection/$action/$id(.$format)?" {
				controller = 'mongoRestful'
				constraints {
					// println "Generic rest mapping 3: $controllerName $actionName $id"
				}
			}


			"/$namespace/$collection/aggregate(.$format)?" {
				controller = 'mongoAggregator'
				constraints {
				}
			}

			"/v1/fs/$type/$id(.$format)?" {
				controller = 'gridFs'
				action = [ GET: 'getUrl' ]
			}

		}

		"/download/$org/$type/$id" (parseRequest: true) {
			controller = 'gridFs'
			action = 'download'
		}


		"/upload/$type(.$format)?" {
			controller = 'gridFs'
			action = [ POST: 'upload' ]
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

	}
}
