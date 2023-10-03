package ucb.accounting.backend.dto

import java.math.BigDecimal

data class SubaccountTaxTypeDto (
    val taxTypeId: Long?,
    val subaccountId: Long?,
    val taxRate: BigDecimal?
)