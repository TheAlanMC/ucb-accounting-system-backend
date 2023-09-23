package ucb.accounting.backend.api

import jakarta.ws.rs.Path
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
import ucb.accounting.backend.dto.ReducedAccountDto
import ucb.accounting.backend.dto.ResponseDto
import ucb.accounting.backend.exception.UasException
import ucb.accounting.backend.util.ResponseCodeUtil
import javax.validation.constraints.Null

@RestController
@RequestMapping("/api/v1/accounts/companies")
class AccountApi @Autowired constructor(
    private val accountBl: AccountBl,
    ){

    companion object {
        private val logger = LoggerFactory.getLogger(AccountApi::class.java.name)
    }

    @GetMapping("/{companyId}")
    fun getComapnyAccounts(
        @PathVariable("companyId") companyId: Long,
    ) : ResponseEntity<ResponseDto<List<ReducedAccountDto>>> {
        logger.info("Starting the API call to get company accounts")
        logger.info("GET /api/v1/accounts/companies/$companyId")

        val accounts = accountBl.getCompanyAccounts(companyId)

        val code = "200-12"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, accounts), responseInfo.httpStatus)
    }

    @PostMapping("/{companyId}")
    fun postCompnyAccount(
        @PathVariable("companyId") companyId: Long,
        @RequestBody accountDto: ReducedAccountDto
    ) : ResponseEntity<ResponseDto<Null>>{
        logger.info("Starting the API call to post an account for a company")
        logger.info("POST /api/v1/accoubts/companies/$companyId")

        accountBl.createAccount(companyId, accountDto)

        val code = "201-07"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, null), responseInfo.httpStatus)
    }

    @PutMapping("/{companyId}")
    fun updateAccount(
        @PathVariable("companyId") companyId: Long,
        @RequestBody accountDto: ReducedAccountDto
    ) : ResponseEntity<ResponseDto<Null>>{
        logger.info("Starting the API call to update an account for a company")
        logger.info("PUT /api/v1/accoubts/companies/$companyId")

        accountBl.updateAccount(companyId, accountDto)

        val code = "200-13"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, null), responseInfo.httpStatus)
    }
}