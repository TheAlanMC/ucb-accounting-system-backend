package ucb.accounting.backend.dao.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ucb.accounting.backend.dao.KcUserCompany

@Repository
interface KcUserCompanyRepository: JpaRepository<KcUserCompany, Long> {
    fun findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue (kcUuid: String, companyId: Long): KcUserCompany?
    fun findAllByKcUser_KcUuidAndStatusIsTrue (kcUuid: String): List<KcUserCompany>
    fun findAllByCompany_CompanyIdAndStatusIsTrue (companyId: Long): List<KcUserCompany>

}
