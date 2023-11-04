package ucb.accounting.backend.dao.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.data.repository.query.Param
import ucb.accounting.backend.dao.ExpenseTransaction
import ucb.accounting.backend.dao.SaleTransaction
import java.util.*

interface ExpenseTransactionRepository: PagingAndSortingRepository<ExpenseTransaction, Long> {
    fun findByCompanyIdAndTransactionTypeIdAndExpenseTransactionNumberAndStatusIsTrue (companyId: Int, transactionTypeId: Int, expenseTransactionNumber: Int): ExpenseTransaction?

    fun findFirstByCompanyIdAndTransactionTypeIdAndStatusIsTrueOrderByExpenseTransactionNumberDesc (companyId: Int, transactionTypeId: Int): ExpenseTransaction?

    @Query(value = "SELECT journal_entry_id FROM expense_transaction WHERE journal_entry_id = :journalEntryId AND company_id = :companyId  AND status = true", nativeQuery = true)
    fun findByJournalEntryIdAndCompanyIdAndStatusIsTrue(journalEntryId: Int, companyId: Int): Long?

    fun findByJournalEntryIdAndStatusIsTrue(journalEntryId: Int): ExpenseTransaction?

    @Query(value = """
        SELECT  s.subaccount_name as name,
        SUM((etd.quantity * etd.unit_price_bs) + etd.amount_bs) AS total
    FROM expense_transaction et
    JOIN subaccount s ON s.subaccount_id = et.subaccount_id
    JOIN expense_transaction_detail etd ON etd.expense_transaction_id = et.expense_transaction_id
    WHERE et.company_id = :companyId
    GROUP BY s.subaccount_name
    ORDER BY total DESC
    """, nativeQuery = true)
    fun countExpensesBySupplier(@Param ("companyId") companyId: Int): List<Map<String, Any>>

    @Query(value = """
        SELECT  s.subaccount_name as name,
        SUM((etd.quantity * etd.unit_price_bs) + etd.amount_bs) AS total
    FROM expense_transaction et
    JOIN expense_transaction_detail etd ON etd.expense_transaction_id = et.expense_transaction_id
    JOIN subaccount s ON s.subaccount_id = etd.subaccount_id
    WHERE et.company_id = :companyId
    AND et.expense_transaction_date BETWEEN '2023-10-01' AND '2023-10-31'
    GROUP BY s.subaccount_name
    ORDER BY total DESC
    LIMIT 10;
    """, nativeQuery = true)
    fun countExpensesBySubaccount(@Param ("companyId") companyId: Int): List<Map<String, Any>>

}