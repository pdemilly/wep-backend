import grails.converters.JSON
import org.bson.types.ObjectId

import org.bson.BSON
import org.bson.Transformer
import org.joda.time.DateTime

class BootStrap {

    def init = { servletContext ->

                Date.metaClass.'static'.fromString = { str ->
                    com.mdimension.jchronic.Chronic.parse(str).beginCalendar.time
                }

                JSON.registerObjectMarshaller(ObjectId) {
                        // println "coverting an objectid"
                        return it.toStringMongod()
                }

		JSON.registerObjectMarshaller(Date) {
			return it?.format("dd-MM-yyyy")
		 }

    }
    def destroy = {
    }
}
