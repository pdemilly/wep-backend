@Grab('com.github.lookfirst:sardine:5.4')
@Grab('org.slf4j:slf4j-nop:1.7.5')
import com.github.sardine.*

if (args.length < 2) {
        println "groovy webdav <org> <folder>"
        return
}

def start = new Date ()

def org = args[0]
def folder = args[1]
def sardine = SardineFactory.begin()
def url = "http://${org}:92651@${org}.cust.newgenesys.com/fs/${folder}/"

println "getting documents ${folder} at ${org}"
sardine.list(url).each {
        println "$it: ${it.dump()}"
}

use (groovy.time.TimeCategory) {
        def duration = new Date() - start
        println "Done in ${duration}"
}

