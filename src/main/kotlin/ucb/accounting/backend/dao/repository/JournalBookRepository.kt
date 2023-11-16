package ucb.accounting.backend.dao.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.data.repository.query.Param
import ucb.accounting.backend.dao.JournalBook
import java.util.Date

interface JournalBookRepository: JpaRepository<JournalBook, Long> {

    @Query(value =
    """
        SELECT  ROW_NUMBER() OVER (ORDER BY t.transaction_date) AS id,
                je.journal_entry_id AS journal_entry_id,
                dt.document_type_id AS document_type_id,
                dt.document_type_name AS document_type_name,
                je.journal_entry_number AS journal_entry_number,
                je.gloss AS gloss,
                t.description AS description,
                t.transaction_date AS transaction_date,
                t.transaction_id AS transaction_id,
                s.subaccount_id AS subaccount_id,
                s.subaccount_code AS subaccount_code,
                s.subaccount_name AS subaccount_name,
                td.debit_amount_bs AS debit_amount_bs,
                td.credit_amount_bs AS credit_amount_bs
        FROM journal_entry je
        JOIN document_type dt ON je.document_type_id = dt.document_type_id
        JOIN transaction t ON je.journal_entry_id = t.journal_entry_id
        JOIN transaction_detail td ON t.transaction_id = td.transaction_id
        JOIN subaccount s ON td.subaccount_id = s.subaccount_id
        AND je.journal_entry_accepted = TRUE
        AND je.status = TRUE
        AND je.company_id = :companyId
        AND (t.transaction_date BETWEEN :dateFrom AND :dateTo OR CAST(:dateFrom AS DATE) IS NULL OR CAST(:dateTo AS DATE) IS NULL)
        ORDER BY t.transaction_date
    """, nativeQuery = true)
    fun findAllByCompanyIdAndStatusIsTrue (
        @Param("companyId") companyId: Int,
        @Param("dateFrom") dateFrom: Date?,
        @Param("dateTo") dateTo: Date?,
    ): List<JournalBook>
}