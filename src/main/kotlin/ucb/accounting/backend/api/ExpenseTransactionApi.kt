package ucb.accounting.backend.api

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ucb.accounting.backend.bl.ExpenseTransactionBl
import ucb.accounting.backend.dto.ResponseDto
import ucb.accounting.backend.dto.ExpenseTransactionDto
import ucb.accounting.backend.dto.ExpenseTransactionPartialDto
import ucb.accounting.backend.dto.SubAccountDto
import ucb.accounting.backend.util.ResponseCodeUtil
import javax.validation.constraints.Null

@RestController
@RequestMapping("/api/v1/expense-transactions/companies")
class ExpenseTransactionApi @Autowired constructor(private val expenseTransactionBl: ExpenseTransactionBl) {

    companion object {
        private val logger = LoggerFactory.getLogger(ExpenseTransactionApi::class.java.name)
    }

    @PostMapping("/{companyId}")
    fun postExpenseTransaction(
        @PathVariable("companyId") companyId: Long,
        @RequestBody expenseTransactionDto: ExpenseTransactionDto
    ): ResponseEntity<ResponseDto<Null>> {
        logger.info("Starting the API call to post expense transaction")
        logger.info("POST /api/v1/expense-transactions/companies/${companyId}")
        expenseTransactionBl.createExpenseTransaction(companyId, expenseTransactionDto)
        logger.info("Sending response")
        val code = "201-13"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, null), responseInfo.httpStatus)
    }

    @GetMapping("/{companyId}/subaccounts")
    fun getSubaccountsForExpenseTransaction (
        @PathVariable("companyId") companyId: Long
    ): ResponseEntity<ResponseDto<List<SubAccountDto>>>{
        logger.info("Starting the API call to get subaccounts for expense transaction")
        logger.info("GET /api/v1/expense-transactions/companies/${companyId}/subaccounts")
        val subaccounts: List<SubAccountDto> = expenseTransactionBl.getSubaccountsForExpenseTransaction(companyId)
        logger.info("Sending response")
        val code = "200-14"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, subaccounts), responseInfo.httpStatus)
    }

    @GetMapping("/{companyId}")
    fun getExpenseTransactions (
        @PathVariable("companyId") companyId: Long
    ): ResponseEntity<ResponseDto<List<ExpenseTransactionPartialDto>>>{
        logger.info("Starting the API call to get expense transactions")
        logger.info("GET /api/v1/expense-transactions/companies/${companyId}")
        val expenseTransactions: List<ExpenseTransactionPartialDto> = expenseTransactionBl.getExpenseTransactions(companyId)
        logger.info("Sending response")
        val code = "200-33"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, expenseTransactions), responseInfo.httpStatus)
    }
}