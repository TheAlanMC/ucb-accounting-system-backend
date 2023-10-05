package ucb.accounting.backend.dao.repository

import org.springframework.data.jpa.repository.JpaRepository
import ucb.accounting.backend.dao.Subaccount

interface SubaccountRepository: JpaRepository<Subaccount, Long>{
    fun findAllByCompanyIdAndStatusIsTrue(companyId: Int): List<Subaccount>

    fun findBySubaccountIdAndStatusIsTrue(subaccountId: Long): Subaccount?

    fun findAllByCompanyIdAndAccountIdAndStatusIsTrue(companyId: Int, accountId: Int): List<Subaccount>

    fun findAllByAccountAccountSubgroupAccountGroupAccountCategoryAccountCategoryNameAndCompanyIdAndStatusIsTrue(accountCategoryName: String, companyId: Int): List<Subaccount>

    fun findFirstByAccountIdAndCompanyIdAndStatusIsTrueOrderBySubaccountCodeDesc(accountId: Int, companyId: Int): Subaccount?

    fun findAllByAccountAccountSubgroupAccountSubgroupNameAndCompanyIdAndStatusIsTrue(accountCategoryName: String, companyId: Int): List<Subaccount>
}
