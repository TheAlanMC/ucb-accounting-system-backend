package ucb.accounting.backend.api

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ucb.accounting.backend.bl.CompanyBl
import ucb.accounting.backend.dto.CompanyDto
import ucb.accounting.backend.dto.ResponseDto
import ucb.accounting.backend.util.ResponseCodeUtil
import java.util.logging.Logger
import javax.validation.constraints.Null

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

    @PostMapping()
    fun postCompanyInfo(
        @RequestBody companyDto: CompanyDto
    ) : ResponseEntity<ResponseDto<Null>>{
        logger.info("Starting the API call to post company info")
        logger.info("POST /api/v1/companies")
        companyBl.createCompany(companyDto)
        val code = "201-04"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, null), responseInfo.httpStatus)
    }

    @PutMapping("/{companyId}")
    fun putCompanyInfo(
        @RequestParam("companyId") companyId: Long,
        @RequestBody companyDto: CompanyDto,
    ) : ResponseEntity<ResponseDto<CompanyDto>>{
        logger.info("Starting the API call to put company info")
        logger.info("PUT /api/v1/companies/{companyId}")
        val updatedCompany = companyBl.updateCompany(companyDto, companyId)
        val code = "200-06"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, updatedCompany), responseInfo.httpStatus)
    }
}