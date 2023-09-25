package ucb.accounting.backend.dao.repository

import org.springframework.data.jpa.repository.JpaRepository
import ucb.accounting.backend.dao.ExpenseTransaction
import ucb.accounting.backend.dao.SaleTransaction

interface ExpenseTransactionRepository: JpaRepository<ExpenseTransaction, Long> {
    fun findByCompanyIdAndExpenseTransactionNumberAndStatusIsTrue (companyId: Int, saleTransactionNumber: Int): ExpenseTransaction?

    fun findAllByCompanyIdAndStatusIsTrue (companyId: Int): List<ExpenseTransaction>
}