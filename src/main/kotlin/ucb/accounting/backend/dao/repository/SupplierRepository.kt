package ucb.accounting.backend.dao.repository

import org.springframework.data.jpa.repository.JpaRepository
import ucb.accounting.backend.dao.Customer
import ucb.accounting.backend.dao.Supplier

interface SupplierRepository: JpaRepository<Supplier, Long> {
    fun findBySupplierIdAndStatusTrue (supplierId: Long): Supplier?

    fun findAllByCompanyIdAndStatusTrue (companyId: Int): List<Supplier>
}