package ucb.accounting.backend.dao.repository

import org.springframework.data.jpa.repository.JpaRepository
import ucb.accounting.backend.dao.Subaccount

interface SubaccountRepository: JpaRepository<Subaccount, Long>{
    fun findAllByCompanyIdAndStatusIsTrue(companyId: Int): List<Subaccount>

    fun findBySubaccountIdAndStatusIsTrue(subaccountId: Long): Subaccount?

    fun findAllByCompanyIdAndAccountIdAndStatusIsTrue(companyId: Int, accountId: Int): List<Subaccount>

    fun findAllByAccountAccountSubgroupAccountGroupAccountCategoryAccountCategoryNameAndCompanyIdAndStatusIsTrueOrderBySubaccountIdAsc(accountCategoryName: String, companyId: Int): List<Subaccount>
    fun findByCompanyId(companyId: Long): List<Subaccount>

    fun findFirstByCompanyIdAndSubaccountNameAndStatusIsTrue(companyId: Int, subaccountName: String): Subaccount?
    fun findAllByCompanyIdAndAccountIdAndStatusIsTrueOrderBySubaccountIdAsc(companyId: Int, toInt: Int): List<Subaccount>
    fun findFirstByAccountIdAndCompanyIdAndStatusIsTrueOrderBySubaccountCodeDesc(toInt: Int, toInt1: Int): Subaccount?

    fun findAllByAccountAccountSubgroupAccountSubgroupNameAndCompanyIdAndStatusIsTrueOrderBySubaccountIdAsc(accountSubgroupName: String, companyId: Int): List<Subaccount>
    fun findAllByCompanyIdAndStatusIsTrueOrderBySubaccountIdAsc(toInt: Int): List<Subaccount>
}
