package ucb.accounting.backend.bl

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ucb.accounting.backend.dao.repository.ExpenseTransactionRepository
import ucb.accounting.backend.dto.ExpenseSaleDashboardDto
import ucb.accounting.backend.dto.ExpensesSalesDto
import ucb.accounting.backend.exception.UasException
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*

@Service
class ExpensesSalesBl @Autowired constructor(
    private val expenseTransactionRepository: ExpenseTransactionRepository
){

    companion object {
        private val logger = LoggerFactory.getLogger(ExpensesSalesBl::class.java.name)
    }

    fun getExpensesSales(companyId: Long, dateFrom: String, dateTo: String): ExpenseSaleDashboardDto {
        // Validation of user belongs to company
//        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
//        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId)
//            ?: throw UasException("403-22")
//        logger.info("User $kcUuid is trying to get journal book report from company $companyId")

        // Convert dateFrom and dateTo to Date
        val format: java.text.DateFormat = SimpleDateFormat("yyyy-MM-dd")
        val newDateFrom: Date = format.parse(dateFrom)
        val newDateTo: Date = format.parse(dateTo)

        // Validation of dateFrom and dateTo
        if (newDateFrom.after(newDateTo)) {
            throw UasException("400-15")
        }
        val expensesSalesList = expenseTransactionRepository.countExpensesAndSalesByMonth(companyId.toInt(), newDateFrom, newDateTo)
        val expensesSales: List<ExpensesSalesDto> = expensesSalesList.map { resultMap ->
            ExpensesSalesDto(
                (resultMap["year"] as? Int) ?: 0,  // Manejar valor nulo de manera segura
                (resultMap["month"] as? Int) ?: 0,  // Manejar valor nulo de manera segura
                (resultMap["expenses"] as? BigDecimal) ?: BigDecimal.ZERO,  // Manejar valor nulo de manera segura
                (resultMap["sales"] as? BigDecimal) ?: BigDecimal.ZERO  // Manejar valor nulo de manera segura
            )
        }
        return ExpenseSaleDashboardDto(expensesSales)
    }
}