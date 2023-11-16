package ucb.accounting.backend.dao

import java.math.BigDecimal
import java.util.*
import javax.persistence.Entity
import javax.persistence.Id

@Entity
class GeneralLedger (
    @Id
    val id: Long,
    val subaccountId: Long,
    val subaccountCode: Int,
    val subaccountName: String,
    val transactionDate: Date,
    val gloss: String,
    val description: String,
    val debitAmountBs: BigDecimal,
    val creditAmountBs: BigDecimal
) {
    constructor(): this(0, 0, 0, "", Date(), "", "", BigDecimal.ZERO, BigDecimal.ZERO)
}