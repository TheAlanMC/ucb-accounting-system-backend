package ucb.accounting.backend.dao.repository

import org.springframework.data.jpa.repository.JpaRepository
import ucb.accounting.backend.dao.Subaccount

interface SubaccountRepository: JpaRepository<Subaccount, Long>{
    fun findAllByCompanyIdAndStatusIsTrueOrderBySubaccountIdAsc(companyId: Int): List<Subaccount>

    fun findBySubaccountIdAndStatusIsTrue(subaccountId: Long): Subaccount?

    fun findAllByCompanyIdAndAccountIdAndStatusIsTrueOrderBySubaccountIdAsc(companyId: Int, accountId: Int): List<Subaccount>

    fun findAllByAccountAccountSubgroupAccountGroupAccountCategoryAccountCategoryNameAndCompanyIdAndStatusIsTrueOrderBySubaccountIdAsc(accountCategoryName: String, companyId: Int): List<Subaccount>

    fun findFirstByAccountIdAndCompanyIdAndStatusIsTrueOrderBySubaccountCodeDesc(accountId: Int, companyId: Int): Subaccount?

    fun findAllByAccountAccountSubgroupAccountSubgroupNameAndCompanyIdAndStatusIsTrueOrderBySubaccountIdAsc(accountCategoryName: String, companyId: Int): List<Subaccount>
}
