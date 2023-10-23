package ucb.accounting.backend.dao.repository

import org.springframework.data.jpa.repository.JpaRepository
import ucb.accounting.backend.dao.CurrencyType

interface CurrencyTypeRepository: JpaRepository<CurrencyType, Long> {
    fun findByCurrencyCodeAndStatusIsTrue(currencyCode: String): CurrencyType?
}