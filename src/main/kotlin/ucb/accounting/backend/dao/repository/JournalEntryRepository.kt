package ucb.accounting.backend.dao.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ucb.accounting.backend.dao.JournalEntry

@Repository
interface JournalEntryRepository: JpaRepository<JournalEntry, Long> {
    fun findByCompanyIdAndJournalEntryNumberAndStatusIsTrue (companyId: Int, journalEntryNumber: Int): JournalEntry?
    fun findFirstByCompanyIdAndStatusIsTrueOrderByJournalEntryNumberDesc (companyId: Int): JournalEntry?
    fun findAllByCompanyIdAndStatusIsTrue (companyId: Int): List<JournalEntry>
}