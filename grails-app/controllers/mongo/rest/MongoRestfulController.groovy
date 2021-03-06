package mongo.rest

import static org.springframework.http.HttpStatus.*
import grails.transaction.Transactional

import grails.converters.JSON
import groovy.json.JsonSlurper
import org.bson.types.ObjectId

@Transactional(readOnly = true)
class MongoRestfulController {

	static namespace = 'v1'

	static responseFormats = ['json', 'xml']
	static allowedMethods = [ index: 'GET', show: 'GET', 'count': 'GET', create: 'POST', update: 'PUT', delete: 'DELETE']

	static mongodbService

	def index() {

		params.limit = (params.limit && params.int('limit') > 0) ? params.int('limit') : 0
                params.offset = Math.max (params.int('offset') ?: 0, 0)

		def result = []
		list().skip(params.offset).limit(params.limit).each {
                        result << normalize(it)
                }
		respond result
	}

	def count() {
		def result =  [ count: list().count() ]
		respond result
	}

	private list() {
		def collection = params.collection
		println ">>> MONGO: list: ${collection}: ${params}"

                params.where = params.where ? JSON.parse (params.where) : [:]

		if (params.filter) {
			params.where += [ '$text': [ '$search': params.filter ] ]
		}

                return mongodbService.DB."$collection".find(params.where)
	}

	def show(String id) {
		def collection = params.collection
		println ">>> MONGO: show: ${collection}: ${params}"
		def result

                if (id) {
			println "looking for $id"
                        result = findOneById (collection, id)
                }

		if (result)
			respond normalize(result) 
		else
			render error: "$id not found in $collection", status: 404 
	}

	@Transactional
        def create () {
		String collection = params.collection
                println ">>> MONGO create ${collection}: ${params}"

                // params.each { k, v -> println "params [$k] ="; println "\t<<$v>>" }

                def obj = params.data

                if (!obj) {
                        render error: "obj is not defined", status: "${METHOD_FAILURE}"
                }
                else {
                        mongodbService.DB."$collection" << obj
			respond normalize(obj)
                }
        }



	@Transactional
	def update(String id) {
		String collection = params.collection
                println ">>> MONGO update ${collection}: ${params}"

		if (!params?.data?._id) {
			return create ()
		}

                def obj = params.data
                if (!obj) {
                        render error: "Nothing to update", status: "${METHOD_FAILURE}"
                }
                else {
			if (obj?._id) {
				id = obj.remove ('_id') // we need to remove it because not an ObjectID
				mongodbService.DB."$collection".update ([ _id: new ObjectId (id) ], obj)
			}
			else
				mongodbService.DB."$collection".update ([ id: id ], obj)

                        def result = findOneById (collection, id)
			if (result)
				respond normalize(result) 
			else
				render error: "$id not found in $collection", status: 404 
                }
	}

	protected findOneById (String collection, String id) {
		def obj = mongodbService.DB."$collection".findOne ([ id: id ])
		if (!obj) {
			try {
				obj = mongodbService.DB."$collection".findOne ([ _id: new ObjectId (id) ])
			} catch (e) {
				return null
			}
		}

		return obj
	}

	protected normalize (obj) {
		if (!obj.id)
		   obj.id = "${obj._id}"
		return obj
	}
}
