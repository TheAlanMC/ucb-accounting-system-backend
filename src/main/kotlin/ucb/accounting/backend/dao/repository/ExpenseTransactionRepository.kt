package ucb.accounting.backend.dao.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.PagingAndSortingRepository
import ucb.accounting.backend.dao.ExpenseTransaction
import ucb.accounting.backend.dao.SaleTransaction

interface ExpenseTransactionRepository: PagingAndSortingRepository<ExpenseTransaction, Long> {
    fun findByCompanyIdAndTransactionTypeIdAndExpenseTransactionNumberAndStatusIsTrue (companyId: Int, transactionTypeId: Int, saleTransactionNumber: Int): ExpenseTransaction?

    fun findAll (specification: Specification<ExpenseTransaction>, pageable: Pageable): Page<ExpenseTransaction>

    fun findFirstByCompanyIdAndTransactionTypeIdAndStatusIsTrueOrderByExpenseTransactionNumberDesc (companyId: Int, transactionTypeId: Int): ExpenseTransaction?

    @Query(value = "SELECT journal_entry_id FROM expense_transaction WHERE journal_entry_id = :journalEntryId AND company_id = :companyId  AND status = true", nativeQuery = true)
    fun findByJournalEntryIdAndCompanyIdAndStatusIsTrue(journalEntryId: Int, companyId: Int): Long?

    fun findByJournalEntryIdAndStatusIsTrue(journalEntryId: Int): ExpenseTransaction?
}