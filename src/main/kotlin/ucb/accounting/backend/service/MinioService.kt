package ucb.accounting.backend.service

import io.minio.GetPresignedObjectUrlArgs
import io.minio.MinioClient
import io.minio.PutObjectArgs
import io.minio.http.Method
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import ucb.accounting.backend.dto.NewFileDto
import java.util.*

@Service
class MinioService @Autowired constructor(
    private val minioClient: MinioClient
) {

    fun uploadFile(file: MultipartFile, bucket: String): NewFileDto {
        // file name
        val filename =  "${UUID.randomUUID()}.${file.originalFilename!!.split(".").last()}"
        // save object
        minioClient.putObject(
            PutObjectArgs.builder()
                .bucket(bucket)
                .`object`(filename)
                .stream(file.inputStream, file.size, -1)
                .contentType(file.contentType)
                .build()
        )
        val url = getPreSignedUrl(bucket, filename)
        return NewFileDto(filename, bucket, file.contentType ?: "application/octet-stream", url)
    }

    fun getPreSignedUrl(bucket: String, filename: String): String {
        return minioClient.getPresignedObjectUrl(
            GetPresignedObjectUrlArgs.builder()
                .method(Method.GET)
                .bucket(bucket)
                .`object`(filename)
                .expiry(60 * 60 * 24 ) // 1 day
                .build()
        )
    }

    fun uploadTempFile(fileData: ByteArray, filename: String, contentType: String, bucket: String): NewFileDto {
        // file name
        val newFilename =  "${UUID.randomUUID()}.${filename.split(".").last()}"
        // save object
        minioClient.putObject(
            PutObjectArgs.builder()
                .bucket(bucket)
                .`object`(newFilename)
                .stream(fileData.inputStream(), fileData.size.toLong(), -1)
                .contentType(contentType)
                .build()
        )
        val url = getPreSignedUrl(bucket, newFilename)
        return NewFileDto(newFilename, bucket, contentType, url)
    }
}