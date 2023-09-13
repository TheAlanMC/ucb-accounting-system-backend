package ucb.accounting.backend.dao.repository

import org.springframework.data.jpa.repository.JpaRepository
import ucb.accounting.backend.dao.AccountSubgroup

interface AccountSubGroupRepository: JpaRepository<AccountSubgroup, Long> {
    fun findAllByCompanyIdAndAccountGroupIdAndStatusIsTrue (companyId: Long, accountGroupId: Long): List<AccountSubgroup>
}