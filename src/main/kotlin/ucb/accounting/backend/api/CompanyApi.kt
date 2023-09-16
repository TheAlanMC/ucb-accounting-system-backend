package ucb.accounting.backend.api

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ucb.accounting.backend.bl.CompanyBl
import ucb.accounting.backend.dto.CompanyDto
import ucb.accounting.backend.dto.ResponseDto
import ucb.accounting.backend.util.ResponseCodeUtil
import java.util.logging.Logger

@RestController
@RequestMapping("/api/v1/companies")
class CompanyApi @Autowired constructor(private val companyBl: CompanyBl){

    companion object {
        private val logger = LoggerFactory.getLogger(CompanyApi::class.java.name)
    }

    @GetMapping("/{companyId}")
    fun getCompanyInfo(
        @PathVariable("companyId") companyId: Long,
    ) : ResponseEntity<ResponseDto<CompanyDto>>{
        logger.info("Starting the API call to get company info")
        logger.info("GET /api/v1/companies/{companyId}")
        val companyInfo = companyBl.getCompanyInfo(companyId)
        val code = "200-05"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, companyInfo), responseInfo.httpStatus)
    }

}