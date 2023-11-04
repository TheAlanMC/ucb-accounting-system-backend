package ucb.accounting.backend.dao.repository

import org.checkerframework.checker.units.qual.A
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
    AND (et.expense_transaction_date BETWEEN :dateFrom AND :dateTo OR CAST(:dateFrom AS DATE) IS NULL OR CAST(:dateTo AS DATE) IS NULL) 
    GROUP BY s.subaccount_name
    ORDER BY total DESC
    """, nativeQuery = true)
    fun countExpensesBySupplier(@Param ("companyId") companyId: Int,
                                @Param ("dateFrom") dateFrom: Date?,
                                @Param ("dateTo") dateTo: Date?): List<Map<String, Any>>

    @Query(value = """
        SELECT  s.subaccount_name as name,
        SUM((etd.quantity * etd.unit_price_bs) + etd.amount_bs) AS total
    FROM expense_transaction et
    JOIN expense_transaction_detail etd ON etd.expense_transaction_id = et.expense_transaction_id
    JOIN subaccount s ON s.subaccount_id = etd.subaccount_id
    WHERE et.company_id = :companyId
    AND (et.expense_transaction_date BETWEEN :dateFrom AND :dateTo OR CAST(:dateFrom AS DATE) IS NULL OR CAST(:dateTo AS DATE) IS NULL) 
    GROUP BY s.subaccount_name
    ORDER BY total DESC
    LIMIT 10;
    """, nativeQuery = true)
    fun countExpensesBySubaccount(@Param ("companyId") companyId: Int,
                                  @Param ("dateFrom") dateFrom: Date?,
                                  @Param ("dateTo") dateTo: Date?): List<Map<String, Any>>

    @Query(value = """
        SELECT
    year,
    month,
    SUM(expenses) AS expenses,
    SUM(sales) AS sales
    FROM (
        SELECT
            EXTRACT(YEAR FROM DATE_TRUNC('year', et.expense_transaction_date)) AS year,
            EXTRACT(MONTH FROM DATE_TRUNC('month', et.expense_transaction_date)) AS month,
            SUM((etd.quantity * etd.unit_price_bs) + etd.amount_bs) AS expenses,
            0 AS sales
        FROM expense_transaction et
        JOIN expense_transaction_detail etd ON etd.expense_transaction_id = et.expense_transaction_id
        WHERE et.company_id = :companyId
        AND (et.expense_transaction_date BETWEEN :dateFrom AND :dateTo OR CAST(:dateFrom AS DATE) IS NULL OR CAST(:dateTo AS DATE) IS NULL)
        GROUP BY year, month
        UNION ALL
        SELECT
            EXTRACT(YEAR FROM DATE_TRUNC('year', st.sale_transaction_date)) AS year,
            EXTRACT(MONTH FROM DATE_TRUNC('month', st.sale_transaction_date)) AS month,
            0 AS expenses,
            SUM((std.quantity * std.unit_price_bs) + std.amount_bs) AS sales
        FROM sale_transaction st
        JOIN sale_transaction_detail std ON std.sale_transaction_id = st.sale_transaction_id
        WHERE st.company_id = :companyId
        AND (st.sale_transaction_date BETWEEN :dateFrom AND :dateTo OR CAST(:dateFrom AS DATE) IS NULL OR CAST(:dateTo AS DATE) IS NULL)
        GROUP BY year, month
    ) AS combined_data
    GROUP BY year, month
    ORDER BY year, month
    """, nativeQuery = true)
    fun countExpensesAndSalesByMonth(@Param ("companyId") companyId: Int,
                                     @Param ("dateFrom") dateFrom: Date?,
                                     @Param ("dateTo") dateTo: Date?): List<Map<String, Any>>

}