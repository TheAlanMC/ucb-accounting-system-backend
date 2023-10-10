package ucb.accounting.backend.dao.repository

import org.springframework.data.jpa.repository.JpaRepository
import ucb.accounting.backend.dao.Account

interface AccountRepository: JpaRepository<Account, Long> {
    fun findAllByCompanyIdAndAccountSubgroupIdAndStatusIsTrueOrderByAccountIdAsc (companyId: Int, accountSubgroupId: Int): List<Account>

    fun findAllByCompanyIdAndStatusIsTrueOrderByAccountIdAsc (companyId: Int): List<Account>

    fun findByAccountIdAndStatusIsTrue(accountId: Long): Account?

    fun findByAccountNameAndCompanyIdAndStatusIsTrue(accountName: String, companyId: Int): Account?
}