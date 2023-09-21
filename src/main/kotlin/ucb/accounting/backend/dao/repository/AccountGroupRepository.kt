package ucb.accounting.backend.dao.repository

import org.springframework.data.jpa.repository.JpaRepository
import ucb.accounting.backend.dao.AccountGroup
import ucb.accounting.backend.dto.AccoGroupDto

interface AccountGroupRepository: JpaRepository<AccountGroup, Long> {

    fun findAllByStatusIsTrue (): List<AccountGroup>
    fun findAllByCompanyIdAndAccountCategoryIdAndStatusIsTrue (companyId: Int, accountCategoryId: Int): List<AccountGroup>

}