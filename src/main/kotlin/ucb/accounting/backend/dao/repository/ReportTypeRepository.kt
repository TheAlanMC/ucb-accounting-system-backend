package ucb.accounting.backend.dao.repository

import org.springframework.data.jpa.repository.JpaRepository
import ucb.accounting.backend.dao.ReportType

interface ReportTypeRepository: JpaRepository<ReportType, Long> {
    fun findAllByStatusIsTrue(): List<ReportType>
    fun findByReportTypeIdAndStatusIsTrue(reportTypeId: Long): ReportType?
}