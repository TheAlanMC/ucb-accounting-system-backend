package ucb.accounting.backend.dto

import java.sql.Timestamp

data class JournalEntryDto(
    val journalEntryId: Long,
    val companyId: Int,
    val documentTypeId: Int,
    val journalEntryNumber: Int,
    val entryDate: Timestamp,
    val gloss: String,
    val journalEntryAccepted: Boolean,
    val status: Boolean,
    val company: CompanyDto?,
    val documentType: DocumentTypeDto?
)