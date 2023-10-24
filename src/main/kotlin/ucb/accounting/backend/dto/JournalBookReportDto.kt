package ucb.accounting.backend.dto

import java.sql.Date

data class JournalBookReportDto (
    val journalEntryId: Int?,
    val documentType: DocumentTypeDto?,
    val journalEntryNumber: Int?,
    val gloss: String?,
    val description: String?,
    val transactionDate: Date?,
    val attachments: List<AttachmentDownloadDto>?,
    val transactionDetails: List<JournalBookTransactionDetailDto>?
)