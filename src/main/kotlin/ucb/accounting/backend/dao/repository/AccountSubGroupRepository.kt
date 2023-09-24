package ucb.accounting.backend.dao.repository

import org.springframework.data.jpa.repository.JpaRepository
import ucb.accounting.backend.dao.AccountSubgroup

interface AccountSubGroupRepository: JpaRepository<AccountSubgroup, Long> {
    fun findAllByCompanyIdAndAccountGroupIdAndStatusIsTrue (companyId: Int, accountGroupId: Int): List<AccountSubgroup>

    fun findByAccountSubgroupIdAndStatusIsTrue(id: Long): AccountSubgroup?

    fun findAllByCompanyIdAndStatusIsTrue(companyId: Int): List<AccountSubgroup>
}