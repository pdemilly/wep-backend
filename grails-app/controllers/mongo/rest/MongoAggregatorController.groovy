
package mongo.rest

import static org.springframework.http.HttpStatus.*
import org.springframework.web.multipart.MultipartHttpServletRequest
import org.springframework.web.multipart.commons.CommonsMultipartFile
import grails.converters.JSON
import org.apache.commons.io.*

import org.bson.types.ObjectId
import com.mongodb.AggregationOptions

class MongoAggregatorController {

	static namespace = 'v1';
	static responseFormats = ['json', 'xml']
	static allowedMethods = [ GET: 'execute', ]

	// db.resource_allocations.aggregate ([ { $match: { type: 'Contribution to Growth' } }, { $group: { "_id": { code: "$pl1_name", name: "$fullname", }, total: { $sum: "$res_alloc_amt" }, count: { $sum: 1 }, avg: { $avg: "$res_alloc_amt" } } } ], { explain: true, allowDiskUse: true, cursor: 10  })

	def mongodbService
	def crsService

	// working version is execute2

	def execute () {
		def v = params.int ('version')
		return (v > 0) ?  "execute$v" () : execute2()
	}

	private execute3 () {
		def collection = params.collection
		// for rows and cols we create a map of g1, g2 and c1, c2 respectively with a value of '$key' with key being the row or col wanted
		// so rows=[ 'state', 'county' ] becomes rows=[ g0: '$state', g1: '$county' ]
		def rows = JSON.parse (params.rows ?: "[]").withIndex().collectEntries  { field, i -> [ ('g'+i):('$'+field) ] }
		def columns = JSON.parse (params.columns ?: "[]").withIndex().collectEntries  { field, i -> [ ('c'+i):('$'+field) ] }
		def values = JSON.parse (params.values ?: "[]").withIndex().collectEntries { aggr, i -> 
			def s = aggr.split(':')
			(s.size() > 1) ?  [ ('v'+i): [ ('$'+s.first()):('$'+s.last()) ] ] : [ ('v'+i): [ ('$sum'):1 ] ]
		}
		def pre_condition = JSON.parse (params.pre_condition ?: "{}")
		def post_condition = JSON.parse (params.post_condition ?: "{}")
		def orderBy = JSON.parse(params.orderBy ?: "{}")
		def query = []

		if (pre_condition) 
		   query += [ '$match': pre_condition ]

		def group = [ _id: rows + columns ] + values
		query += [ '$group': group ] 

		if (post_condition) 
		   query += [ '$match': post_condition ]

		if (orderBy)
		   query += [ '$sort': orderBy ]

		println "ROWS: $rows"
		println "COLS: $columns"
		println "VALS: $values"
		println "PRE: $pre_condition"
		println "POST: $post_condition"
		println "GRP: $group"
		println "Q: $query"

		def results = mongodbService.DB."$collection".aggregate (query).results()
	
		def cols = mongodbService.DB."$collection".distinct (JSON.parse (params.columns)[0]);
		println "DISTINCT COLS: $cols"
		def R = [ cols: cols, results: results ]

		respond R
	}

	private execute2 () {
		def collection = params.collection
		def rows = JSON.parse (params.rows ?: "[]").collectEntries  { field -> [ (field):('$'+field) ] }
		def columns = JSON.parse (params.columns ?: "[]").collectEntries  { field -> [ (field):('$'+field) ] }
		def values = JSON.parse (params.values ?: "{}")
		def pre_condition = JSON.parse (params.pre_condition ?: "{}")
		def post_condition = JSON.parse (params.post_condition ?: "{}")
		def orderBy = JSON.parse(params.orderBy ?: "{}")
		def query = []

		if (pre_condition) 
		   query += [ '$match': pre_condition ]

		def group = [ _id: rows + columns ] + values
		query += [ '$group': group ] 

		if (post_condition) 
		   query += [ '$match': post_condition ]

		if (orderBy)
		   query += [ '$sort': orderBy ]

		def colValueKeys = [:]
		columns.keySet().each { k ->
			colValueKeys[k] = mongodbService.DB."$collection".distinct(k).withIndex().collectEntries { v, i -> [ (v): k+i ] }
		}

		println "colValueKeys: $colValueKeys"
		def results = [:]
		mongodbService.DB."$collection".aggregate (query).results().each { row ->

			def row_values = row._id.grep { it.key in rows.keySet() }
			def col_values = row._id.grep { it.key in columns.keySet() }
			def aggr_values = row.clone()
			aggr_values.remove ('_id')

			println "row_values:(${row_values.size()}) $row_values"
			println "col_values(${col_values.size()}): $col_values"

			def row_result = results["${row_values}"]
			if (!row_result)
			   row_result = results["${row_values}"] = row_values.collectEntries { it }

			println "row_result: $row_result"
			// field key is a list of all column field keys
			def field_keys = col_values.collect { colValueKeys[it.key][it.value] }
			println "field_keys: $field_keys"
			// now that we have the list of field keys for each we initialize an empty map
			field_keys.inject(row_result) { r, k -> r[k] = [:]; r[k] }
			// now let's iterate over the aggregate and fill their values
			aggr_values.each { aggr ->
				field_keys.inject(row_result) { r, k -> r[k] }[aggr.key] = aggr.value
			}
		}

		def R = [ colKeys: colValueKeys, rowData: results.values() ]
		respond R
	}

