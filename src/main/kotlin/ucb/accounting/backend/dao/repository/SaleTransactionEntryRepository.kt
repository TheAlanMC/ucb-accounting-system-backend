package ucb.accounting.backend.dao.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.data.repository.query.Param
import ucb.accounting.backend.dao.SaleTransaction
import ucb.accounting.backend.dao.SaleTransactionEntry
import ucb.accounting.backend.dao.TransactionEntry
import java.util.*


interface SaleTransactionEntryRepository: PagingAndSortingRepository<SaleTransactionEntry, Long> {
    @Query(
        """
            SELECT  st.sale_transaction_id as sale_transaction_id,
                    tt.transaction_type_id as transaction_type_id,
                    tt.transaction_type_name as transaction_type_name,
                    st.sale_transaction_number as sale_transaction_number,
                    st.sale_transaction_date as sale_transaction_date,
                    c.customer_id as customer_id,
                    c.display_name as display_name,
                    c.company_name as company_name,
                    c.company_phone_number as company_phone_number,
                    st.tx_date as creation_date,    
                    st.gloss as gloss,
                    SUM((std.quantity * std.unit_price_bs) + std.amount_bs) AS total_amount_bs,
                    st.sale_transaction_accepted as sale_transaction_accepted
            FROM sale_transaction st
            JOIN sale_transaction_detail std ON std.sale_transaction_id = st.sale_transaction_id
            JOIN customer c ON c.customer_id = st.customer_id
            JOIN transaction_type tt ON tt.transaction_type_id = st.transaction_type_id
            WHERE st.company_id = :companyId
            AND (st.sale_transaction_date BETWEEN :dateFrom AND :dateTo OR CAST(:dateFrom AS DATE) IS NULL OR CAST(:dateTo AS DATE) IS NULL)
            AND (tt.transaction_type_name = :transactionType OR CAST(:transactionType AS VARCHAR) IS NULL)
            AND (c.display_name IN (:customers))
            AND st.status = true
            GROUP BY st.sale_transaction_id, tt.transaction_type_id, tt.transaction_type_name, st.sale_transaction_number, st.sale_transaction_date, c.customer_id, c.display_name, c.company_name, c.company_phone_number, st.tx_date, st.gloss, st.sale_transaction_accepted
        """, nativeQuery = true
    )
    fun findAll(
        @Param("companyId") companyId: Long,
        @Param("dateFrom") dateFrom: Date?,
        @Param("dateTo") dateTo: Date?,
        @Param("transactionType") transactionType: String?,
        @Param("customers") customers: List<String>,
        pageable: Pageable
    ): Page<SaleTransactionEntry>
}