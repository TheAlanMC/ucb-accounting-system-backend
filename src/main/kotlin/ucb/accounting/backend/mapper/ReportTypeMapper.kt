package ucb.accounting.backend.mapper

import ucb.accounting.backend.dao.ReportType
import ucb.accounting.backend.dto.PaymentTypeDto
import ucb.accounting.backend.dto.ReportTypeDto

class ReportTypeMapper {

    companion object{
        fun entityToDto(reportType: ReportType): ReportTypeDto {
            return ReportTypeDto(
                reportTypeId = reportType.reportTypeId,
                reportName = reportType.reportName
            )
        }
    }
}