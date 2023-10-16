package ucb.accounting.backend.dao.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository
import ucb.accounting.backend.dao.JournalEntry

@Repository
interface JournalEntryRepository: PagingAndSortingRepository<JournalEntry, Long> {
    fun findByCompanyIdAndJournalEntryNumberAndStatusIsTrue (companyId: Int, journalEntryNumber: Int): JournalEntry?
    fun findFirstByCompanyIdAndStatusIsTrueOrderByJournalEntryNumberDesc (companyId: Int): JournalEntry?
    fun findAllByCompanyIdAndStatusIsTrue (companyId: Int): List<JournalEntry>
    fun findByJournalEntryIdAndStatusIsTrue(journalEntryId: Long): JournalEntry?

    fun findAllByJournalEntryIdIsInAndStatusIsTrue(journalEntryId: List<Long>, pageable: Pageable): Page<JournalEntry>
}