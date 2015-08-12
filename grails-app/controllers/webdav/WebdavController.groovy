package webdav

import grails.converters.JSON
import com.github.sardine.SardineFactory
import static org.springframework.http.HttpStatus.*

import org.bson.types.ObjectId

class WebdavController {

	static namespace = 'v1'

	def crsService
	def mongodbService

	static responseFormats = ['json', 'xml']
	static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

	def index() {
		println "Get Documents: $params"

		def org = crsService.organization.id

		def result = []

		try {
			if (folder) {
				def webdav = SardineFactory.begin()
				def url = getWebDavServerUrl(folder, subfolder)
				println "webdav: $url"
				webdav.list(url).grep { !it.directory }.sort { a, b -> a.creation <=> b.creation }.reverse().each {
					def id = it.path
					result <<  [ 
						id: it.path,
						contentLength: it.contentLength,
						contentType: it.contentType,
						creation: it.creation,
						etag: it.etag,
						name: it.name,
						modified: it.modified,
						// TODO: temporary until I figure out a central download module with UUID and TTL
						url: g.createLink (absolute: true, controller: 'download', action: 'customerDocument', params: [ id: custno, org: org, file: it.name ]), 
					]
				}
				println "result=$result"
			}
		}
		catch (e) {
		}

		respond result, [ status: OK ]
	}

	def download () {
		println "downloading documents: $params"

		try {
			def webdav = SardineFactory.begin ()
			def url = "${getWebDavServerUrl(params.id)}/${params.file}"
			println "download URL: ${url}"
			def resources = webdav.list (url)
			if (resources.size() == 1) {
				def file = resources.get(0);
				response.contentType = file.contentType
				response.contentLength = file.contentLength
				response.setHeader("Content-Disposition", "attachment;filename=${file.name}")
				def input = webdav.get (url)
				response.outputStream << input
				response.outputStream.flush()
				input.close()
			}
		}
		catch (e) { }
	}

	private getWebDavServerUrl (folder, name) {
		def org = crsService.organization.id
		return "http://localhost/fs/${folder}/${name}"
	}
}

