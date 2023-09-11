package ucb.accounting.backend.dto

import java.sql.Date

data class TransactionDto(
    val transactionId: Long,
    val companyId: Int,
    val journalEntryId: Int,
    val transactionDate: Date,
    val description: String,
    val status: Boolean,
    val company: CompanyDto?,
    val journalEntry: JournalEntryDto?,
    val transactionDetails: List<TransactionDetailDto>?,
    val transactionAttachments: List<TransactionAttachmentDto>?
)