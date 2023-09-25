package ucb.accounting.backend.api

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ucb.accounting.backend.bl.AccountingPlanBl
import ucb.accounting.backend.dto.*
import ucb.accounting.backend.util.ResponseCodeUtil

@RestController
@RequestMapping("/api/v1/accounting-plans/companies")
class AccountPlanApi @Autowired constructor(private val accountingPlanBl: AccountingPlanBl){

    companion object {
        private val logger = LoggerFactory.getLogger(AccountPlanApi::class.java.name)
    }

    @GetMapping("/{companyId}")
    fun getAccountingPlan(@PathVariable companyId: Long): ResponseEntity<ResponseDto<List<AccountCategoryDto>>> {
        logger.info("Starting the API call to get accounting plan")
        logger.info("GET /api/v1/accounting-plans/companies/${companyId}")
        val accountingPlan: List<AccountCategoryDto> = accountingPlanBl.getAccountingPlan(companyId)
        logger.info("Sending response")
        val code = "200-07"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, accountingPlan), responseInfo.httpStatus)
    }

}