package ucb.accounting.backend.dao.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.PagingAndSortingRepository
import ucb.accounting.backend.dao.Customer

interface CustomerRepository: PagingAndSortingRepository<Customer, Long> {
    fun findByCustomerIdAndStatusIsTrue (customerId: Long): Customer?

    fun findAllByCompanyIdAndStatusIsTrue (companyId: Int, pageable: Pageable): Page<Customer>

    fun findAllByCompanyIdAndStatusIsTrue (companyId: Int): List<Customer>
}