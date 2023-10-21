package ucb.accounting.backend.dao

import java.math.BigDecimal
import java.sql.Date
import javax.persistence.Entity
import javax.persistence.Id

@Entity
data class TransactionEntry(
    @Id
    val journalEntryId: Long,
    val transactionNumber: Int,
    val clientId: Long,
    val displayName: String,
    val companyName: String,
    val companyPhoneNumber: String,
    val clientCreationDate: Date,
    val transactionAccepted: Boolean,
    val documentTypeId: Int,
    val documentTypeName: String,
    val transactionTypeId: Int,
    val transactionTypeName: String,
    val totalAmountBs: BigDecimal,
    val creationDate: Date,
    val transactionDate: Date,
    val description: String,
) {
    constructor() : this(0, 0, 0, "", "", "", Date(0), false, 0, "", 0, "", BigDecimal(0), Date(0), Date(0), "")
}
