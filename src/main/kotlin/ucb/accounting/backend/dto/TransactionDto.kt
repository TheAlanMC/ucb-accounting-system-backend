package ucb.accounting.backend.dto

import java.math.BigDecimal
import java.sql.Date

data class TransactionDto (
    val journalEntryId: Int?,
    val transactionNumber: Int?,
    val client: ClientPartialDto?,
    val transactionAccepted: Boolean?,
    val documentType: DocumentTypeDto?,
    val transactionType: TransactionTypeDto?,
    val totalAmountBs: BigDecimal?,
    val creationDate: Date?,
    val transactionDate: Date?,
    val description: String?,
)