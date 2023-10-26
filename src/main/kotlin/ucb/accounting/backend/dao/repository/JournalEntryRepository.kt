package ucb.accounting.backend.dao.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import ucb.accounting.backend.dao.JournalEntry
import java.util.Date

@Repository
interface JournalEntryRepository: PagingAndSortingRepository<JournalEntry, Long> {
    fun findByCompanyIdAndJournalEntryNumberAndStatusIsTrue (companyId: Int, journalEntryNumber: Int): JournalEntry?
    fun findFirstByCompanyIdAndStatusIsTrueOrderByJournalEntryNumberDesc (companyId: Int): JournalEntry?
    fun findAllByCompanyIdAndStatusIsTrue (companyId: Int): List<JournalEntry>
    fun findByJournalEntryIdAndStatusIsTrue(journalEntryId: Long): JournalEntry?

    fun findAllByJournalEntryIdIsInAndStatusIsTrue(journalEntryId: List<Long>, pageable: Pageable): Page<JournalEntry>

    @Query(value = "SELECT " +
            "je.tx_date AS fecha, " +
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
            "AND je.document_type_id = :documentTypeId " +
            "AND je.status = true " +
            "And je.journal_entry_accepted = true " +
            "AND t.transaction_date BETWEEN :startDate AND :endDate", nativeQuery = true)
    fun getJournalBookData(
        @Param("companyId") companyId: Int,
        @Param("documentTypeId") documentType: Int,
        @Param("startDate") startDate: Date,
        @Param("endDate") endDate: Date
    ): List<Map<String, Any>>



    @Query("SELECT je FROM JournalEntry je WHERE je.companyId = :companyId AND je.documentTypeId = :documentTypeId AND je.status = true AND MONTH(je.txDate) = :month")
    fun findByCompanyIdAndDocumentTypeIdAndMonth(
        @Param("companyId") companyId: Int,
        @Param("documentTypeId") documentType: Int,
        @Param("month") month: Int
    ): List<JournalEntry>

}