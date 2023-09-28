package ucb.accounting.backend.api

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ucb.accounting.backend.bl.DocumentTypeBl
import ucb.accounting.backend.dto.DocumentTypeDto
import ucb.accounting.backend.dto.ResponseDto
import ucb.accounting.backend.util.ResponseCodeUtil

@RestController
@RequestMapping("/api/v1/document-types")
class DocumentTypeApi @Autowired constructor(private val documentTypeBl: DocumentTypeBl){

    companion object {
        private val logger = LoggerFactory.getLogger(DocumentTypeApi::class.java.name)
    }

    @GetMapping
    fun getDocumentTypes(): ResponseEntity<ResponseDto<List<DocumentTypeDto>>> {
        logger.info("Starting the API call to get document types")
        logger.info("GET /api/v1/document-types")
        val documentTypes: List<DocumentTypeDto> = documentTypeBl.getDocumentTypes()
        logger.info("Sending response")
        val code = "200-20"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, documentTypes), responseInfo.httpStatus)
    }
}
