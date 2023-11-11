package ucb.accounting.backend.dto

import java.util.Date

data class OpeningBalanceDto (
    val openingBalanceDate: Date,
    val financialStatementReports: List<FinancialStatementReportDetailDto>
)