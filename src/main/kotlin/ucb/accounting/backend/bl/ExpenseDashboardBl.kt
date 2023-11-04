package ucb.accounting.backend.bl

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ucb.accounting.backend.dao.repository.ExpenseTransactionRepository
import ucb.accounting.backend.dto.ExpenseDashboardDto
import ucb.accounting.backend.dto.ExpensesDto
import java.math.BigDecimal

@Service
class ExpenseDashboardBl @Autowired constructor(
    private val expenseTransactionRepository: ExpenseTransactionRepository
){

    companion object {
        private val logger = LoggerFactory.getLogger(ExpenseDashboardBl::class.java.name)
    }

    fun getExpenseBySupplier(companyId: Long): ExpenseDashboardDto {
        val supplierTotalList = expenseTransactionRepository.countExpensesBySupplier(companyId.toInt())
        val expenseSupplier: List<ExpensesDto> = supplierTotalList.map {
            ExpensesDto(it["name"].toString(), BigDecimal.valueOf(it["total"].toString().toDouble()))
        }
        return ExpenseDashboardDto(expenseSupplier)
    }

    fun getExpenseBySubaccount(companyId: Long): ExpenseDashboardDto {
        val subaccountTotalList = expenseTransactionRepository.countExpensesBySubaccount(companyId.toInt())
        val expenseSubaccount: List<ExpensesDto> = subaccountTotalList.map {
            ExpensesDto(it["name"].toString(), BigDecimal.valueOf(it["total"].toString().toDouble()))
        }
        return ExpenseDashboardDto(expenseSubaccount)
    }

}