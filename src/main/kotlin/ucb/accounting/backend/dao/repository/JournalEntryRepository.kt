package ucb.accounting.backend.dao.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import ucb.accounting.backend.dao.JournalEntry
import java.util.*

@Repository
interface JournalEntryRepository: PagingAndSortingRepository<JournalEntry, Long> {

    @Query(value =
    """
        SELECT *
        FROM journal_entry je
        LEFT JOIN sale_transaction st ON st.journal_entry_id = je.journal_entry_id
        LEFT JOIN expense_transaction et ON et.journal_entry_id = je.journal_entry_id
        
        WHERE je.company_id = :companyId
        AND je.status = TRUE
        AND st.journal_entry_id IS NULL
        AND et.journal_entry_id IS NULL
        AND je.journal_entry_number = :journalEntryNumber
    """
        , nativeQuery = true)
    fun findByCompanyIdAndJournalEntryNumberAndStatusIsTrue (@Param("companyId") companyId: Int, @Param("journalEntryNumber") journalEntryNumber: Int): JournalEntry?

    @Query(value =
        """
            SELECT *
            FROM journal_entry je
            LEFT JOIN sale_transaction st ON st.journal_entry_id = je.journal_entry_id
            LEFT JOIN expense_transaction et ON et.journal_entry_id = je.journal_entry_id
            
            WHERE je.company_id = :companyId
            AND je.status = TRUE
            AND st.journal_entry_id IS NULL
            AND et.journal_entry_id IS NULL
            ORDER by journal_entry_number DESC LIMIT 1;
        """
        , nativeQuery = true)
    fun findFirstByCompanyIdAndStatusIsTrueOrderByJournalEntryNumberDesc (@Param("companyId") companyId: Int): JournalEntry?

    fun findAllByCompanyIdAndStatusIsTrue (companyId: Int): List<JournalEntry>
    fun findByJournalEntryIdAndStatusIsTrue(journalEntryId: Long): JournalEntry?

    fun findAllByJournalEntryIdIsInAndStatusIsTrue(journalEntryId: List<Long>, pageable: Pageable): Page<JournalEntry>

    @Query(
        """
        SELECT *
        FROM journal_entry je
        JOIN transaction t ON je.journal_entry_id = t.journal_entry_id
        AND je.journal_entry_accepted = TRUE
        AND je.status = TRUE
        AND je.company_id = :companyId
        AND t.transaction_date BETWEEN :dateFrom AND :dateTo
        """,
        nativeQuery = true
    )
    fun findAllByCompanyIdAndStatusIsTrue(
        @Param("companyId") companyId: Int,
        @Param("dateFrom") dateFrom: Date,
        @Param("dateTo") dateTo: Date,
        page: Pageable
    ): Page<JournalEntry>
    @Query(value = "SELECT " +
            "t.transaction_date AS fecha, " +
            "je.journal_entry_number AS numero_comprobante, " +
            "s.subaccount_code AS codigo, " +
            "s.subaccount_name AS nombre, " +
            "t.description AS detalle, " +
            "td.debit_amount_bs AS debe, " +
            "td.credit_amount_bs AS haber " +
            "FROM journal_entry je " +
            "INNER JOIN transaction t ON je.journal_entry_id = t.journal_entry_id " +
            "INNER JOIN transaction_detail td ON t.transaction_id = td.transaction_id " +
            "INNER JOIN subaccount s ON td.subaccount_id = s.subaccount_id " +
            "WHERE je.company_id = :companyId " +
//            "AND je.document_type_id = :documentTypeId " +
            "AND je.status = true " +
            "And je.journal_entry_accepted = true " +
            "AND t.transaction_date BETWEEN :startDate AND :endDate " +
            "ORDER BY t.transaction_date ASC"
        , nativeQuery = true)
    fun getJournalBookData(
        @Param("companyId") companyId: Int,
//        @Param("documentTypeId") documentType: Int,
        @Param("startDate") startDate: Date,
        @Param("endDate") endDate: Date
    ): List<Map<String, Any>>


}