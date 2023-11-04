package ucb.accounting.backend.dao.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.data.repository.query.Param
import ucb.accounting.backend.dao.SaleTransaction
import java.util.*

interface SaleTransactionRepository: PagingAndSortingRepository<SaleTransaction, Long> {
    fun findByCompanyIdAndTransactionTypeIdAndSaleTransactionNumberAndStatusIsTrue(
        companyId: Int,
        transactionTypeId: Int,
        saleTransactionNumber: Int
    ): SaleTransaction?

    fun findFirstByCompanyIdAndTransactionTypeIdAndStatusIsTrueOrderBySaleTransactionNumberDesc(
        companyId: Int,
        transactionTypeId: Int
    ): SaleTransaction?

    @Query(
        value = "SELECT journal_entry_id FROM sale_transaction WHERE journal_entry_id = :journalEntryId AND company_id = :companyId  AND status = true",
        nativeQuery = true
    )
    fun findByJournalEntryIdAndCompanyIdAndStatusIsTrue(journalEntryId: Int, companyId: Int): Long?

    fun findByJournalEntryIdAndStatusIsTrue(journalEntryId: Int): SaleTransaction?

    @Query(value = """
        SELECT  s.subaccount_name as name,
        SUM((std.quantity * std.unit_price_bs) + std.amount_bs) AS total
    FROM sale_transaction st
    JOIN subaccount s ON s.subaccount_id = st.subaccount_id
    JOIN sale_transaction_detail std ON std.sale_transaction_id = st.sale_transaction_id
    WHERE st.company_id = :companyId
    GROUP BY s.subaccount_name
    ORDER BY total DESC
    """, nativeQuery = true)
    fun countSalesByClients(@Param ("companyId") companyId: Int): List<Map<String, Any>>

    @Query(value = """
        SELECT  s.subaccount_name as name,
        SUM((std.quantity * std.unit_price_bs) + std.amount_bs) AS total
    FROM sale_transaction st
    JOIN sale_transaction_detail std ON std.sale_transaction_id = st.sale_transaction_id
    JOIN subaccount s ON s.subaccount_id = std.subaccount_id
    WHERE st.company_id = :companyId
     AND st.sale_transaction_date BETWEEN '2023-10-01' AND '2023-10-31'
    GROUP BY s.subaccount_name
    ORDER BY total DESC
    LIMIT 10;
    """, nativeQuery = true)
    fun countSalesBySubaccounts(@Param ("companyId") companyId: Int): List<Map<String, Any>>

}

