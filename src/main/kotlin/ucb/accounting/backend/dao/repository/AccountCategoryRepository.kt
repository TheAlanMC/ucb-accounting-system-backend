package ucb.accounting.backend.dao.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ucb.accounting.backend.dao.AccountCategory

@Repository
interface AccountCategoryRepository: JpaRepository<AccountCategory, Long> {

    fun findByAccountCategoryIdAndStatusIsTrue (accountCategoryId: Long): AccountCategory?
    fun findAllByStatusIsTrueOrderByAccountCategoryIdAsc (): List<AccountCategory>

    fun findByAccountCategoryNameAndStatusIsTrue (accountCategoryName: String): AccountCategory?

}