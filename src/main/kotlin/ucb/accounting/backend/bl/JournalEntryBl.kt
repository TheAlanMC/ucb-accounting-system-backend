package ucb.accounting.backend.bl

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import ucb.accounting.backend.dao.JournalEntry
import ucb.accounting.backend.dao.repository.JournalEntryRepository
import ucb.accounting.backend.dto.JournalEntryDto

@Controller
class JournalEntryBl @Autowired constructor(
    private val journalEntryRepository: JournalEntryRepository,
) {
    fun createJournalEntry(companyId: Int, request: JournalEntryDto): Long{
        val journalEntry = mapToJournalEntry(companyId, request)

        //val savedJournalEntry = journalEntryRepository.save(journalEntry)
        println(journalEntry)
        return 1
    }
    private fun mapToJournalEntry(companyId: Int, request: JournalEntryDto): JournalEntry {
        val journalEntry = JournalEntry()
        journalEntry.companyId = companyId
        journalEntry.documentTypeId = request.documentTypeId
        journalEntry.journalEntryNumber = request.journalEntryNumber
        journalEntry.gloss = request.gloss
        journalEntry.journalEntryAccepted = false
        journalEntry.status = true
        return journalEntry
    }
}