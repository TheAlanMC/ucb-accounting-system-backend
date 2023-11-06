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
         SELECT  je.journal_entry_id AS journal_entry_id,
                CASE
                    WHEN (CONCAT(et.expense_transaction_number, st.sale_transaction_number) != '') THEN CONCAT(et.expense_transaction_number, st.sale_transaction_number)
                    ELSE CONCAT(je.journal_entry_number)
                    END
                    AS transaction_number,
                CASE
                    WHEN (CONCAT(c.customer_id, s.supplier_id) != '') THEN CONCAT(c.customer_id, s.supplier_id)
                    ELSE CONCAT(0)
                    END
                    AS client_id,
                CASE
                    WHEN (CONCAT(c.display_name, s.display_name) != '') THEN CONCAT(c.display_name, s.display_name)
                    ELSE 'N/A'
                    END
                    AS display_name,
                CASE
                    WHEN (CONCAT(c.company_name, s.company_name) != '') THEN CONCAT(c.company_name, s.company_name)
                    ELSE 'N/A'
                    END
                    AS company_name,
                CASE
                    WHEN (CONCAT(c.company_phone_number, s.company_phone_number) != '') THEN CONCAT(c.company_phone_number, s.company_phone_number)
                    ELSE 'N/A'
                    END
                    AS company_phone_number,
                CASE
                    WHEN (CONCAT(c.tx_date, s.tx_date) != '') THEN CONCAT(c.tx_date, s.tx_date)
                    ELSE CONCAT(je.tx_date)
                    END
                    AS client_creation_date,
                je.journal_entry_accepted AS transaction_accepted,
                dt.document_type_id AS document_type_id,
                dt.document_type_name AS document_type_name,
                CASE
                    WHEN (CONCAT(tt1.transaction_type_id, tt2.transaction_type_id) != '') THEN CONCAT(tt1.transaction_type_id, tt2.transaction_type_id)
                    ELSE CONCAT(3)
                    END
                    AS transaction_type_id,
                CASE
                    WHEN (CONCAT(tt1.transaction_type_name, tt2.transaction_type_name) != '') THEN CONCAT(tt1.transaction_type_name, tt2.transaction_type_name)
                    ELSE CONCAT('Comprobante Contable')
                    END
                    AS transaction_type_name,
                CASE
                    WHEN (CONCAT(SUM((etd.quantity * etd.unit_price_bs) + etd.amount_bs), SUM((std.quantity * std.unit_price_bs) + std.amount_bs)) != '') THEN CONCAT(SUM((etd.quantity * etd.unit_price_bs) + etd.amount_bs), SUM((std.quantity * std.unit_price_bs) + std.amount_bs))
                    ELSE CONCAT(0)
                    END
                    AS total_amount_bs,
                CASE
                    WHEN (CONCAT(et.tx_date, st.tx_date) != '') THEN CONCAT(et.tx_date, st.tx_date)
                    ELSE CONCAT(je.tx_date)
                    END
                    AS creation_date,
                CASE
                    WHEN (CONCAT(et.expense_transaction_date, st.sale_transaction_date) != '') THEN CONCAT(et.expense_transaction_date, st.sale_transaction_date)
                    ELSE CONCAT(je.tx_date)
                    END
                    AS transaction_date,
                CASE
                    WHEN (CONCAT(et.description, st.description) != '') THEN CONCAT(et.description, st.description)
                    ELSE CONCAT('N/A')
                    END
                    AS description
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
        AND (
             CASE WHEN (CONCAT(c.display_name, s.display_name) != '')
                 THEN LOWER(CONCAT(c.display_name, s.display_name))
                 ELSE LOWER(CONCAT('N/A'))
                 END
             LIKE LOWER(CONCAT('%', :keyword, '%')) OR
             LOWER(dt.document_type_name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
             CASE WHEN (CONCAT(tt1.transaction_type_name, tt2.transaction_type_name) != '')
                 THEN LOWER(CONCAT(tt1.transaction_type_name, tt2.transaction_type_name))
                 ELSE LOWER(CONCAT('Comprobante Contable'))
                 END
             LIKE LOWER(CONCAT('%', :keyword, '%')) OR
             CASE WHEN (CONCAT(et.expense_transaction_number, st.sale_transaction_number) != '')
                 THEN LOWER(CONCAT(et.expense_transaction_number, st.sale_transaction_number))
                 ELSE LOWER(CONCAT(je.journal_entry_number))
                 END
             LIKE LOWER(CONCAT('%', :keyword, '%')))
        GROUP BY je.journal_entry_id, et.expense_transaction_number, st.sale_transaction_number, c.customer_id, s.supplier_id, c.display_name, s.display_name, c.company_name, s.company_name, c.company_phone_number, s.company_phone_number, c.tx_date, s.tx_date, je.journal_entry_accepted, dt.document_type_id, dt.document_type_name, tt1.transaction_type_id, tt1.transaction_type_name, tt2.transaction_type_id, tt2.transaction_type_name, et.tx_date, st.tx_date, et.expense_transaction_date, st.sale_transaction_date, et.description, st.description
        """
        , nativeQuery = true
    )
    fun findAll(@Param("companyId") companyId: Long, @Param("keyword") keyword: String, pageable: Pageable): Page<TransactionEntry>
}