package ucb.accounting.backend.api

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ucb.accounting.backend.bl.SaleTransactionBl
import ucb.accounting.backend.dto.ResponseDto
import ucb.accounting.backend.dto.SaleTransactionDto
import ucb.accounting.backend.util.ResponseCodeUtil
import javax.validation.constraints.Null

@RestController
@RequestMapping("/api/v1/sale-transactions/companies")
class SaleTransactionApi @Autowired constructor(private val saleTransactionBl: SaleTransactionBl) {

    companion object {
        private val logger = LoggerFactory.getLogger(SaleTransactionApi::class.java.name)
    }

    @PostMapping("/{companyId}")
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
}