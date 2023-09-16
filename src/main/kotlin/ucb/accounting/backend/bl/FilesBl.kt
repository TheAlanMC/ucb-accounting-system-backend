package ucb.accounting.backend.bl

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import ucb.accounting.backend.dao.Attachment
import ucb.accounting.backend.dao.S3Object
import ucb.accounting.backend.dao.repository.AttachmentRepository
import ucb.accounting.backend.dao.repository.CompanyRepository
import ucb.accounting.backend.dao.repository.KcUserCompanyRepository
import ucb.accounting.backend.dao.repository.S3ObjectRepository
import ucb.accounting.backend.dto.AttachmentDownloadDto
import ucb.accounting.backend.dto.AttachmentDto
import ucb.accounting.backend.dto.FileDto
import ucb.accounting.backend.exception.UasException
import ucb.accounting.backend.service.MinioService
import ucb.accounting.backend.util.KeycloakSecurityContextHolder

@Service
class FilesBl @Autowired constructor(
    private val attachmentRepository: AttachmentRepository,
    private val s3ObjectRepository: S3ObjectRepository,
    private val companyRepository: CompanyRepository,
    private val kcUserCompanyRepository: KcUserCompanyRepository,
    private val minioService: MinioService
){
    companion object {
        val logger: Logger = LoggerFactory.getLogger(FilesBl::class.java)
    }

    fun uploadFile(attachment: MultipartFile, companyId: Long): AttachmentDto {
        // Validation of company
        companyRepository.findByCompanyIdAndStatusTrue(companyId) ?: throw UasException("404-05")
        // Validation of user belongs to company
        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId) ?: throw UasException("403-18")
        logger.info("User $kcUuid is uploading file to company $companyId")
        // Upload to database as blob
        val attachmentEntity = Attachment()
        attachmentEntity.companyId = companyId.toInt()
        attachmentEntity.fileData = attachment.bytes
        attachmentEntity.filename = attachment.originalFilename!!
        attachmentEntity.contentType = attachment.contentType!!
        logger.info("Uploading file to database")
        val savedAttachment = attachmentRepository.save(attachmentEntity)
        logger.info("File uploaded to database")
        return AttachmentDto(
            attachmentId = savedAttachment.attachmentId,
            filename = savedAttachment.filename,
            contentType = savedAttachment.contentType
        )
    }

    fun uploadPicture (picture: MultipartFile): FileDto {
        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        logger.info("User $kcUuid is uploading picture")
        val bucket = "pictures"
        val newFile = minioService.uploadFile(picture, bucket)
        logger.info("Storing file metadata in database")
        val s3Object = S3Object()
        s3Object.filename = newFile.filename
        s3Object.bucket = newFile.bucket
        s3Object.contentType = newFile.contentType
        val savedS3Object = s3ObjectRepository.save(s3Object)
        return FileDto(
            s3ObjectId = savedS3Object.s3ObjectId,
            contentType = savedS3Object.contentType,
            bucket = savedS3Object.bucket,
            filename = savedS3Object.filename,
            fileUrl = newFile.fileUrl,
        )
    }

    fun downloadFile(attachmentId: Long, companyId: Long): AttachmentDownloadDto {
        // Validation of company
        companyRepository.findByCompanyIdAndStatusTrue(companyId) ?: throw UasException("404-05")
        // Validation of attachment
        val attachmentEntity: Attachment = attachmentRepository.findByAttachmentIdAndStatusTrue(attachmentId) ?: throw UasException("404-11")
        // Validation of user belongs to company
        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId) ?: throw UasException("403-19")
        // Validate the attachment belongs to the company
        if (attachmentEntity.companyId != companyId.toInt()) {
            throw UasException("403-19")
        }
        logger.info("User $kcUuid is downloading file from company $companyId")
        logger.info("File found in database")
        // Byte array to multipart file
        val bucket = "documents"
        val newFile = minioService.uploadTempFile(attachmentEntity.fileData, attachmentEntity.filename, attachmentEntity.contentType, bucket)
        logger.info("File uploaded to minio")
        return AttachmentDownloadDto(
            filename = attachmentEntity.filename,
            contentType = attachmentEntity.contentType,
            fileUrl = newFile.fileUrl,
        )
    }
}