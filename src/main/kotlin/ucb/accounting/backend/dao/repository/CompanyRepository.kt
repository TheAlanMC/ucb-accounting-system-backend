package ucb.accounting.backend.dao.repository

import org.springframework.data.jpa.repository.JpaRepository
import ucb.accounting.backend.dao.Company

interface CompanyRepository: JpaRepository<Company, Long> {
    fun findByCompanyIdAndStatusTrue (companyId: Long): Company?
}