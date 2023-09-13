package ucb.accounting.backend.dao.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ucb.accounting.backend.dao.JournalEntry

@Repository
interface JournalEntryRepository: JpaRepository<JournalEntry, Long> {
    fun findByCompanyIdAndJournalEntryNumberAndStatusTrue (companyId: Int, journalEntryNumber: Int): JournalEntry?

}