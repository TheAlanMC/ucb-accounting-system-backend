package ucb.accounting.backend.dao.repository

import org.springframework.data.jpa.repository.JpaRepository
import ucb.accounting.backend.dao.Subaccount

interface SubaccountRepository: JpaRepository<Subaccount, Long> {
    fun findAllByCompanyIdAndAccountIdAndStatusIsTrue (companyId: Int, accountId: Int): List<Subaccount>

    fun findBySubaccountIdAndStatusIsTrue (subaccountId: Long): Subaccount?

    fun findAllByAccountAccountSubgroupAccountGroupAccountCategoryAccountCategoryNameAndCompanyIdAndStatusIsTrue (accountCategoryName: String, companyId: Int): List<Subaccount>
}