package ucb.accounting.backend.mapper

import ucb.accounting.backend.dao.Customer
import ucb.accounting.backend.dto.CustomerDto
import ucb.accounting.backend.dto.CustomerPartialDto
import java.sql.Date

class CustomerPartialMapper {

    companion object{
        fun entityToDto(customer: Customer): CustomerPartialDto {
            return CustomerPartialDto(
                customerId = customer.customerId,
                displayName = customer.displayName,
                companyName = customer.companyName,
                companyPhoneNumber = customer.companyPhoneNumber,
                creationDate = Date(customer.txDate.time)
            )
        }
    }
}