package ucb.accounting.backend.dao.repository

import org.springframework.data.jpa.repository.JpaRepository
import ucb.accounting.backend.dao.AccountGroup

interface AccountGroupRepository: JpaRepository<AccountGroup, Long> {
    fun findAllByCompanyIdAndAccountCategoryIdAndStatusIsTrue (companyId: Long, accountCategoryId: Long): List<AccountGroup>
}