package ucb.accounting.backend.bl

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ucb.accounting.backend.dao.repository.CustomerRepository
import org.slf4j.LoggerFactory
import ucb.accounting.backend.dao.Customer
import ucb.accounting.backend.dao.repository.CompanyRepository
import ucb.accounting.backend.dao.repository.KcUserCompanyRepository
import ucb.accounting.backend.dao.repository.SubAccountRepository
import ucb.accounting.backend.dto.CustomerDto
import ucb.accounting.backend.dto.CustomerPartialDto
import ucb.accounting.backend.exception.UasException
import ucb.accounting.backend.mapper.CustomerMapper
import ucb.accounting.backend.mapper.CustomerPartialMapper
import ucb.accounting.backend.util.KeycloakSecurityContextHolder
import java.sql.Date

@Service
class CustomerBl @Autowired constructor(
    private val customerRepository: CustomerRepository,
    private val companyRepository: CompanyRepository,
    private val subAccountRepository: SubAccountRepository,
    private val kcUserCompanyRepository: KcUserCompanyRepository,
) {

    companion object{
        private val logger = LoggerFactory.getLogger(CustomerBl::class.java.name)
    }
    fun createCustomer(companyId: Long, customerDto: CustomerDto) {
        logger.info("Starting the BL call to create customer")
        // Validation that all fields are sent
        if (customerDto.subaccountId == null || customerDto.prefix == null || customerDto.firstName == null || customerDto.lastName == null ||
            customerDto.displayName == null || customerDto.companyName == null || customerDto.companyAddress == null || customerDto.companyPhoneNumber == null ||
            customerDto.companyEmail== null) throw UasException("400-23")
        // Validation of company
        companyRepository.findByCompanyIdAndStatusIsTrue(companyId) ?: throw UasException("404-05")
        // Validation that subaccount exists
        val subAccountEntity = subAccountRepository.findBySubaccountIdAndStatusIsTrue(customerDto.subaccountId) ?: throw UasException("404-10")
        // Validation of user belongs to company
        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId) ?: throw UasException("403-27")
        logger.info("User $kcUuid is uploading file to company $companyId")
        // Validation that subaccount belongs to company
        if (subAccountEntity.companyId != companyId.toInt()) throw UasException("403-27")

        logger.info("User $kcUuid is creating a new customer")

        val customerEntity = Customer()
        customerEntity.companyId = companyId.toInt()
        customerEntity.subaccountId = customerDto.subaccountId.toInt()
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

    fun getCustomers(companyId: Long): List<CustomerPartialDto> {
        logger.info("Starting the BL call to get customers")
        // Validation of company
        companyRepository.findByCompanyIdAndStatusIsTrue(companyId) ?: throw UasException("404-05")
        // Validation of user belongs to company
        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId) ?: throw UasException("403-28")
        logger.info("User $kcUuid is getting customers from company $companyId")
        // Get customers
        val customers = customerRepository.findAllByCompanyIdAndStatusIsTrue(companyId.toInt())
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
        if (customerDto.subaccountId == null && customerDto.prefix == null && customerDto.firstName == null && customerDto.lastName == null &&
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
        // Validation that subaccount exists
        if (customerDto.subaccountId != null) {
            val subAccountEntity = subAccountRepository.findBySubaccountIdAndStatusIsTrue(customerDto.subaccountId) ?: throw UasException("404-10")
            // Validation that subaccount belongs to company
            if (subAccountEntity.companyId != companyId.toInt()) throw UasException("403-29")
        }

        logger.info("User $kcUuid is updating customer $customerId from company $companyId")

        customerEntity.subaccountId = (customerDto.subaccountId ?: customerEntity.subaccountId).toInt()
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