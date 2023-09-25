package ucb.accounting.backend.dao.repository

import org.springframework.data.jpa.repository.JpaRepository
import ucb.accounting.backend.dao.Customer

interface CustomerRepository: JpaRepository<Customer, Long> {
    fun findByCustomerIdAndStatusIsTrue (customerId: Long): Customer?

    fun findAllByCompanyIdAndStatusIsTrue (companyId: Int): List<Customer>
}