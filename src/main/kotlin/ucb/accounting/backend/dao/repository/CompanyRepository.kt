package ucb.accounting.backend.dao.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ucb.accounting.backend.dao.Company

@Repository
interface CompanyRepository: JpaRepository<Company, Long> {
    fun findByCompanyIdAndStatusIsTrue (companyId: Long): Company?

}