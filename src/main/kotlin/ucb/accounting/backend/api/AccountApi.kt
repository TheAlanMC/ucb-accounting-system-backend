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
import ucb.accounting.backend.bl.AccountBl
import ucb.accounting.backend.dto.AccountPartialDto
import ucb.accounting.backend.dto.ResponseDto
import ucb.accounting.backend.util.ResponseCodeUtil

@RestController
@RequestMapping("/api/v1/accounts")
class AccountApi @Autowired constructor(
    private val accountBl: AccountBl,
    ){

    companion object {
        private val logger = LoggerFactory.getLogger(AccountApi::class.java.name)
    }

    @PostMapping("/companies/{companyId}")
    fun createCompanyAccount(
        @PathVariable companyId: Long,
        @RequestBody accountPartialDto: AccountPartialDto
    ) : ResponseEntity<ResponseDto<Nothing>>{
        logger.info("Starting the API call to post an account for a company")
        logger.info("POST /api/v1/accoubts/companies/$companyId")
        accountBl.createCompanyAccount(companyId, accountPartialDto)
        logger.info("Sending response")
        val code = "201-07"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, null), responseInfo.httpStatus)
    }

    @GetMapping("/companies/{companyId}")
    fun getCompanyAccounts(
        @PathVariable companyId: Long,
    ) : ResponseEntity<ResponseDto<List<AccountPartialDto>>> {
        logger.info("Starting the API call to get company accounts")
        logger.info("GET /api/v1/accounts/companies/$companyId")
        val accounts = accountBl.getCompanyAccounts(companyId)
        logger.info("Sending response")
        val code = "200-12"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, accounts), responseInfo.httpStatus)
    }


    @GetMapping("/{accountId}/companies/{companyId}")
    fun getCompanyAccount(
        @PathVariable companyId: Long,
        @PathVariable accountId: Long,
    ) : ResponseEntity<ResponseDto<AccountPartialDto>> {
        logger.info("Starting the API call to get an account for a company")
        logger.info("GET /api/v1/accounts/$accountId/companies/$companyId")
        val account = accountBl.getCompanyAccount(companyId, accountId)
        logger.info("Sending response")
        val code = "200-12"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, account), responseInfo.httpStatus)
    }


    @PutMapping("/{accountId}/companies/{companyId}")
    fun updateCompanyAccount(
        @PathVariable companyId: Long,
        @PathVariable accountId: Long,
        @RequestBody accountPartialDto: AccountPartialDto,
    ) : ResponseEntity<ResponseDto<AccountPartialDto>>{
        logger.info("Starting the API call to update an account for a company")
        logger.info("PUT /api/v1/accoubts/companies/$companyId")
        val newAccount = accountBl.updateCompanyAccount(companyId, accountId, accountPartialDto)
        logger.info("Sending response")
        val code = "200-13"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, newAccount), responseInfo.httpStatus)
    }
}