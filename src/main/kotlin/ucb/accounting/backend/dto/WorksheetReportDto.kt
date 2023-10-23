package ucb.accounting.backend.dto

import java.math.BigDecimal

data class WorksheetReportDto (
    val  worksheetDetails: List<WorksheetReportDetailDto>,
    val totalBalanceDebtor: BigDecimal,
    val totalBalanceCreditor: BigDecimal,
    val totalIncomeStatementExpense: BigDecimal,
    val totalIncomeStatementIncome: BigDecimal,
    val totalIncomeStatementNetIncome: BigDecimal,
    val totalBalanceSheetAsset: BigDecimal,
    val totalBalanceSheetLiability: BigDecimal,
    val totalBalanceSheetEquity: BigDecimal
)

