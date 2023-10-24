package ucb.accounting.backend.dto

import java.util.*

data class ReportDto <T>(
    val company: CompanyDto,
    val startDate: Date,
    val endDate: Date,
    val reportData: T
)