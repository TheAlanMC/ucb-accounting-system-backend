package ucb.accounting.backend.dao

import java.math.BigDecimal
import java.sql.Date
import javax.persistence.Entity
import javax.persistence.Id

@Entity
data class SaleTransactionEntry (
    @Id
    val saleTransactionId: Long,
    val transactionTypeId: Int,
    val transactionTypeName: String,
    val saleTransactionNumber: Int,
    val saleTransactionDate: Date,
    val customerId: Long,
    val displayName: String,
    val companyName: String,
    val companyPhoneNumber: String,
    val creationDate: Date,
    val gloss: String,
    val totalAmountBs: BigDecimal,
    val saleTransactionAccepted: Boolean,

) {
    constructor() : this(0, 0, "", 0, Date(0), 0, "", "", "", Date(0), "", BigDecimal(0), false)
}