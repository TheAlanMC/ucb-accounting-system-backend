package ucb.accounting.backend.dao.repository

import org.springframework.data.jpa.repository.JpaRepository
import ucb.accounting.backend.dao.AccountGroup

interface AccountGroupRepository: JpaRepository<AccountGroup, Long> {

    fun findAllByCompanyIdAndStatusIsTrueOrderByAccountGroupIdAsc (companyId: Int): List<AccountGroup>
    fun findAllByCompanyIdAndAccountCategoryIdAndStatusIsTrueOrderByAccountGroupIdAsc (companyId: Int, accountCategoryId: Int): List<AccountGroup>
    fun findByAccountGroupIdAndStatusIsTrue(id: Long): AccountGroup?

    fun findFirstByCompanyIdAndAccountGroupNameAndStatusIsTrue (companyId: Int, accountGroupName: String): AccountGroup?

}