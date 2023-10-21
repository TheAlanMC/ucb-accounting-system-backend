package ucb.accounting.backend.dao

import java.math.BigDecimal
import java.sql.Date
import javax.persistence.Entity
import javax.persistence.Id

@Entity
data class ExpenseTransactionEntry (
    @Id
    val expenseTransactionId: Long,
    val transactionTypeId: Int,
    val transactionTypeName: String,
    val expenseTransactionNumber: Int,
    val expenseTransactionDate: Date,
    val supplierId: Long,
    val displayName: String,
    val companyName: String,
    val companyPhoneNumber: String,
    val creationDate: Date,
    val gloss: String,
    val totalAmountBs: BigDecimal,
    val expenseTransactionAccepted: Boolean,

) {
    constructor() : this(0, 0, "", 0, Date(0), 0, "", "", "", Date(0), "", BigDecimal(0), false)
}