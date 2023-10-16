package ucb.accounting.backend.api

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ucb.accounting.backend.bl.SaleTransactionBl
import ucb.accounting.backend.dto.*
import ucb.accounting.backend.util.ResponseCodeUtil
import javax.validation.constraints.Null

@RestController
@RequestMapping("/api/v1/sale-transactions")
class SaleTransactionApi @Autowired constructor(private val saleTransactionBl: SaleTransactionBl) {

    companion object {
        private val logger = LoggerFactory.getLogger(SaleTransactionApi::class.java.name)
    }

    @PostMapping("/invoices/companies/{companyId}")
    fun createInvoiceSaleTransaction(
        @PathVariable("companyId") companyId: Long,
        @RequestBody invoiceDto: InvoiceDto
    ): ResponseEntity<ResponseDto<Null>> {
        logger.info("Starting the API call to post sale transaction")
        logger.info("POST /api/v1/sale-transactions/invoices/companies/${companyId}")
        saleTransactionBl.createInvoiceSaleTransaction(companyId, invoiceDto)
        logger.info("Sending response")
        val code = "201-11"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, null), responseInfo.httpStatus)
    }

    @GetMapping("/invoices/companies/{companyId}/subaccounts")
    fun getSubaccountsForInvoiceSaleTransaction (
        @PathVariable("companyId") companyId: Long
    ): ResponseEntity<ResponseDto<List<SubaccountDto>>>{
        logger.info("Starting the API call to get subaccounts for sale transaction")
        logger.info("GET /api/v1/sale-transactions/invoices/companies/${companyId}/subaccounts")
        val subaccounts: List<SubaccountDto> = saleTransactionBl.getSubaccountsForInvoiceSaleTransaction(companyId)
        logger.info("Sending response")
        val code = "200-14"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, subaccounts), responseInfo.httpStatus)
    }

    @GetMapping("/invoices/last-numbers/companies/{companyId}")
    fun getLastInvoiceSaleTransactionNumber(
        @PathVariable("companyId") companyId: Long
    ): ResponseEntity<ResponseDto<Int>>{
        logger.info("Starting the API call to get last sale transaction number")
        logger.info("GET /api/v1/sale-transactions/invoices/last-numbers/companies/${companyId}")
        val lastSaleTransactionNumber: Int = saleTransactionBl.getLastInvoiceSaleTransactionNumber(companyId)
        logger.info("Sending response")
        val code = "200-38"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, lastSaleTransactionNumber), responseInfo.httpStatus)
    }

    @PostMapping("/payments/companies/{companyId}")
    fun createPaymentSaleTransaction(
        @PathVariable("companyId") companyId: Long,
        @RequestBody paymentDto: PaymentDto
    ): ResponseEntity<ResponseDto<Null>> {
        logger.info("Starting the API call to post sale transaction")
        logger.info("POST /api/v1/sale-transactions/payments/companies/${companyId}")
        saleTransactionBl.createPaymentSaleTransaction(companyId, paymentDto)
        logger.info("Sending response")
        val code = "201-11"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, null), responseInfo.httpStatus)
    }

    @GetMapping("/payments/companies/{companyId}/subaccounts")
    fun getSubaccountsForPaymentSaleTransaction (
        @PathVariable("companyId") companyId: Long
    ): ResponseEntity<ResponseDto<List<SubaccountDto>>>{
        logger.info("Starting the API call to get subaccounts for sale transaction")
        logger.info("GET /api/v1/sale-transactions/payments/companies/${companyId}/subaccounts")
        val subaccounts: List<SubaccountDto> = saleTransactionBl.getSubaccountsForPaymentSaleTransaction(companyId)
        logger.info("Sending response")
        val code = "200-14"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, subaccounts), responseInfo.httpStatus)
    }

    @GetMapping("/payments/last-numbers/companies/{companyId}")
    fun getLastPaymentSaleTransactionNumber(
        @PathVariable("companyId") companyId: Long
    ): ResponseEntity<ResponseDto<Int>>{
        logger.info("Starting the API call to get last sale transaction number")
        logger.info("GET /api/v1/sale-transactions/payments/last-numbers/companies/${companyId}")
        val lastSaleTransactionNumber: Int = saleTransactionBl.getLastPaymentSaleTransactionNumber(companyId)
        logger.info("Sending response")
        val code = "200-38"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, lastSaleTransactionNumber), responseInfo.httpStatus)
    }

    @GetMapping("/companies/{companyId}")
    fun getSaleTransactions (
        @PathVariable("companyId") companyId: Long,
        @RequestParam(defaultValue = "saleTransactionId") sortBy: String,
        @RequestParam(defaultValue = "asc") sortType: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<ResponseDto<List<SaleTransactionDto>>>{
        logger.info("Starting the API call to get sale transactions")
        logger.info("GET /api/v1/sale-transactions/companies/${companyId}")
        val saleTransactionsPage: Page<SaleTransactionDto> = saleTransactionBl.getSaleTransactions(companyId, sortBy, sortType, page, size)
        logger.info("Sending response")
        val code = "200-32"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, saleTransactionsPage.content, saleTransactionsPage.totalElements), responseInfo.httpStatus)
    }

}