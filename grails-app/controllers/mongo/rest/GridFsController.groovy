package mongo.rest

import static org.springframework.http.HttpStatus.*
import org.springframework.web.multipart.MultipartHttpServletRequest
import org.springframework.web.multipart.commons.CommonsMultipartFile
import grails.converters.JSON
import org.apache.commons.io.*
import net.sf.jmimemagic.Magic

import org.bson.types.ObjectId

class GridFsController {

	static responseFormats = ['json', 'xml']
	static allowedMethods = [ GET: 'download', POST: 'upload' ]

	def mongodbService

	def mimeParser = new Magic ()

	def download (String type, String id) {

		println "GridFS: Download ${type} ${id}"
		def bucket = mongodbService.DB.getBucket(type)
		def doc = bucket.findOne (new ObjectId (id))

		response.outputStream << doc.inputStream
		response.contentType = doc.contentType
	}

        def upload (String type) {
                println "Upload: $params"

                if (request instanceof MultipartHttpServletRequest) {
                    for (filename in request.getFileNames ()) {
                        println "upload file: for type: $type $filename: ${request.dump()}"
                        println "parameter map: ${request.parameterMap}"

                        // Let's extract the parameters
                        def p = request.parameterMap

                        try {
				def remoteFile = request.getFile (filename)

				// Let's save it to a temp file first
				def tempFile = File.createTempFile (type, ".tmp")
				remoteFile.transferTo (tempFile)

				// Now let save it to our bucket
				def bucket = mongodbService.DB.getBucket(type)
				def doc = bucket.createFile (tempFile.newInputStream(), remoteFile.originalFilename, true)

				// find the mine type and save it
				doc.contentType = mimeParser.getMagicMatch(tempFile, true).mimeType
				doc.save()

				// cleanup and return the id to be able to download
				tempFile.delete()

                                def result = [ _id: "${type}/${doc._id}" ]
				respond result
                        }
                        catch (err) {
                                println "An error has occur: ${err}"
                                respond error: err, status: "420"
                        }

                    }
                }
        }
}
