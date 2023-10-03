package ucb.accounting.backend.dao.repository

import org.springframework.data.jpa.repository.JpaRepository
import ucb.accounting.backend.dao.Supplier

interface SupplierRepository: JpaRepository<Supplier, Long> {
    fun findBySupplierIdAndStatusIsTrue (supplierId: Long): Supplier?

    fun findAllByCompanyIdAndStatusIsTrue (companyId: Int): List<Supplier>
}