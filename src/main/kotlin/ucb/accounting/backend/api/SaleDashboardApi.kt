package ucb.accounting.backend.api

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ucb.accounting.backend.bl.SaleDashboardBl
import ucb.accounting.backend.dto.ExpenseDashboardDto
import ucb.accounting.backend.dto.ResponseDto
import ucb.accounting.backend.dto.SaleDashboardDto
import ucb.accounting.backend.util.ResponseCodeUtil

@RestController
@RequestMapping("/api/v1/sale-dashboard")
class SaleDashboardApi @Autowired constructor(
    private val saleDashboardBl: SaleDashboardBl
){

    companion object {
        private val logger = LoggerFactory.getLogger(SaleDashboardApi::class.java.name)
    }

    @GetMapping("/client/{companyId}")
    fun getSaleDashboardDataByClient(@PathVariable companyId: Long,
                                     @RequestParam(required = true) dateFrom: String,
                                     @RequestParam(required = true) dateTo: String): ResponseEntity<ResponseDto<SaleDashboardDto>> {
        logger.info("Starting the API call to get sale dashboard data")
        logger.info("GET /sale-dashboard")
        val saleDashboardData: SaleDashboardDto = saleDashboardBl.getSaleByClient(companyId, dateFrom, dateTo)
        logger.info("Sending response")
        val code = "200-20"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, saleDashboardData), responseInfo.httpStatus)
    }

    @GetMapping("/subaccount/{companyId}")
    fun getSaleDashboardDataBySubaccount(@PathVariable companyId: Long,
                                         @RequestParam (required = true) dateFrom: String,
                                         @RequestParam (required = true) dateTo: String): ResponseEntity<ResponseDto<SaleDashboardDto>> {
        logger.info("Starting the API call to get sale dashboard data")
        logger.info("GET /sale-dashboard")
        val saleDashboardData: SaleDashboardDto = saleDashboardBl.getSaleBySubaccount(companyId, dateFrom, dateTo)
        logger.info("Sending response")
        val code = "200-20"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, saleDashboardData), responseInfo.httpStatus)
    }

}