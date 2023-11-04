package ucb.accounting.backend.api

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ucb.accounting.backend.bl.ExpensesSalesBl
import ucb.accounting.backend.dto.ExpenseDashboardDto
import ucb.accounting.backend.dto.ExpenseSaleDashboardDto
import ucb.accounting.backend.dto.ResponseDto
import ucb.accounting.backend.util.ResponseCodeUtil

@RestController
@RequestMapping ("/api/v1/expenses-sales")
class ExpensesSalesApi @Autowired constructor(
    private val expensesSalesBl: ExpensesSalesBl
){

    companion object {
        private val logger = LoggerFactory.getLogger(ExpensesSalesApi::class.java.name)
    }

    @GetMapping("/{companyId}")
    fun getExpensesSales(@PathVariable companyId: Long,
                         @RequestParam(required = true) dateFrom: String,
                         @RequestParam(required = true) dateTo: String): ResponseEntity<ResponseDto<ExpenseSaleDashboardDto>> {
        logger.info("Starting the API call to get expenses sales data")
        logger.info("GET /expenses-sales")
        val expensesSalesData: ExpenseSaleDashboardDto = expensesSalesBl.getExpensesSales(companyId, dateFrom, dateTo)
        logger.info("Sending response")
        val code = "200-20"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, expensesSalesData), responseInfo.httpStatus)
    }

}