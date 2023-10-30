package ucb.accounting.backend.dto

import java.math.BigDecimal
import java.util.*


data class JournalBookReportDto (
    val journalEntryId: Int?,
    val documentType: DocumentTypeDto?,
    val journalEntryNumber: Int?,
    val gloss: String?,
    val description: String?,
    val transactionDate: Date?,
    val transactionDetails: List<JournalBookTransactionDetailDto>?,
    val totalDebitAmountBs: BigDecimal?,
    val totalCreditAmountBs: BigDecimal?
)