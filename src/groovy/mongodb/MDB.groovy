package mongodb

import com.mongodb.MongoCredential
import com.mongodb.ServerAddress

import com.gmongo.GMongo
import com.gmongo.GMongoClient

import com.mongodb.gridfs.GridFS

class MDB {
	def __db = null
	
	def MDB (name) {
		def mongo = new GMongo ()
		__db = mongo.getDB (name)
	}

	def getBucket (String name) {
		return new GridFS (__db, name);
	}

	def propertyMissing (String name) {
		return __db."${name}"
	}
}
