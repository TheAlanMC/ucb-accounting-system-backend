package ucb.accounting.backend.dto

import jdk.jfr.DataAmount
import java.math.BigDecimal

data class FinancialStatementReportDto (
    val financialStatementDetails: List<FinancialStatementReportDetailDto>,
    val description: String,
    val totalAmountBs: BigDecimal,
)