	private execute4 () {

		def collection = params.collection
		def rows = JSON.parse (params.rows ?: "[]").collectEntries  { field -> [ (field):('$'+field) ] }
		def columns = JSON.parse (params.columns ?: "[]").collectEntries  { field -> [ (field):('$'+field) ] }
		def values = JSON.parse (params.values ?: "{}")
		def pre_condition = JSON.parse (params.pre_condition ?: "{}")
		def post_condition = JSON.parse (params.post_condition ?: "{}")
		def orderBy = JSON.parse(params.orderBy ?: "{}")
		def query = []

		if (pre_condition) 
		   query += [ '$match': pre_condition ]

		def group = [ _id: rows + columns ] + values
		query += [ '$group': group ] 

		if (post_condition) 
		   query += [ '$match': post_condition ]

		if (orderBy)
		   query += [ '$sort': orderBy ]

		println "ROWS: $rows"
		println "COLS: $columns"
		println "VALS: $values"
		println "PRE: $pre_condition"
		println "POST: $post_condition"
		println "GRP: $group"
		println "Q: $query"


		def colDefs = []
		def groups = []

		columns.keySet().eachWithIndex { k, i ->
			def newGroup = []
			if (groups) {
				println "got groups: $groups"
				groups.each { 
					newGroup += buildColDef (collection, k, it)
				}
			}
			else {
				println "got no groups"
				newGroup = buildColDef (collection, k)
			}

			println "newGroup: $newGroup"
			groups = newGroup
		}
		colDefs = groups

		/*
		mongodbService.DB."$collection".aggregate (query).results().each { row ->
			def group = row._id.subMap(r)
			def cols = row._id.subMap(c)
			def vals = row - row._id
			if (!results[group])
			   results[group = [:]
			results[group][cols] = vals
		};
		def grid = []
		results.collect {k, v ->
		}
		respond grid
		*/
		respond colDefs
	}

	private buildColDef (collection, field, groupHeader = [:]) {
		def colDefs = []
		mongodbService.DB."$collection".distinct (field).eachWithIndex { v, n -> 
			def f = field + n
			def colDef = [ headerName: v ]
			if (groupHeader) {
				colDef.field = groupHeader.field + '.' + field + n
				colDef.headerGroup = groupHeader.headerName
			}
			else {
				colDef.field = field + n
			}
			colDefs += colDef
		}
		return colDefs
	}
		
	private execute1 () {

		def collection = params.collection
		def rows = JSON.parse (params.rows ?: "{}")
		def columns = JSON.parse (params.columns ?: "{}")
		def values = JSON.parse (params.values ?: "{}")
		def pre_condition = JSON.parse (params.pre_condition ?: "{}")
		def post_condition = JSON.parse (params.post_condition ?: "{}")
		def orderBy = JSON.parse(params.orderBy ?: "{}")
		def query = []

		if (pre_condition) 
		   query += [ '$match': pre_condition ]

		def group = [ _id: rows + columns ] + values
		query += [ '$group': group ] 

		if (post_condition) 
		   query += [ '$match': post_condition ]

		if (orderBy)
		   query += [ '$sort': orderBy ]

		println "ROWS: $rows"
		println "COLS: $columns"
		println "VALS: $values"
		println "PRE: $pre_condition"
		println "POST: $post_condition"
		println "GRP: $group"
		println "Q: $query"

		respond mongodbService.DB."$collection".aggregate (query).results()
	}
}
