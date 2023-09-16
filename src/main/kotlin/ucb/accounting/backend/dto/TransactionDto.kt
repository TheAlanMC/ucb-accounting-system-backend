package ucb.accounting.backend.dto

import java.sql.Date

data class TransactionDto(
    val transactionId: Long,
    val journalEntryId: Int,
    val transactionDate: Date,
    val description: String,
    val status: Boolean,
)