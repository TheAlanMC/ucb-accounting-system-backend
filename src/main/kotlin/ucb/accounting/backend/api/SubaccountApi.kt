package ucb.accounting.backend.api

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ucb.accounting.backend.bl.SubaccountBl
import ucb.accounting.backend.dto.SubaccountPartialDto
import ucb.accounting.backend.dto.ResponseDto
import ucb.accounting.backend.util.ResponseCodeUtil

@RestController
@RequestMapping("/api/v1/subaccounts")
class SubaccountApi @Autowired constructor(
    private val subaccountBl: SubaccountBl,
){

    companion object {
        private val logger = LoggerFactory.getLogger(SubaccountApi::class.java.name)
    }

    @PostMapping("/companies/{companyId}")
    fun createCompanySubaccount(
        @PathVariable companyId: Long,
        @RequestBody subaccountPartialDto: SubaccountPartialDto
    ) : ResponseEntity<ResponseDto<Nothing>>{
        logger.info("Starting the API call to post a subaccount for a company")
        logger.info("POST /api/v1/subaccounts/companies/$companyId")
        subaccountBl.createCompanySubaccount(companyId, subaccountPartialDto)
        logger.info("Sending response")
        val code = "201-08"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, null), responseInfo.httpStatus)
    }

    @GetMapping("/companies/{companyId}")
    fun getCompanySubaccounts(
        @PathVariable companyId: Long,
    ) : ResponseEntity<ResponseDto<List<SubaccountPartialDto>>> {
        logger.info("Starting the API call to get company subaccounts")
        logger.info("GET /api/v1/subaccounts/companies/$companyId")
        val subaccounts = subaccountBl.getCompanySubaccounts(companyId)
        logger.info("Sending response")
        val code = "200-14"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, subaccounts), responseInfo.httpStatus)
    }

    @GetMapping("/{subaccountId}/companies/{companyId}")
    fun getCompanySubaccount(
        @PathVariable companyId: Long,
        @PathVariable subaccountId: Long
    ) : ResponseEntity<ResponseDto<SubaccountPartialDto>> {
        logger.info("Starting the API call to get a company subaccount")
        logger.info("GET /api/v1/subaccounts/$subaccountId/companies/$companyId")
        val subaccount = subaccountBl.getCompanySubaccount(companyId, subaccountId)
        logger.info("Sending response")
        val code = "200-14"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, subaccount), responseInfo.httpStatus)
    }

    @PutMapping("/{subaccountId}/companies/{companyId}")
    fun updateCompanySubaccount(
        @PathVariable companyId: Long,
        @PathVariable subaccountId: Long,
        @RequestBody subaccountPartialDto: SubaccountPartialDto
    ) : ResponseEntity<ResponseDto<SubaccountPartialDto>>{
        logger.info("Starting the API call to put a subaccount for a company")
        logger.info("PUT /api/v1/subaccounts/$subaccountId/companies/$companyId")
        val newSubaccount = subaccountBl.updateSubaccount(companyId, subaccountId, subaccountPartialDto)
        logger.info("Sending response")
        val code = "200-15"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, newSubaccount), responseInfo.httpStatus)
    }


}