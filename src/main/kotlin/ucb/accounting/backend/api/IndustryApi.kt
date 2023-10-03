package ucb.accounting.backend.api

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ucb.accounting.backend.bl.IndustryBl
import ucb.accounting.backend.dto.IndustryDto
import ucb.accounting.backend.dto.ResponseDto
import ucb.accounting.backend.util.ResponseCodeUtil

@RestController
@RequestMapping("/api/v1/industries")
class IndustryApi @Autowired constructor(private val industryBl: IndustryBl)
{

    companion object {
        private val logger = LoggerFactory.getLogger(IndustryApi::class.java.name)
    }

    @GetMapping
    fun getIndustries(): ResponseEntity<ResponseDto<List<IndustryDto>>> {
        logger.info("Starting the API call to get industries")
        logger.info("GET /api/v1/industries")
        val industries: List<IndustryDto> = industryBl.getIndustries()
        logger.info("Sending response")
        val code = "200-03"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, industries), responseInfo.httpStatus)
    }
}
