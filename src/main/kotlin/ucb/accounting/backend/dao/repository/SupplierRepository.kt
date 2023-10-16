package ucb.accounting.backend.dao.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.PagingAndSortingRepository
import ucb.accounting.backend.dao.Supplier

interface SupplierRepository: PagingAndSortingRepository<Supplier, Long> {
    fun findBySupplierIdAndStatusIsTrue (supplierId: Long): Supplier?

    fun findAllByCompanyIdAndStatusIsTrue (companyId: Int, pageable: Pageable): Page<Supplier>

    fun findAllByCompanyIdAndStatusIsTrue (companyId: Int): List<Supplier>
}