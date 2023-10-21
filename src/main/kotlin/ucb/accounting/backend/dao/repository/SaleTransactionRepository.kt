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

    fun findAll(specification: Specification<SaleTransaction>, pageable: Pageable): Page<SaleTransaction>

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

    @Query(
        """
            SELECT  st.sale_transaction_id,
                    st.transaction_type_id,
                    st.payment_type_id,
                    st.journal_entry_id,
                    st.company_id,
                    st.customer_id,
                    st.subaccount_id,
                    st.sale_transaction_number,
                    st.sale_transaction_reference,
                    st.sale_transaction_date,
                    st.description,
                    st.gloss,
                    SUM((std.quantity * std.unit_price_bs) + std.amount_bs) AS total_amount_bs,
                    st.sale_transaction_accepted,
                    st.status,
                    st.tx_date,
                    st.tx_user,
                    st.tx_host
            FROM sale_transaction st
            JOIN sale_transaction_detail std ON std.sale_transaction_id = st.sale_transaction_id
            JOIN customer c ON c.customer_id = st.customer_id
            JOIN transaction_type tt ON tt.transaction_type_id = st.transaction_type_id
            WHERE st.company_id = :companyId
            AND (st.sale_transaction_date BETWEEN :dateFrom AND :dateTo OR CAST(:dateFrom AS DATE) IS NULL OR CAST(:dateTo AS DATE) IS NULL)
            AND (tt.transaction_type_name = :transactionType OR CAST(:transactionType AS VARCHAR) IS NULL)
            AND (c.display_name IN (:customers))
            AND st.status = true
            GROUP BY st.sale_transaction_id, st.transaction_type_id, st.payment_type_id, st.journal_entry_id, st.company_id, st.customer_id, st.subaccount_id, st.sale_transaction_number, st.sale_transaction_reference, st.sale_transaction_date, st.description, st.gloss, st.sale_transaction_accepted, st.status, st.tx_date, st.tx_user, st.tx_host
        """, nativeQuery = true
    )
    fun findAll(
        @Param("companyId") companyId: Long,
        @Param("dateFrom") dateFrom: Date?,
        @Param("dateTo") dateTo: Date?,
        @Param("transactionType") transactionType: String?,
        @Param("customers") customers: List<String>,
        pageable: Pageable
    ): Page<SaleTransaction>
}

