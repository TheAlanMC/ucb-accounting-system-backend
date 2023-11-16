package ucb.accounting.backend.dao

import java.math.BigDecimal
import javax.persistence.Entity
import javax.persistence.Id
@Entity
class SubaccountPartial (
    @Id
    val subaccountId: Long,
    val subaccountCode: Int,
    val subaccountName: String,
){
    constructor() : this(0, 0, "")
}