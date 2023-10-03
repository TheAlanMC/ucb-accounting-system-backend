package ucb.accounting.backend.dao.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ucb.accounting.backend.dao.BusinessEntity

@Repository
interface BusinessEntityRepository: JpaRepository<BusinessEntity, Long> {
    fun findByBusinessEntityIdAndStatusIsTrue (businessEntityId: Long): BusinessEntity?

    fun findAllByStatusIsTrue(): List<BusinessEntity>
}