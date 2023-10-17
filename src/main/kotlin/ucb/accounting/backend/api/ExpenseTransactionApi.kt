package ucb.accounting.backend.api

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ucb.accounting.backend.bl.ExpenseTransactionBl
import ucb.accounting.backend.dto.*
import ucb.accounting.backend.util.ResponseCodeUtil
import javax.validation.constraints.Null

@RestController
@RequestMapping("/api/v1/expense-transactions")
class ExpenseTransactionApi @Autowired constructor(private val expenseTransactionBl: ExpenseTransactionBl) {

    companion object {
        private val logger = LoggerFactory.getLogger(ExpenseTransactionApi::class.java.name)
    }

    @PostMapping("/invoices/companies/{companyId}")
    fun createInvoiceExpenseTransaction(
        @PathVariable("companyId") companyId: Long,
        @RequestBody invoiceDto: InvoiceDto
    ): ResponseEntity<ResponseDto<Null>> {
        logger.info("Starting the API call to post expense transaction")
        logger.info("POST /api/v1/expense-transactions/invoices/companies/${companyId}")
        expenseTransactionBl.createInvoiceExpenseTransaction(companyId, invoiceDto)
        logger.info("Sending response")
        val code = "201-13"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, null), responseInfo.httpStatus)
    }

    @GetMapping("/invoices/companies/{companyId}/subaccounts")
    fun getSubaccountsForInvoiceExpenseTransaction (
        @PathVariable("companyId") companyId: Long
    ): ResponseEntity<ResponseDto<List<SubaccountDto>>>{
        logger.info("Starting the API call to get subaccounts for expense transaction")
        logger.info("GET /api/v1/expense-transactions/invoices/companies/${companyId}/subaccounts")
        val subaccounts: List<SubaccountDto> = expenseTransactionBl.getSubaccountsForInvoiceExpenseTransaction(companyId)
        logger.info("Sending response")
        val code = "200-14"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, subaccounts), responseInfo.httpStatus)
    }

    @GetMapping("/invoices/last-numbers/companies/{companyId}")
    fun getLastInvoiceExpenseTransactionNumber(
        @PathVariable("companyId") companyId: Long
    ): ResponseEntity<ResponseDto<Int>>{
        logger.info("Starting the API call to get last expense transaction number")
        logger.info("GET /api/v1/expense-transactions/invoices/last-numbers/companies/${companyId}")
        val lastExpenseTransactionNumber: Int = expenseTransactionBl.getLastInvoiceExpenseTransactionNumber(companyId)
        logger.info("Sending response")
        val code = "200-39"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, lastExpenseTransactionNumber), responseInfo.httpStatus)
    }

    @PostMapping("/payments/companies/{companyId}")
    fun createPaymentExpenseTransaction(
        @PathVariable("companyId") companyId: Long,
        @RequestBody paymentDto: PaymentDto
    ): ResponseEntity<ResponseDto<Null>> {
        logger.info("Starting the API call to post sale transaction")
        logger.info("POST /api/v1/sale-transactions/payments/companies/${companyId}")
        expenseTransactionBl.createPaymentExpenseTransaction(companyId, paymentDto)
        logger.info("Sending response")
        val code = "201-13"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, null), responseInfo.httpStatus)
    }

    @GetMapping("/payments/companies/{companyId}/subaccounts")
    fun getSubaccountsForPaymentExpenseTransaction (
        @PathVariable("companyId") companyId: Long
    ): ResponseEntity<ResponseDto<List<SubaccountDto>>>{
        logger.info("Starting the API call to get subaccounts for sale transaction")
        logger.info("GET /api/v1/sale-transactions/payments/companies/${companyId}/subaccounts")
        val subaccounts: List<SubaccountDto> = expenseTransactionBl.getSubaccountsForPaymentExpenseTransaction(companyId)
        logger.info("Sending response")
        val code = "200-14"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, subaccounts), responseInfo.httpStatus)
    }

    @GetMapping("/payments/last-numbers/companies/{companyId}")
    fun getLastPaymentExpenseTransactionNumber(
        @PathVariable("companyId") companyId: Long
    ): ResponseEntity<ResponseDto<Int>>{
        logger.info("Starting the API call to get last sale transaction number")
        logger.info("GET /api/v1/sale-transactions/payments/last-numbers/companies/${companyId}")
        val lastExpenseTransactionNumber: Int = expenseTransactionBl.getLastPaymentExpenseTransactionNumber(companyId)
        logger.info("Sending response")
        val code = "200-39"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, lastExpenseTransactionNumber), responseInfo.httpStatus)
    }
    
    @GetMapping("/companies/{companyId}")
    fun getExpenseTransactions (
        @RequestParam(defaultValue = "expenseTransactionId") sortBy: String,
        @RequestParam(defaultValue = "asc") sortType: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @PathVariable("companyId") companyId: Long,
        @RequestParam(required = false) creationDate: String?,
        @RequestParam(required = false) transactionTypeId: Long?,
        @RequestParam(required = false) supplierIds: List<Long>?,
    ): ResponseEntity<ResponseDto<List<ExpenseTransactionDto>>>{
        logger.info("Starting the API call to get expense transactions")
        logger.info("GET /api/v1/expense-transactions/companies/${companyId}")
        val expenseTransactionsPage: Page<ExpenseTransactionDto> = expenseTransactionBl.getExpenseTransactions(companyId, sortBy, sortType, page, size, creationDate, transactionTypeId, supplierIds)
        logger.info("Sending response")
        val code = "200-33"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, expenseTransactionsPage.content, expenseTransactionsPage.totalElements), responseInfo.httpStatus)
    }

}