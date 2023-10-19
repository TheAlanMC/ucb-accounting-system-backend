package ucb.accounting.backend.dao.repository

import org.springframework.data.jpa.repository.JpaRepository
import ucb.accounting.backend.dao.AccountSubgroup

interface AccountSubgroupRepository: JpaRepository<AccountSubgroup, Long> {
    fun findAllByCompanyIdAndAccountGroupIdAndStatusIsTrueOrderByAccountSubgroupIdAsc (companyId: Int, accountGroupId: Int): List<AccountSubgroup>

    fun findByAccountSubgroupIdAndStatusIsTrue(id: Long): AccountSubgroup?

    fun findAllByCompanyIdAndStatusIsTrueOrderByAccountSubgroupIdAsc (companyId: Int): List<AccountSubgroup>
}