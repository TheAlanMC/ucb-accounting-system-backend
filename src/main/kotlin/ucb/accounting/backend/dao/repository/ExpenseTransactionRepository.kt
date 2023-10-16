package ucb.accounting.backend.dao.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.PagingAndSortingRepository
import ucb.accounting.backend.dao.ExpenseTransaction

interface ExpenseTransactionRepository: PagingAndSortingRepository<ExpenseTransaction, Long> {
    fun findByCompanyIdAndTransactionTypeIdAndExpenseTransactionNumberAndStatusIsTrue (companyId: Int, transactionTypeId: Int, saleTransactionNumber: Int): ExpenseTransaction?

    fun findAllByCompanyIdAndStatusIsTrue (companyId: Int,pageable: Pageable): Page<ExpenseTransaction>

    fun findFirstByCompanyIdAndTransactionTypeIdAndStatusIsTrueOrderByExpenseTransactionNumberDesc (companyId: Int, transactionTypeId: Int): ExpenseTransaction?

    fun findAllByCompanyIdAndJournalEntryIdAndStatusIsTrue (companyId: Int, journalEntryId: Int): List<ExpenseTransaction>
}