package ucb.accounting.backend.dto

import java.math.BigDecimal

data class WorksheetReportDetailDto (
    val subaccount: SubaccountDto,
    val balanceDebtor: BigDecimal,
    val balanceCreditor: BigDecimal,
    val incomeStatementExpense: BigDecimal,
    val incomeStatementIncome: BigDecimal,
    val balanceSheetAsset: BigDecimal,
    val balanceSheetLiability: BigDecimal,
)