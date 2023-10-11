package ucb.accounting.backend.dto

import java.math.BigDecimal
import java.sql.Date

data class TransactionDto (
    val transactionId: Int?,
    val transactionType: TransactionTypeDto?,
    val transactionNumber: Int?,
    val transactionDate: Date?,
    val client: ClientPartialDto?,
    val gloss: String?,
    val totalAmountBs: BigDecimal?,
    val transactionAccepted: Boolean?,
    val documentTypeDto: DocumentTypeDto?,
    val journalEntryId: Int?
    )