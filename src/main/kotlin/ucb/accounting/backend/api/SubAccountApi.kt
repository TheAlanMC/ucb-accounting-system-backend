package ucb.accounting.backend.api

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ucb.accounting.backend.bl.SubAccountBl
import ucb.accounting.backend.dto.InsertSubAccountDto
import ucb.accounting.backend.dto.ResponseDto
import ucb.accounting.backend.dto.SubAccountDto
import ucb.accounting.backend.util.ResponseCodeUtil
import javax.validation.constraints.Null

@RestController
@RequestMapping("/api/v1/subaccounts/companies")
class SubAccountApi @Autowired constructor(
    private val subAccountBl: SubAccountBl,
){

    companion object {
        private val logger = LoggerFactory.getLogger(SubAccountApi::class.java.name)
    }

    @PostMapping("/{companyId}")
    fun postCompanySubaccount(
        @PathVariable("companyId") companyId: Long,
        @RequestBody subAccountDto: InsertSubAccountDto
    ) : ResponseEntity<ResponseDto<Null>>{
        logger.info("Starting the API call to post a subaccount for a company")
        logger.info("POST /api/v1/subaccounts/companies/$companyId")

        subAccountBl.createSubAccount(companyId, subAccountDto)

        val code = "201-08"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, null), responseInfo.httpStatus)
    }

    @GetMapping("/{companyId}")
    fun getCompanySubaccounts(
        @PathVariable("companyId") companyId: Long,
    ) : ResponseEntity<ResponseDto<List<InsertSubAccountDto>>> {
        logger.info("Starting the API call to get company subaccounts")
        logger.info("GET /api/v1/subaccounts/companies/$companyId")

        val subaccounts = subAccountBl.getCompanySubAccounts(companyId)

        val code = "200-13"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, subaccounts), responseInfo.httpStatus)
    }

    @PutMapping("/{companyId}")
    fun putCompanySubaccount(
        @PathVariable("companyId") companyId: Long,
        @RequestBody subAccountDto: InsertSubAccountDto
    ) : ResponseEntity<ResponseDto<Null>>{
        logger.info("Starting the API call to put a subaccount for a company")
        logger.info("PUT /api/v1/subaccounts/companies/$companyId")

        subAccountBl.updateSubAccount(companyId, subAccountDto)

        val code = "200-14"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, null), responseInfo.httpStatus)
    }


}