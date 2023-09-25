package ucb.accounting.backend.dao.repository

import org.springframework.data.jpa.repository.JpaRepository
import ucb.accounting.backend.dao.SubaccountTaxType

interface SubaccountTaxTypeRepository: JpaRepository<SubaccountTaxType, Long> {

    fun findAllByCompanyIdAndStatusIsTrue(companyId: Long): List<SubaccountTaxType>

    fun findBySubaccount_SubaccountIdAndTaxType_TaxTypeIdAndStatusIsTrue(subaccountId: Long, taxTypeId: Long): SubaccountTaxType?
}