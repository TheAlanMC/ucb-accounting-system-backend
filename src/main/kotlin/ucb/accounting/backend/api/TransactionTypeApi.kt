package ucb.accounting.backend.api

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ucb.accounting.backend.bl.TransactionTypeBl
import ucb.accounting.backend.dto.TransactionTypeDto
import ucb.accounting.backend.dto.ResponseDto
import ucb.accounting.backend.util.ResponseCodeUtil

@RestController
@RequestMapping("/api/v1/transaction-types")
class TransactionTypeApi @Autowired constructor(private val transactionTypeBl: TransactionTypeBl) {

    companion object {
        private val logger = LoggerFactory.getLogger(TransactionTypeApi::class.java.name)
    }

    @GetMapping
    fun getTransactionTypes(): ResponseEntity<ResponseDto<List<TransactionTypeDto>>> {
        logger.info("Starting the API call to get transaction types")
        logger.info("GET /api/v1/transaction-types")
        val transactionTypes: List<TransactionTypeDto> = transactionTypeBl.getTransactionTypes()
        logger.info("Sending response")
        val code = "200-43"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, transactionTypes), responseInfo.httpStatus)
    }
}
