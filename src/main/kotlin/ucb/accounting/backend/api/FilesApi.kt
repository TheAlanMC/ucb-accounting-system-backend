package ucb.accounting.backend.api

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import ucb.accounting.backend.bl.FilesBl
import ucb.accounting.backend.dto.AttachmentDto
import ucb.accounting.backend.dto.FileDto
import ucb.accounting.backend.dto.ResponseDto
import ucb.accounting.backend.util.ResponseCodeUtil

@RestController
@RequestMapping("/api/v1/files")
class FilesApi @Autowired constructor(private val filesBl: FilesBl) {
    companion object {
        private val logger = LoggerFactory.getLogger(FilesApi::class.java.name)
    }

    @PostMapping("attachments/companies/{companyId}")
    fun uploadAttachment(
        @PathVariable("companyId") companyId: Long,
        @RequestParam("attachment") attachment: MultipartFile,
    ) : ResponseEntity<ResponseDto<AttachmentDto>>{
        logger.info("Starting the API call to upload attachment")
        logger.info("POST /api/v1/files/attachments/companies/$companyId")
        val attachmentDto = filesBl.uploadFile(attachment, companyId)
        logger.info("Sending response")
        val code = "200-16"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, attachmentDto), responseInfo.httpStatus)
    }

    @PostMapping("pictures")
    fun uploadPicture(
        @RequestParam("picture") picture: MultipartFile,
    ) : ResponseEntity<ResponseDto<FileDto>>{
        logger.info("Starting the API call to upload picture")
        logger.info("POST /api/v1/files/pictures")
        val attachmentDto = filesBl.uploadPicture(picture)
        logger.info("Sending response")
        val code = "200-18"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, attachmentDto), responseInfo.httpStatus)
    }
}