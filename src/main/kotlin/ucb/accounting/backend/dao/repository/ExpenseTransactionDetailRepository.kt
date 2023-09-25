package ucb.accounting.backend.dao.repository

import org.springframework.data.jpa.repository.JpaRepository
import ucb.accounting.backend.dao.ExpenseTransactionDetail

interface ExpenseTransactionDetailRepository: JpaRepository<ExpenseTransactionDetail, Long> {
    fun findAllByExpenseTransactionIdAndStatusIsTrue(saleTransactionId: Long): List<ExpenseTransactionDetail>

}