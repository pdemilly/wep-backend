package mongodb

import com.mongodb.MongoCredential
import com.mongodb.ServerAddress

import com.gmongo.GMongo
import com.gmongo.GMongoClient

class MDB {
	def __db = null
	
	def MDB (name) {
		def mongo = new GMongo ()
		__db = mongo.getDB (name)
	}

	def propertyMissing (String name) {
		return __db."${name}"
	}
}
