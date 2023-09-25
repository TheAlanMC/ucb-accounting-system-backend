package ucb.accounting.backend.dto

import java.math.BigDecimal

data class SubaccountTaxTypePartialDto (
    val taxType: TaxTypeDto,
    val subaccount: SubAccountDto,
    val taxRate: BigDecimal
)