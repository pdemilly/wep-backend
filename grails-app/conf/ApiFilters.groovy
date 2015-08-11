
class ApiFilters {

	def crsService

	static APIKEYS = [
		"33cfed4ef45559656b88b54fcf17af49": "ffah",
		"d9190ff7813421ad6bfa9fc5ff6053e0": "integrity-housing",
	];

	private checkAuthorization (request) {
		def renderError =  { status, message ->
			boolean xml = request.getHeader("accept").contains("xml")
			boolean json = request.getHeader("accept").contains("json")
			def result = [:]
			if (json || !xml) {
				result = [ status: status, contentType: 'application/json', text: '{"error":"' + message + '"}']
			} else {
				result = [ status: status, contentType: 'application/xml', text: '' + message + ''] 
			}
			println "$result"
			return result
		}

		try {

			String authorizationHeader = request.getHeader('authorization')?.trim()?.toLowerCase()

			String key

			if (authorizationHeader == null) {
				// Try with api_key request parameter and render error if it's missing.
				key = params.api_key
				if (key == null) {
					renderError(403, 'I need an authorization parameters to serve this request.')
					return false
				}
			} else {
				try {
					byte[] token
					if (authorizationHeader.startsWith("apikey ")) {
						token = authorizationHeader.substring(7).getBytes("UTF-8");
					} 
					key = new String(token, "UTF-8");
				} catch (ignore) { }

				// Return if key is empty.
				if (key == null || key.trim() == "") {
					renderError(401, "Check your authorization header syntax. It should look like \"Authorization: APIKey [your_api_key]\".")
					return false
				}
			}
			// Find API key and return if it does not exist.

			if (!APIKEYS[key]) {
				renderError(401, "Unknown API key: $key")
				return false
			}

			println "API check key $key"
			def org = APIKEYS[key]
			println "setting org to: $org for key: $key"
			crsService.organization.id = org

		} catch (Exception e) {
			renderError(500, "${e}")
			return false
		}
	}

	def filters = {
	
		log (uri: '/**') {
			before = {
				// println "request: ${request} params: ${params}"
				// return true
			}
		}

		// TODO: We should validate before uploading documents - good for now

		download (uri: '/upload/**') {
			before = {
				if (request.JSON) {
					params.data = [:]
					params.data << request.JSON
				}
				return checkAuthorization (request)
			}
		}

		download (uri: '/fs/**') {
			before = {
				if (request.JSON) {
					params.data = [:]
					params.data << request.JSON
				}
				return checkAuthorization (request)
			}
		}

		download (uri: '/download/**') {
			before = {
				return true
			}
		}

		rest (uri: '/api/**') {

			before = {
				if (request.JSON) {
					params.data = [:]
					params.data << request.JSON
				}
				println "\n\nApi filter request: ${request.requestURI} (${request.dispatcherType}) params: ${params}"
				return checkAuthorization (request)
			}

		}
	}
}
