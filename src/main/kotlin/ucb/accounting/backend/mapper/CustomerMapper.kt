package ucb.accounting.backend.mapper

import ucb.accounting.backend.dao.Customer
import ucb.accounting.backend.dto.CustomerDto

class CustomerMapper {

    companion object{
        fun entityToDto(customer: Customer): CustomerDto {
            return CustomerDto(
                customerId = customer.customerId,
                subaccountId = customer.subaccountId.toLong(),
                prefix = customer.prefix,
                displayName = customer.displayName,
                firstName = customer.firstName,
                lastName = customer.lastName,
                companyName = customer.companyName,
                companyEmail = customer.companyEmail,
                companyPhoneNumber = customer.companyPhoneNumber,
                companyAddress = customer.companyAddress
            )
        }
    }
}