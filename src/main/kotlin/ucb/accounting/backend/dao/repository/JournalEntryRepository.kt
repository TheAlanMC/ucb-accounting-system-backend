package ucb.accounting.backend.dao.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import ucb.accounting.backend.dao.DocumentType
import ucb.accounting.backend.dao.JournalEntry
import java.util.Date

@Repository
interface JournalEntryRepository: PagingAndSortingRepository<JournalEntry, Long> {
    fun findByCompanyIdAndJournalEntryNumberAndStatusIsTrue (companyId: Int, journalEntryNumber: Int): JournalEntry?
    fun findFirstByCompanyIdAndStatusIsTrueOrderByJournalEntryNumberDesc (companyId: Int): JournalEntry?
    fun findAllByCompanyIdAndStatusIsTrue (companyId: Int): List<JournalEntry>
    fun findByJournalEntryIdAndStatusIsTrue(journalEntryId: Long): JournalEntry?

    fun findAllByJournalEntryIdIsInAndStatusIsTrue(journalEntryId: List<Long>, pageable: Pageable): Page<JournalEntry>

    @Query("SELECT je FROM JournalEntry je WHERE je.companyId = :companyId AND je.documentTypeId = :documentTypeId AND je.status = true AND je.txDate BETWEEN :startDate AND :endDate")
    fun findByCompanyIdAndDocumentTypeIdAndTxDate(
        @Param("companyId") companyId: Int,
        @Param("documentTypeId") documentType: Int,
        @Param("startDate") startDate: Date,
        @Param("endDate") endDate: Date
    ): List<JournalEntry>
}