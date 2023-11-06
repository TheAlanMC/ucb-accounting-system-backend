package ucb.accounting.backend.dto

import java.sql.Timestamp

data class GeneratedReportDto (
    val reportId: Long,
    val dateTime: Timestamp,
    val reportDescription: String,
    val reportType: ReportTypeDto,
    val user: UserDto,
    val isFinancialStatement: Boolean,
    )