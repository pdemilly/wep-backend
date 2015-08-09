
package mongodb

import com.mongodb.MongoCredential
import com.mongodb.ServerAddress

import com.gmongo.GMongo
import com.gmongo.GMongoClient

class MongodbService {

	def crsService
	def dbs = [:]

	def getDB () {
		return getDB (crsService.org.id)
	}

	def getDB (String id) {
		def db = dbs[id] ?: connectToDB (id)
	}

	private connectToDB (name) {
		// TODO: get credentials from config
		// def credentials = MongoCredential.createMongoCRCredential('username', 'database', 'password' as char[])
		// dbs[name] = new GMongoClient(new ServerAddress(), [/*credentials*/])

		dbs[name] = new MDB (name)
		return dbs[name]
	}
}
