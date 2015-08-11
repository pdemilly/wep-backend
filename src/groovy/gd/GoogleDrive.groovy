package gd

/***
@GrabResolver(name='com.google.apis', root='http://google-api-client-libraries.appspot.com/mavenrepo')
@Grapes([
    @Grab(group='com.google.http-client',    module='google-http-client-jackson',    version='1.12.0-beta'),
    @Grab(group='com.google.api-client',    module='google-api-client',        version='1.12.0-beta'),
    @Grab(group='com.google.apis',        module='google-api-services-drive',    version='v2-rev30-1.12.0-beta'),
	@Grab(group='commons-io', module='commons-io', version='2.4'),
])
**/

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse

import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson.JacksonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import groovy.json.*
import crs.repo.googledrive.*
import com.google.api.client.http.FileContent
import com.google.api.services.drive.model.*

import org.apache.commons.io.FileUtils
import org.apache.commons.codec.digest.DigestUtils

class GoogleDrive {

	def config
	def drive
	def portfolio

	GoogleDrive () {

		config = [
		    httpTransport: new NetHttpTransport (),
		    jsonFactory: new JacksonFactory (),
		    accountId: '926630763341-mktjnvhjbmlhshplkqkli86fbv0kleeu@developer.gserviceaccount.com',
		    accountUser: 'studio@garreaudesigns.com',
		    scopes: DriveScopes.DRIVE,
		    p12File: 'gds/gds-sync-764925db9768.p12',
			// p12File: new URI ("data:text/plain;base64,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")
		]

		drive = new GoogleDriveServiceFactory ().createDrive(config)

		portfolio = drive.files().list().setQ("mimeType = 'application/vnd.google-apps.folder'").execute().getItems()[0]
	}

	List<File> listFiles() {

		def list = []
		def request = drive.files().list()

		while (true) {
			def files = request.execute()
			list += files.getItems()
			request.pageToken = files.nextPageToken

			if (!request.pageToken)
				break
		}

		return list
	}

	List<File> filter (s) {
		def list = []
		def request = drive.files().list().setQ (s)

		while (true) {
			def files = request.execute()
			list += files.getItems()
			request.pageToken = files.nextPageToken

			if (!request.pageToken)
				break
		}

		return list
	}

	def forEach (String s, Closure c) {
		def request = drive.files().list()

		while (true) {
			def files = request.execute()
			files.getItems().each { f ->
				c.call(f)
			}
			request.pageToken = files.nextPageToken

			if (!request.pageToken)
				break
		}
	}

	String uploadFileToPortfolio (java.io.File content) {

	    def gdsfile

	    if (!content.exists())
	    	return null

	    def file = new File ()

	    def existings = filter ("title = '${encode(content.name)}'")

	    if (existings.size () == 0) {
	        file.title = content.name
	    	file.description = content.canonicalPath
	    	file.mimeType = getMimeType (content)
	    	file.parents = [ new ParentReference().setId(portfolio.id) ] 
	    	def media = new FileContent (getMimeType (content), content)
	    	gdsfile = drive.files().insert (file, media).execute()
	    }
	    else {
	        def md5 = DigestUtils.md5Hex (content.newDataInputStream())
	    	gdsfile = existings.find { it.description == content.canonicalPath || it.md5Checksum == md5 }
		if (!gdsfile) {
			file.title = content.name
			file.description = content.canonicalPath
			file.mimeType = getMimeType (content)
			file.parents = [ new ParentReference().setId(portfolio.id) ] 
			def media = new FileContent (getMimeType (content), content)
			gdsfile = drive.files().insert (file, media).execute()
		}
		print "* "
	    }

	    return gdsfile?.id
	}

	String uploadFileToPortfolio (String path) {
		return uploadFileToPortfolio (new java.io.File (path))
	}

	File getFile (java.io.File file, String md5 = null) {
		def gdsfile = null
		def existings = filter ("title = '${encode(file.name)}'")
		if (existings.size () > 0) {
			gdsfile = existings.find { md5 == it.md5Checksum || it.description == file.canonicalPath }
		}
		return gdsfile
	}

	def getFile (String id) {
		return drive.files().get (id).execute()
	}

	def patch (File f) {
		return drive.files().patch (f.id, f).execute()
	}

	private def getExtensionFromFilename(filename) {
		def returned_value = ""
		def m = (filename =~ /(\.[^\.]*)$/)
		if (m.size()>0) returned_value = ((m[0][0].size()>0) ? m[0][0].substring(1).trim().toLowerCase() : "");
		return returned_value.toLowerCase()
	}

	private def getMimeType (file) {
		switch (getExtensionFromFilename (file.name)) {
			case 'psd':
				return 'application/photoshop'
			case 'ai':
				return 'application/illustrator'
			default:
				return URLConnection.guessContentTypeFromName(file.name)
		}
	}

	private String encode (s) {
		return s.replace ("'", "\\'")
	}


}


/********************************************************************

public class DriveCommandLine {

	def CLIENT_ID = "YOUR_CLIENT_ID";
	def CLIENT_SECRET = "YOUR_CLIENT_SECRET";
	def httpTransport
	def jsonFactory
    
	def REDIRECT_URI = "urn:ietf:wg:oauth:2.0:oob";
  
	public DriveCommandLine (Map config) {
		CLIENT_ID = config.accountId
		CLIENT_SECRET= config.p12File
		httpTransport = config.httpTrasnport
		jsonFactory = config.jsonFactory
	}
  
	public test() throws IOException {
    
		def flow = new GoogleAuthorizationCodeFlow.Builder(
			httpTransport, jsonFactory, CLIENT_ID, CLIENT_SECRET, Arrays.asList(DriveScopes.DRIVE))
			.setAccessType("online")
			.setApprovalPrompt("auto").build();
    
		def url = flow.newAuthorizationUrl().setRedirectUri(REDIRECT_URI).build();
		System.out.println("Please open the following URL in your browser then type the authorization code:");
		System.out.println("  " + url);
		def br = new BufferedReader(new InputStreamReader(System.in));
		def code = br.readLine();
		
		def response = flow.newTokenRequest(code).setRedirectUri(REDIRECT_URI).execute();
		def credential = new GoogleCredential().setFromTokenResponse(response);
		
		//Create a new authorized API client
		def service = new Drive.Builder(httpTransport, jsonFactory, credential).build();

		//Insert a file  
		def body = new File();
		body.setTitle("My document");
		body.setDescription("A test document");
		body.setMimeType("text/plain");
		
		def fileContent = new java.io.File("document.txt");
		def mediaContent = new FileContent("text/plain", fileContent);

		def file = service.files().insert(body, mediaContent).execute();
		System.out.println("File ID: " + file.getId());
	}
}

********************************************************************/
