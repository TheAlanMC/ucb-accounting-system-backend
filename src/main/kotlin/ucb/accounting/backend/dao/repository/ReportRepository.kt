package ucb.accounting.backend.dao.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import ucb.accounting.backend.dao.Report
import java.sql.Timestamp


interface ReportRepository:JpaRepository<Report, Long> {

    fun findAllByCompanyIdAndStatusIsTrueAndTxDateBetween(
        companyId: Int,
        txDate: Timestamp,
        txDate2: Timestamp,
        pageable: Pageable
    ): Page<Report>

    fun findByReportIdAndCompanyIdAndStatusIsTrue(reportId: Long, companyId: Int): Report

}