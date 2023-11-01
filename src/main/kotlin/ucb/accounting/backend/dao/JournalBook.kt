package ucb.accounting.backend.dao

import java.math.BigDecimal
import java.util.*
import javax.persistence.Entity
import javax.persistence.Id

@Entity
class JournalBook (
    @Id
    val id: Long,
    val journalEntryId: Long,
    val documentTypeId: Int,
    val documentTypeName: String,
    val journalEntryNumber: Int,
    val gloss: String,
    val description: String,
    val transactionDate: Date,
    val subaccountId: Int,
    val subaccountCode: Int,
    val subaccountName: String,
    val debitAmountBs: BigDecimal,
    val creditAmountBs: BigDecimal
) {
    constructor() : this(0,0, 0, "", 0, "", "", Date(), 0, 0, "", BigDecimal.ZERO, BigDecimal.ZERO)
}
