package ucb.accounting.backend.dto

import java.sql.Date

data class JournalEntryDto(
    val documentTypeId: Long?,
    val journalEntryNumber: Int?,
    val gloss: String?,
    val description: String?,
    val transactionDate: Date?,
    val attachments: List<AttachmentDto>?,
    val transactionDetails: List<TransactionDetailDto>?
)