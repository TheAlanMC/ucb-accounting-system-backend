package ucb.accounting.backend.api

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ucb.accounting.backend.bl.ExpenseDashboardBl
import ucb.accounting.backend.dto.ExpenseDashboardDto
import ucb.accounting.backend.dto.ResponseDto
import ucb.accounting.backend.util.ResponseCodeUtil

@RestController
@RequestMapping("/api/v1/expense-dashboard")
class ExpenseDashboardApi @Autowired constructor(
    private val expenseDashboardBl: ExpenseDashboardBl
){

    companion object {
        private val logger = LoggerFactory.getLogger(ExpenseDashboardApi::class.java.name)
    }

    @GetMapping("/supplier/{companyId}")
    fun getExpenseDashboardDataBySupplier(@PathVariable companyId: Long): ResponseEntity<ResponseDto<ExpenseDashboardDto>> {
        logger.info("Starting the API call to get expense dashboard data")
        logger.info("GET /expense-dashboard")
        val expenseDashboardData: ExpenseDashboardDto = expenseDashboardBl.getExpenseBySupplier(companyId)
        logger.info("Sending response")
        val code = "200-20"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, expenseDashboardData), responseInfo.httpStatus)
    }

    @GetMapping("/subaccount/{companyId}")
    fun getExpenseDashboardDataBySubaccount(@PathVariable companyId: Long): ResponseEntity<ResponseDto<ExpenseDashboardDto>> {
        logger.info("Starting the API call to get expense dashboard data")
        logger.info("GET /expense-dashboard")
        val expenseDashboardData: ExpenseDashboardDto = expenseDashboardBl.getExpenseBySubaccount(companyId)
        logger.info("Sending response")
        val code = "200-20"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, expenseDashboardData), responseInfo.httpStatus)
    }

}