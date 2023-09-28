package ucb.accounting.backend.api

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ucb.accounting.backend.bl.BusinessEntityBl
import ucb.accounting.backend.dto.BusinessEntityDto
import ucb.accounting.backend.dto.ResponseDto
import ucb.accounting.backend.util.ResponseCodeUtil

@RestController
@RequestMapping("/api/v1/business-entities")
class BusinessEntityApi @Autowired constructor(private val businessEntityBl: BusinessEntityBl)
{

    companion object {
        private val logger = LoggerFactory.getLogger(BusinessEntityApi::class.java.name)
    }

    @GetMapping
    fun getBusinessEntities(): ResponseEntity<ResponseDto<List<BusinessEntityDto>>> {
        logger.info("Starting the API call to get business entities")
        logger.info("GET /api/v1/business-entities")
        val businessEntities: List<BusinessEntityDto> = businessEntityBl.getBusinessEntities()
        logger.info("Sending response")
        val code = "200-04"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, businessEntities), responseInfo.httpStatus)
    }
}
