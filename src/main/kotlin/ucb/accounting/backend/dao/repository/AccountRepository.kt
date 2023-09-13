package ucb.accounting.backend.dao.repository

import org.springframework.data.jpa.repository.JpaRepository
import ucb.accounting.backend.dao.Account

interface AccountRepository: JpaRepository<Account, Long> {
    fun findAllByCompanyIdAndAccountSubgroupIdAndStatusIsTrue (companyId: Int, accountSubgroupId: Int): List<Account>
}