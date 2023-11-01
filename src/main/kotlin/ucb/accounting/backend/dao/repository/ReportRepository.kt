package ucb.accounting.backend.dao.repository

import org.springframework.data.jpa.repository.JpaRepository
import ucb.accounting.backend.dao.Report


interface ReportRepository:JpaRepository<Report, Long> {

}