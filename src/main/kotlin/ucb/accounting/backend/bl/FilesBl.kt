package ucb.accounting.backend.bl

import org.keycloak.admin.client.Keycloak
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.multipart.MultipartFile
import ucb.accounting.backend.dao.Attachment
import ucb.accounting.backend.dao.repository.AttachmentRepository
import ucb.accounting.backend.dao.repository.CompanyRepository
import ucb.accounting.backend.dto.AttachmentDto
import ucb.accounting.backend.exception.UasException

@Controller
class FilesBl @Autowired constructor(
    private val attachmentRepository: AttachmentRepository,
    private val companyRepository: CompanyRepository
){
    companion object {
        val logger: Logger = LoggerFactory.getLogger(FilesBl::class.java)
    }

    fun uploadFile(attachment: MultipartFile, companyId: Long): AttachmentDto {
        // Validation
        companyRepository.findByCompanyIdAndStatusTrue(companyId) ?: throw UasException("404-05")
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
}