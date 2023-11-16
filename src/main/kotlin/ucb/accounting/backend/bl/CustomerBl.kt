package ucb.accounting.backend.bl

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import ucb.accounting.backend.dao.Customer
import ucb.accounting.backend.dao.Subaccount
import ucb.accounting.backend.dao.repository.*
import ucb.accounting.backend.dao.specification.CustomerSpecification
import ucb.accounting.backend.dto.CustomerDto
import ucb.accounting.backend.dto.CustomerPartialDto
import ucb.accounting.backend.exception.UasException
import ucb.accounting.backend.mapper.CustomerMapper
import ucb.accounting.backend.mapper.CustomerPartialMapper
import ucb.accounting.backend.util.KeycloakSecurityContextHolder

@Service
class CustomerBl @Autowired constructor(
    private val accountRepository: AccountRepository,
    private val companyRepository: CompanyRepository,
    private val customerRepository: CustomerRepository,
    private val kcUserCompanyRepository: KcUserCompanyRepository,
    private val subaccountRepository: SubaccountRepository,
) {

    companion object{
        private val logger = LoggerFactory.getLogger(CustomerBl::class.java.name)
    }
    fun createCustomer(companyId: Long, customerDto: CustomerDto) {
        logger.info("Starting the BL call to create customer")
        // Validation that all fields are sent
        if (customerDto.prefix == null || customerDto.firstName == null || customerDto.lastName == null ||
            customerDto.displayName == null || customerDto.companyName == null || customerDto.companyAddress == null || customerDto.companyPhoneNumber == null ||
            customerDto.companyEmail== null) throw UasException("400-23")

        // Validation of company
        companyRepository.findByCompanyIdAndStatusIsTrue(companyId) ?: throw UasException("404-05")

        // Validation of user belongs to company
        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId) ?: throw UasException("403-27")
        logger.info("User $kcUuid is creating a new customer")

        // Get account for "CUENTAS POR COBRAR CLIENTES M/N" account
        val accountEntity = accountRepository.findByAccountNameAndCompanyIdAndStatusIsTrue("CUENTAS POR COBRAR CLIENTES M/N", companyId.toInt()) ?: throw UasException("404-09")

        // Get subaccount code, which is the last subaccount code + 1
        val subaccountCode =
            subaccountRepository.findFirstByAccountIdAndCompanyIdAndStatusIsTrueOrderBySubaccountCodeDesc(
                accountEntity.accountId.toInt(),
                companyId.toInt()
            )?.subaccountCode ?: (0 + 1)

        // Creat a subaccount for the customer
        logger.info("Creating subaccount for customer")
        val subaccountEntity = Subaccount()
        subaccountEntity.accountId = accountEntity.accountId.toInt()
        subaccountEntity.companyId = companyId.toInt()
        subaccountEntity.subaccountName = customerDto.displayName
        subaccountEntity.subaccountCode = subaccountCode
        val savedSubaccountEntity = subaccountRepository.save(subaccountEntity)

        logger.info("User $kcUuid is creating a new customer")
        val customerEntity = Customer()
        customerEntity.companyId = companyId.toInt()
        customerEntity.subaccountId = savedSubaccountEntity.subaccountId.toInt()
        customerEntity.prefix = customerDto.prefix
        customerEntity.displayName = customerDto.displayName
        customerEntity.firstName = customerDto.firstName
        customerEntity.lastName = customerDto.lastName
        customerEntity.companyName = customerDto.companyName
        customerEntity.companyEmail = customerDto.companyEmail
        customerEntity.companyPhoneNumber = customerDto.companyPhoneNumber
        customerEntity.companyAddress = customerDto.companyAddress

        logger.info("Saving customer")
        customerRepository.save(customerEntity)
        logger.info("Customer saved")
    }

    fun getCustomers(
        companyId: Long,
        sortBy: String,
        sortType: String,
        page: Int,
        size: Int,
        keyword: String?,
    ): Page<CustomerPartialDto> {
        logger.info("Starting the BL call to get customers")
        // Validation of company
        companyRepository.findByCompanyIdAndStatusIsTrue(companyId) ?: throw UasException("404-05")

        // Validation of user belongs to company
        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId) ?: throw UasException("403-28")
        logger.info("User $kcUuid is getting customers from company $companyId")

        val pageable: Pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortType), sortBy))
        var specification: Specification<Customer> = Specification.where(null)
        specification = specification.and(specification.and(CustomerSpecification.companyId(companyId.toInt())))
        specification = specification.and(specification.and(CustomerSpecification.statusIsTrue()))

        if (!keyword.isNullOrEmpty() && keyword.isNotBlank()) {
            specification = specification.and(specification.and(CustomerSpecification.customerKeyword(keyword)))
        }
        // Get customers
        val customers = customerRepository.findAll (specification, pageable)
        logger.info("${customers.size} customers found")
        return customers.map { CustomerPartialMapper.entityToDto(it) }
    }

    fun getCustomer(customerId:Long, companyId: Long): CustomerDto{
        logger.info("Starting the BL call to get customer")
        // Validation of company
        companyRepository.findByCompanyIdAndStatusIsTrue(companyId) ?: throw UasException("404-05")

        // Validation of customer
        val customerEntity = customerRepository.findByCustomerIdAndStatusIsTrue(customerId) ?: throw UasException("404-14")

        // Validation of user belongs to company
        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId) ?: throw UasException("403-28")
        logger.info("User $kcUuid is getting customer $customerId from company $companyId")

        // Validation that customer belongs to company
        if (customerEntity.companyId != companyId.toInt()) throw UasException("403-28")

        logger.info("Customer found")
        return CustomerMapper.entityToDto(customerEntity)
    }

    fun updateCustomer(customerId: Long, companyId:Long, customerDto: CustomerDto): CustomerDto{
        logger.info("Starting the BL call to update customer")
        // Validation that at least one field is sent to update
        if (customerDto.prefix == null && customerDto.firstName == null && customerDto.lastName == null &&
            customerDto.displayName == null && customerDto.companyName == null && customerDto.companyAddress == null && customerDto.companyPhoneNumber == null &&
            customerDto.companyEmail== null) throw UasException("400-24")

        // Validation of company
        companyRepository.findByCompanyIdAndStatusIsTrue(companyId) ?: throw UasException("404-05")

        // Validation of customer
        val customerEntity = customerRepository.findByCustomerIdAndStatusIsTrue(customerId) ?: throw UasException("404-14")

        // Validation of user belongs to company
        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId) ?: throw UasException("403-29")
        logger.info("User $kcUuid is updating customer $customerId from company $companyId")

        // Validation that customer belongs to company
        if (customerEntity.companyId != companyId.toInt()) throw UasException("403-29")

        // Validation that if the displayName is updated, the subaccount name is updated too
        if (customerDto.displayName != null) {
            val subaccountEntity = subaccountRepository.findBySubaccountIdAndStatusIsTrue(customerEntity.subaccountId.toLong()) ?: throw UasException("404-10")
            subaccountEntity.subaccountName = customerDto.displayName
            subaccountRepository.save(subaccountEntity)
        }

        logger.info("User $kcUuid is updating customer $customerId from company $companyId")

        customerEntity.prefix = customerDto.prefix ?: customerEntity.prefix
        customerEntity.displayName = customerDto.displayName ?: customerEntity.displayName
        customerEntity.firstName = customerDto.firstName ?: customerEntity.firstName
        customerEntity.lastName = customerDto.lastName ?: customerEntity.lastName
        customerEntity.companyName = customerDto.companyName ?: customerEntity.companyName
        customerEntity.companyEmail = customerDto.companyEmail ?: customerEntity.companyEmail
        customerEntity.companyPhoneNumber = customerDto.companyPhoneNumber ?: customerEntity.companyPhoneNumber
        customerEntity.companyAddress = customerDto.companyAddress ?: customerEntity.companyAddress

        logger.info("Saving customer")
        customerRepository.save(customerEntity)
        logger.info("Customer saved")
        return CustomerMapper.entityToDto(customerEntity)
    }
}