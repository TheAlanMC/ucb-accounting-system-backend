package ucb.accounting.backend.dto

import java.sql.Date

class JournalEntryPartialDto (
    val journalEntryId: Int?,
    val transactionNumber: Int?,
    val client: ClientPartialDto?,
    val transactionAccepted: Boolean?,
    val documentType: DocumentTypeDto?,
    val transactionType: TransactionTypeDto?,
    val gloss: String?,
    val description: String?,
    val transactionDate: Date?,
    val attachments: List<AttachmentDownloadDto>?,
    val transactionDetails: List<TransactionDetailPartialDto>?
)
