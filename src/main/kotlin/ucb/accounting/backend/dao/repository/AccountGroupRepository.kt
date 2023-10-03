package ucb.accounting.backend.dao.repository

import org.springframework.data.jpa.repository.JpaRepository
import ucb.accounting.backend.dao.AccountGroup

interface AccountGroupRepository: JpaRepository<AccountGroup, Long> {

    fun findAllByCompanyIdAndStatusIsTrue (companyId: Int): List<AccountGroup>
    fun findAllByCompanyIdAndAccountCategoryIdAndStatusIsTrue (companyId: Int, accountCategoryId: Int): List<AccountGroup>
    fun findByAccountGroupIdAndStatusIsTrue(id: Long): AccountGroup?

}