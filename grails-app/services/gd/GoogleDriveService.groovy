package gd

import com.google.api.client.http.ByteArrayContent
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.Permission
import org.springframework.web.multipart.MultipartFile

class GoogleDriveService {

	Drive driveService

	List<File> listFiles() {

		def files = driveService.files().list().execute()

		files.getItems()
	}

	File uploadFile(MultipartFile multipartFile) {

		def info = new File()
			.setTitle(multipartFile.originalFilename)
			.setDescription(multipartFile.originalFilename)
			.setMimeType(multipartFile.contentType)

		def content = new ByteArrayContent(multipartFile.contentType, multipartFile.bytes)

		driveService.files().insert(info, content).execute()
	}

	def insertPermission(String fileId, String role, String type) {

		def permission = new Permission()
				.setRole(role)
				.setType(type)

		driveService.permissions().insert(fileId, permission).execute()
	}

	def remove(String id) {

		driveService.files().delete(id).execute()
	}
}
