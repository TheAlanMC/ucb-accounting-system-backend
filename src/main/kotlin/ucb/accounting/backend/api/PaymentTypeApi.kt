package ucb.accounting.backend.api

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ucb.accounting.backend.bl.PaymentTypeBl
import ucb.accounting.backend.dto.PaymentTypeDto
import ucb.accounting.backend.dto.ResponseDto
import ucb.accounting.backend.util.ResponseCodeUtil

@RestController
@RequestMapping("/api/v1/payment-types")
class PaymentTypeApi @Autowired constructor(private val paymentTypeBl: PaymentTypeBl) {

    companion object {
        private val logger = LoggerFactory.getLogger(PaymentTypeApi::class.java.name)
    }

    @GetMapping
    fun getPaymentTypes(): ResponseEntity<ResponseDto<List<PaymentTypeDto>>> {
        logger.info("Starting the API call to get payment types")
        logger.info("GET /api/v1/payment-types")
        val paymentTypes: List<PaymentTypeDto> = paymentTypeBl.getPaymentTypes()
        logger.info("Sending response")
        val code = "200-40"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, paymentTypes), responseInfo.httpStatus)
    }
}
