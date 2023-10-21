package ucb.accounting.backend.dao.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.data.repository.query.Param
import ucb.accounting.backend.dao.TransactionEntry


interface TransactionEntryRepository: PagingAndSortingRepository<TransactionEntry, Long> {
    @Query(
        """
         SELECT  je.journal_entry_id,
                CONCAT (et.expense_transaction_number, st.sale_transaction_number) AS transaction_number,
                CONCAT(c.customer_id, s.supplier_id) AS client_id,
                CONCAT(c.display_name, s.display_name) AS display_name,
                CONCAT(c.company_name, s.company_name) AS company_name,
                CONCAT(c.company_phone_number, s.company_phone_number) AS company_phone_number,
                CONCAT(c.tx_date, s.tx_date) AS client_creation_date,
                je.journal_entry_accepted AS transaction_accepted,
                dt.document_type_id,
                dt.document_type_name,
                CONCAT(tt1.transaction_type_id, tt2.transaction_type_id) AS transaction_type_id,
                CONCAT(tt1.transaction_type_name, tt2.transaction_type_name) AS transaction_type_name,
                CONCAT(SUM((etd.quantity * etd.unit_price_bs) + etd.amount_bs), SUM((std.quantity * std.unit_price_bs) + std.amount_bs)) AS total_amount_bs,
                CONCAT(et.tx_date, st.tx_date) AS creation_date,
                CONCAT(et.expense_transaction_date, st.sale_transaction_date) AS transaction_date,
                CONCAT(et.description, st.description) AS description
        FROM journal_entry je
        LEFT JOIN  expense_transaction et ON et.journal_entry_id = je.journal_entry_id
        LEFT JOIN  expense_transaction_detail etd ON etd.expense_transaction_id = et.expense_transaction_id
        LEFT JOIN  sale_transaction st ON st.journal_entry_id = je.journal_entry_id
        LEFT JOIN  sale_transaction_detail std ON std.sale_transaction_id = st.sale_transaction_id
        LEFT JOIN  customer c ON c.customer_id = st.customer_id
        LEFT JOIN  supplier s ON s.supplier_id = et.supplier_id
        LEFT JOIN  document_type dt ON dt.document_type_id = je.document_type_id
        LEFT JOIN  transaction_type tt1 ON tt1.transaction_type_id = st.transaction_type_id
        LEFT JOIN  transaction_type tt2 ON tt2.transaction_type_id = et.transaction_type_id    
        WHERE je.company_id = :companyId
        AND je.status = true
        AND (et.expense_transaction_id IS NOT NULL OR st.sale_transaction_id IS NOT NULL)
        AND (LOWER(CONCAT(c.display_name, s.display_name)) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
             LOWER(dt.document_type_name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
             LOWER(CONCAT(tt1.transaction_type_name, tt2.transaction_type_name)) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
             LOWER(CONCAT(et.description, st.description)) LIKE LOWER(CONCAT('%', :keyword, '%')))
        GROUP BY je.journal_entry_id, et.expense_transaction_number, st.sale_transaction_number, c.customer_id, s.supplier_id, c.display_name, s.display_name, c.company_name, s.company_name, c.company_phone_number, s.company_phone_number, c.tx_date, s.tx_date, je.journal_entry_accepted, dt.document_type_id, dt.document_type_name, tt1.transaction_type_id, tt1.transaction_type_name, tt2.transaction_type_id, tt2.transaction_type_name, et.tx_date, st.tx_date, et.expense_transaction_date, st.sale_transaction_date, et.description, st.description
        """
        , nativeQuery = true
    )
    fun findAll(@Param("companyId") companyId: Long, @Param("keyword") keyword: String, pageable: Pageable): Page<TransactionEntry>
}