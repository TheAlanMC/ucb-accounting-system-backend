package ucb.accounting.backend.mapper

import ucb.accounting.backend.dao.JournalEntry
import ucb.accounting.backend.dto.JournalEntryDto

class JournalEntryMapper {

    companion object {
        fun entityToDto(journalEntry: JournalEntry): JournalEntryDto {
            return JournalEntryDto(
                documentTypeId = journalEntry.documentTypeId.toLong(),
                journalEntryNumber = journalEntry.journalEntryNumber,
                gloss = journalEntry.gloss,
                description = journalEntry.transaction!!.description,
                transactionDate = journalEntry.transaction!!.transactionDate,
                attachments = journalEntry.company!!.attachments!!.map { AttachmentMapper.entityToDto(it) },
                transactionDetails = journalEntry.transaction!!.transactionDetails!!.map { TransactionDetailMapper.entityToDto(it) }
            )
        }
    }

}