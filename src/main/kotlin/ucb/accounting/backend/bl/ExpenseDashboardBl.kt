package ucb.accounting.backend.bl

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ucb.accounting.backend.dao.repository.ExpenseTransactionRepository
import ucb.accounting.backend.dto.ExpenseDashboardDto

@Service
class ExpenseDashboardBl @Autowired constructor(
    private val expenseTransactionRepository: ExpenseTransactionRepository
){

    companion object {
        private val logger = LoggerFactory.getLogger(ExpenseDashboardBl::class.java.name)
    }

    fun getExpenseDashboardData(companyId: Long): ExpenseDashboardDto {
        val descriptionCountList = expenseTransactionRepository.countExpensesByDescription(companyId.toInt())
        val descriptionList = mutableListOf<String>()
        val countList = mutableListOf<Int>()
        descriptionCountList.forEach {
            descriptionList.add(it["description"].toString())
            countList.add(it["count"].toString().toInt())
        }
        return ExpenseDashboardDto(descriptionList, countList)
    }

}