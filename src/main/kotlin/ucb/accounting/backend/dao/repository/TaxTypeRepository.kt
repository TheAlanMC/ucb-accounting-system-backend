package ucb.accounting.backend.dao.repository

import org.springframework.data.jpa.repository.JpaRepository
import ucb.accounting.backend.dao.TaxType

interface TaxTypeRepository: JpaRepository<TaxType, Long> {

    fun findAllByStatusIsTrue(): List<TaxType>

    fun findByTaxTypeIdAndStatusIsTrue(taxTypeId: Long): TaxType?
}