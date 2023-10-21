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

    fun findAll (specification: Specification<ExpenseTransaction>, pageable: Pageable): Page<ExpenseTransaction>

    fun findFirstByCompanyIdAndTransactionTypeIdAndStatusIsTrueOrderByExpenseTransactionNumberDesc (companyId: Int, transactionTypeId: Int): ExpenseTransaction?

    @Query(value = "SELECT journal_entry_id FROM expense_transaction WHERE journal_entry_id = :journalEntryId AND company_id = :companyId  AND status = true", nativeQuery = true)
    fun findByJournalEntryIdAndCompanyIdAndStatusIsTrue(journalEntryId: Int, companyId: Int): Long?

    fun findByJournalEntryIdAndStatusIsTrue(journalEntryId: Int): ExpenseTransaction?

    @Query(
        """
            SELECT  et.expense_transaction_id,
                    et.transaction_type_id,
                    et.payment_type_id,
                    et.journal_entry_id,
                    et.company_id,
                    et.supplier_id,
                    et.subaccount_id,
                    et.expense_transaction_number,
                    et.expense_transaction_reference,
                    et.expense_transaction_date,
                    et.description,
                    et.gloss,
                    SUM((std.quantity * std.unit_price_bs) + std.amount_bs) AS total_amount_bs,
                    et.expense_transaction_accepted,
                    et.status,
                    et.tx_date,
                    et.tx_user,
                    et.tx_host
            FROM expense_transaction et
            JOIN expense_transaction_detail std ON std.expense_transaction_id = et.expense_transaction_id
            JOIN supplier c ON c.supplier_id = et.supplier_id
            JOIN transaction_type tt ON tt.transaction_type_id = et.transaction_type_id
            WHERE et.company_id = :companyId
            AND (et.expense_transaction_date BETWEEN :dateFrom AND :dateTo OR CAST(:dateFrom AS DATE) IS NULL OR CAST(:dateTo AS DATE) IS NULL)
            AND (tt.transaction_type_name = :transactionType OR CAST(:transactionType AS VARCHAR) IS NULL)
            AND (c.display_name IN (:suppliers))
            AND et.status = true
            GROUP BY et.expense_transaction_id, et.transaction_type_id, et.payment_type_id, et.journal_entry_id, et.company_id, et.supplier_id, et.subaccount_id, et.expense_transaction_number, et.expense_transaction_reference, et.expense_transaction_date, et.description, et.gloss, et.expense_transaction_accepted, et.status, et.tx_date, et.tx_user, et.tx_host
        """, nativeQuery = true
    )
    fun findAll(
        @Param("companyId") companyId: Long,
        @Param("dateFrom") dateFrom: Date?,
        @Param("dateTo") dateTo: Date?,
        @Param("transactionType") transactionType: String?,
        @Param("suppliers") suppliers: List<String>,
        pageable: Pageable
    ): Page<ExpenseTransaction>
}