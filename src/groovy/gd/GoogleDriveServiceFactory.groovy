package gd

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse

import com.google.api.client.http.HttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.services.drive.Drive

class GoogleDriveServiceFactory {

	private static String REDIRECT_URI = "urn:ietf:wg:oauth:2.0:oob"

	Drive createDrive(HttpTransport httpTransport, JsonFactory jsonFactory, String accountId, String scopes, String p12File) {

		def credential = new GoogleCredential.Builder().setTransport(httpTransport)
				.setJsonFactory(jsonFactory)
				.setServiceAccountId(accountId)
				.setServiceAccountScopes(scopes)
				.setServiceAccountPrivateKeyFromP12File(new File(p12File))
				.build();

		credential.refreshToken();

		return new Drive.Builder(httpTransport, jsonFactory, credential).build()
	}

        Drive createDrive (Map config) {
                def credential = buildCredential (config)
                return new Drive.Builder(config.httpTransport, config.jsonFactory, credential).build()
        }

        private GoogleCredential buildCredential (Map config) {
                def credential = new GoogleCredential.Builder().setTransport(config.httpTransport)
                                .setJsonFactory(config.jsonFactory)
                                .setServiceAccountId(config.accountId)
                                .setServiceAccountScopes(config.scopes)
                                .setServiceAccountPrivateKeyFromP12File(new File(config.p12File))
                                .setServiceAccountUser (config.accountUser)
                                .build();

                credential.refreshToken();
                return credential
        }
}
