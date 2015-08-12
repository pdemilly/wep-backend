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
	static allowedMethods = [index: "GET", show: "GET", create: "POST", update: "PUT", delete: "DELETE"]

	static mongodbService

	def index() {
		def collection = params.collection
		println ">>> MONGO: index: ${collection}: ${params}"

		params.max = (params.max && params.int('max') > 0) ? params.int('max') : mongodbService.DB."$collection".count() as Integer
                params.offset = Math.max (params.int('offset') ?: 0, 0)
                params.where = params.where ? JSON.parse (params.where) : [:]

                def p = params.findAll { it.key in [ 'max', 'offset' ] }

                def result = []
                mongodbService.DB."$collection".find(params.where).skip(p.offset).limit(p.max).each {
                        result << normalize(it)
                }
                respond result, [ status: OK ]
	}

	def show(String id) {
		def collection = params.collection
		println ">>> MONGO: show: ${collection}: ${params}"
		def result

                if (id) {
			println "looking for $id"
                        result = findOneById (collection, id)
                }

                respond result ? normalize(result) : [ error: "$id not found in $collection", status: 404 ]
	}

	@Transactional
        def create () {
		String collection = params.collection
                println ">>> MONGO create ${collection}: ${params}"

                // params.each { k, v -> println "params [$k] ="; println "\t<<$v>>" }

                def obj = params.data

                if (!obj) {
                        respond error: "obj is not defined", status: "${METHOD_FAILURE}"
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

		if (!params.data['_id']) {
			return create ()
		}

                def obj = params.data
                if (!obj) {
                        respond error: "Nothing to update", status: "${METHOD_FAILURE}"
                }
                else {
			if (obj._id) {
				id = obj.remove ('_id') // we need to remove it because not an ObjectID
				mongodbService.DB."$collection".update ([ _id: new ObjectId (id) ], obj)
			}
			else
				mongodbService.DB."$collection".update ([ id: id ], obj)

                        def result = findOneById (collection, id)
			respond normalize(result)
                }
	}

	protected findOneById (String collection, String id) {
		def obj = mongodbService.DB."$collection".findOne ([ id: id ])
		if (!obj) {
			obj = mongodbService.DB."$collection".findOne ([ _id: new ObjectId (id) ])
		}

		return obj
	}

	protected normalize (obj) {
		if (!obj.id)
		   obj.id = "${obj._id}"
		return obj
	}
}
