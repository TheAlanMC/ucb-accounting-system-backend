package ucb.accounting.backend.dto

import java.sql.Date

class JournalEntryPartialDto (
    val documentType: DocumentTypeDto?,
    val journalEntryNumber: Int?,
    val gloss: String?,
    val description: String?,
    val transactionDate: Date?,
    val attachments: List<AttachmentDto>?,
    val transactionDetails: List<TransactionDetailDto>?
    )