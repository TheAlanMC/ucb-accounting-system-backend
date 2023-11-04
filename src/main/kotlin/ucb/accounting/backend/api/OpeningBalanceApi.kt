package ucb.accounting.backend.api

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ucb.accounting.backend.bl.DocumentTypeBl
import ucb.accounting.backend.bl.OpeningBalanceBl
import ucb.accounting.backend.dto.DocumentTypeDto
import ucb.accounting.backend.dto.FinancialStatementReportDetailDto
import ucb.accounting.backend.dto.ResponseDto
import ucb.accounting.backend.util.ResponseCodeUtil

@RestController
@RequestMapping("/api/v1/opening-balances")
class OpeningBalanceApi @Autowired constructor(private val openingBalanceBl: OpeningBalanceBl) {

    companion object {
        private val logger = LoggerFactory.getLogger(OpeningBalanceApi::class.java.name)
    }

    @GetMapping("/companies/{companyId}")
    fun getOpeningBalance(
        @PathVariable("companyId") companyId: Long,
        ): ResponseEntity<ResponseDto<List<FinancialStatementReportDetailDto>>> {
        logger.info("Starting the API call to get opening balances")
        logger.info("GET /api/v1/opening-balances")
        val openingBalances: List<FinancialStatementReportDetailDto> = openingBalanceBl.getOpeningBalance(companyId)
        logger.info("Sending response")
        val code = "200-07"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, openingBalances), responseInfo.httpStatus)
    }

    @PostMapping("/companies/{companyId}")
    fun createOpeningBalance(
        @PathVariable("companyId") companyId: Long,
        @RequestBody openingBalance: List<FinancialStatementReportDetailDto>,
        ): ResponseEntity<ResponseDto<Nothing>> {
        logger.info("Starting the API call to create opening balances")
        logger.info("POST /api/v1/opening-balances")
        openingBalanceBl.createOpeningBalance(companyId, openingBalance)
        logger.info("Sending response")
        val code = "201-15"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, null), responseInfo.httpStatus)
    }
}
