package ucb.accounting.backend.dao.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository
import ucb.accounting.backend.dao.KcUserCompany

@Repository
interface KcUserCompanyRepository: PagingAndSortingRepository<KcUserCompany, Long> {
    fun findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue (kcUuid: String, companyId: Long): KcUserCompany?
    fun findAllByKcUser_KcUuidAndStatusIsTrue (kcUuid: String): List<KcUserCompany>
    fun findAllByCompany_CompanyIdAndStatusIsTrue (companyId: Long, pageable: Pageable): Page<KcUserCompany>

}
