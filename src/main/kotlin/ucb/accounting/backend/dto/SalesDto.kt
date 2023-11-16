package ucb.accounting.backend.dto

import java.math.BigDecimal

data class SalesDto (
    val name: String,
    val total: BigDecimal
)