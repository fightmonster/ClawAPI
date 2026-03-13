package com.claw.api.data.drive

import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class DriveFile(
    val id: String,
    val name: String,
    val mimeType: String,
    val size: Long?,
    val modifiedTime: String?,
    val webViewLink: String?
)

class DriveRepository(private val driveService: Drive) {
    
    suspend fun listFiles(pageSize: Int = 50): Result<List<DriveFile>> = withContext(Dispatchers.IO) {
        try {
            val result = driveService.files().list()
                .setPageSize(pageSize)
                .setFields("files(id, name, mimeType, size, modifiedTime, webViewLink)")
                .setOrderBy("modifiedTime desc")
                .execute()
            
            val files = result.files.map { file ->
                DriveFile(
                    id = file.id,
                    name = file.name,
                    mimeType = file.mimeType,
                    size = file.getSize(),
                    modifiedTime = file.modifiedTime?.toString(),
                    webViewLink = file.webViewLink
                )
            }
            
            Result.success(files)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun searchFiles(query: String): Result<List<DriveFile>> = withContext(Dispatchers.IO) {
        try {
            val result = driveService.files().list()
                .setQ("name contains '$query'")
                .setFields("files(id, name, mimeType, size, modifiedTime, webViewLink)")
                .execute()
            
            val files = result.files.map { file ->
                DriveFile(
                    id = file.id,
                    name = file.name,
                    mimeType = file.mimeType,
                    size = file.getSize(),
                    modifiedTime = file.modifiedTime?.toString(),
                    webViewLink = file.webViewLink
                )
            }
            
            Result.success(files)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getFileContent(fileId: String): Result<ByteArray> = withContext(Dispatchers.IO) {
        try {
            val outputStream = java.io.ByteArrayOutputStream()
            driveService.files().get(fileId).executeMediaAndDownloadTo(outputStream)
            Result.success(outputStream.toByteArray())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
