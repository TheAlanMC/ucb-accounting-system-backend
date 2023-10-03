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
import org.springframework.web.bind.annotation.RestController
import ucb.accounting.backend.bl.AccountSubgroupBl
import ucb.accounting.backend.dto.AccountSubgroupPartialDto
import ucb.accounting.backend.dto.ResponseDto
import ucb.accounting.backend.util.ResponseCodeUtil

@RestController
@RequestMapping("/api/v1/account-subgroups")
class AccountSubgroupApi @Autowired constructor(private val accountSubgroupBl: AccountSubgroupBl){

    companion object {
        private val logger = LoggerFactory.getLogger(AccountSubgroupApi::class.java.name)
    }

    @PostMapping("/companies/{companyId}")
    fun createAccountSubgroup(
        @PathVariable companyId: Long,
        @RequestBody accountSubgroupPartialDto: AccountSubgroupPartialDto
    ): ResponseEntity<ResponseDto<Nothing>> {
        logger.info("Starting the API call to create account sub group")
        logger.info("POST /api/v1/account-subgroups/companies/${companyId}")
        accountSubgroupBl.createAccountSubgroup(companyId, accountSubgroupPartialDto)
        logger.info("Sending response")
        val code = "201-06"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, null), responseInfo.httpStatus)
    }

    @GetMapping("/companies/{companyId}")
    fun getAccountSubgroups(@PathVariable companyId: Long): ResponseEntity<ResponseDto<List<AccountSubgroupPartialDto>>>{
        logger.info("Starting the API call to get account sub groups")
        logger.info("GET /api/v1/account-subgroups/companies/${companyId}")
        val accountSubgroups = accountSubgroupBl.getAccountSubgroups(companyId)
        logger.info("Sending response")
        val code = "200-10"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, accountSubgroups), responseInfo.httpStatus)
    }

    @GetMapping("/{accountSubgroupId}/companies/{companyId}")
    fun getAccountSubgroup(
        @PathVariable companyId: Long,
        @PathVariable accountSubgroupId: Long
    ): ResponseEntity<ResponseDto<AccountSubgroupPartialDto>>{
        logger.info("Starting the API call to get account sub group")
        logger.info("GET /api/v1/account-subgroups/${accountSubgroupId}/companies/${companyId}")
        val accountSubgroup = accountSubgroupBl.getAccountSubgroup(companyId, accountSubgroupId)
        logger.info("Sending response")
        val code = "200-10"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, accountSubgroup), responseInfo.httpStatus)
    }

    @PutMapping("/{accountSubgroupId}/companies/{companyId}")
    fun updateAccountSubgroup(
        @PathVariable companyId: Long,
        @PathVariable accountSubgroupId: Long,
        @RequestBody accountSubgroupPartialDto: AccountSubgroupPartialDto
    ): ResponseEntity<ResponseDto<AccountSubgroupPartialDto>>{
        logger.info("Starting the API call to update account sub group")
        logger.info("PUT /api/v1/account-subgroups/${accountSubgroupId}/companies/${companyId}")
        val newAccountSubgroup = accountSubgroupBl.updateAccountSubgroup(companyId, accountSubgroupId, accountSubgroupPartialDto)
        logger.info("Sending response")
        val code = "200-11"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, newAccountSubgroup), responseInfo.httpStatus)
    }

}