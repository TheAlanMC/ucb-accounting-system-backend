package ucb.accounting.backend.dao.repository

import org.springframework.data.jpa.repository.JpaRepository
import ucb.accounting.backend.dao.Subaccount

interface SubAccountRepository: JpaRepository<Subaccount, Long> {
    fun findAllByCompanyIdAndAccountIdAndStatusIsTrue (companyId: Long, accountId: Long): List<Subaccount>
}