package ucb.accounting.backend.dao.repository

import org.springframework.data.jpa.repository.JpaRepository
import ucb.accounting.backend.dao.Customer

interface CustomerRepository: JpaRepository<Customer, Long> {
    fun findByCustomerIdAndStatusTrue (customerId: Long): Customer?

    fun findAllByCompanyIdAndStatusTrue (companyId: Long): List<Customer>
}