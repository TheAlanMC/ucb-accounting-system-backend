package ucb.accounting.backend.dao.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.PagingAndSortingRepository
import ucb.accounting.backend.dao.ExpenseTransaction

interface ExpenseTransactionRepository: PagingAndSortingRepository<ExpenseTransaction, Long> {
    fun findByCompanyIdAndTransactionTypeIdAndExpenseTransactionNumberAndStatusIsTrue (companyId: Int, transactionTypeId: Int, saleTransactionNumber: Int): ExpenseTransaction?

    fun findAllByCompanyIdAndStatusIsTrue (companyId: Int,pageable: Pageable): Page<ExpenseTransaction>

    fun findFirstByCompanyIdAndTransactionTypeIdAndStatusIsTrueOrderByExpenseTransactionNumberDesc (companyId: Int, transactionTypeId: Int): ExpenseTransaction?

    @Query(value = "SELECT journal_entry_id FROM expense_transaction WHERE company_id = :companyId AND status = true", nativeQuery = true)
    fun findAllJournalEntryId(companyId: Int): List<Long>

    fun findByJournalEntryIdAndStatusIsTrue(journalEntryId: Int): ExpenseTransaction?
}