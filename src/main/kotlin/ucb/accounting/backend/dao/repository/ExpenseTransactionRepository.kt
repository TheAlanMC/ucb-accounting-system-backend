package ucb.accounting.backend.dao.repository

import org.springframework.data.jpa.repository.JpaRepository
import ucb.accounting.backend.dao.ExpenseTransaction

interface ExpenseTransactionRepository: JpaRepository<ExpenseTransaction, Long> {
    fun findByCompanyIdAndExpenseTransactionNumberAndStatusIsTrue (companyId: Int, saleTransactionNumber: Int): ExpenseTransaction?

    fun findAllByCompanyIdAndStatusIsTrue (companyId: Int): List<ExpenseTransaction>

    fun findFirstByCompanyIdAndStatusIsTrueOrderByExpenseTransactionNumberDesc (companyId: Int): ExpenseTransaction?
}