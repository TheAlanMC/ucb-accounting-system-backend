package ucb.accounting.backend.api

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ucb.accounting.backend.bl.SaleTransactionBl
import ucb.accounting.backend.dto.ResponseDto
import ucb.accounting.backend.dto.SaleTransactionDto
import ucb.accounting.backend.dto.SaleTransactionPartialDto
import ucb.accounting.backend.dto.SubaccountDto
import ucb.accounting.backend.util.ResponseCodeUtil
import javax.validation.constraints.Null

@RestController
@RequestMapping("/api/v1/sale-transactions")
class SaleTransactionApi @Autowired constructor(private val saleTransactionBl: SaleTransactionBl) {

    companion object {
        private val logger = LoggerFactory.getLogger(SaleTransactionApi::class.java.name)
    }

    @PostMapping("/companies/{companyId}")
    fun postSaleTransaction(
        @PathVariable("companyId") companyId: Long,
        @RequestBody saleTransactionDto: SaleTransactionDto
    ): ResponseEntity<ResponseDto<Null>> {
        logger.info("Starting the API call to post sale transaction")
        logger.info("POST /api/v1/sale-transactions/companies/${companyId}")
        saleTransactionBl.createSaleTransaction(companyId, saleTransactionDto)
        logger.info("Sending response")
        val code = "201-11"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, null), responseInfo.httpStatus)
    }

    @GetMapping("/companies/{companyId}/subaccounts")
    fun getSubaccountsForSaleTransaction (
        @PathVariable("companyId") companyId: Long
    ): ResponseEntity<ResponseDto<List<SubaccountDto>>>{
        logger.info("Starting the API call to get subaccounts for sale transaction")
        logger.info("GET /api/v1/sale-transactions/companies/${companyId}/subaccounts")
        val subaccounts: List<SubaccountDto> = saleTransactionBl.getSubaccountsForSaleTransaction(companyId)
        logger.info("Sending response")
        val code = "200-14"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, subaccounts), responseInfo.httpStatus)
    }

    @GetMapping("/companies/{companyId}")
    fun getSaleTransactions (
        @PathVariable("companyId") companyId: Long
    ): ResponseEntity<ResponseDto<List<SaleTransactionPartialDto>>>{
        logger.info("Starting the API call to get sale transactions")
        logger.info("GET /api/v1/sale-transactions/companies/${companyId}")
        val saleTransactions: List<SaleTransactionPartialDto> = saleTransactionBl.getSaleTransactions(companyId)
        logger.info("Sending response")
        val code = "200-32"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, saleTransactions), responseInfo.httpStatus)
    }

    @GetMapping("/last-number/companies/{companyId}")
    fun getLastSaleTransactionNumber(
        @PathVariable("companyId") companyId: Long
    ): ResponseEntity<ResponseDto<Int>>{
        logger.info("Starting the API call to get last sale transaction number")
        logger.info("GET /api/v1/sale-transactions/last-number/companies/${companyId}")
        val lastSaleTransactionNumber: Int = saleTransactionBl.getLastSaleTransactionNumber(companyId)
        logger.info("Sending response")
        val code = "200-38"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, lastSaleTransactionNumber), responseInfo.httpStatus)
    }
}