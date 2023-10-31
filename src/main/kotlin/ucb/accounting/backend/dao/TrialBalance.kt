package ucb.accounting.backend.dao

import java.math.BigDecimal
import java.util.*
import javax.persistence.Entity
import javax.persistence.Id

@Entity
class TrialBalance (
    @Id
    val id: Long,
    val subaccountId: Int,
    val subaccountCode: Int,
    val subaccountName: String,
    val debitAmountBs: BigDecimal,
    val creditAmountBs: BigDecimal
) {
    constructor() : this(0, 0, 0, "", BigDecimal.ZERO, BigDecimal.ZERO)
}
