package ucb.accounting.backend.api

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ucb.accounting.backend.bl.PartnerBl
import ucb.accounting.backend.dto.PartnerDto
import ucb.accounting.backend.dto.ResponseDto
import ucb.accounting.backend.util.ResponseCodeUtil

@RestController
@RequestMapping("/api/v1/partners")
class PartnerApi @Autowired constructor(private val partnerBl: PartnerBl) {

    companion object {
        private val logger = LoggerFactory.getLogger(DocumentTypeApi::class.java.name)
    }

    @GetMapping("/companies/{companyId}")
    fun getPartners(
        @PathVariable("companyId") companyId: Long
    ): ResponseEntity<ResponseDto<PartnerDto>> {
        logger.info("Starting the API call to get partners")
        logger.info("GET /api/v1/partners/companies/${companyId}")
        val partners: PartnerDto = partnerBl.getPartners(companyId)
        logger.info("Sending response")
        val code = "200-34"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, partners), responseInfo.httpStatus)
    }
}
