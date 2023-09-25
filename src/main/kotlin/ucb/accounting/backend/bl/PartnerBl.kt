package ucb.accounting.backend.bl

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ucb.accounting.backend.dao.repository.*
import ucb.accounting.backend.dto.PartnerDto
import ucb.accounting.backend.exception.UasException
import ucb.accounting.backend.mapper.CustomerPartialMapper
import ucb.accounting.backend.mapper.SupplierPartialMapper
import ucb.accounting.backend.util.KeycloakSecurityContextHolder

@Service
class PartnerBl  @Autowired constructor(
    private val customerRepository: CustomerRepository,
    private val supplierRepository: SupplierRepository,
    private val companyRepository: CompanyRepository,
    private val kcUserCompanyRepository: KcUserCompanyRepository
) {

    companion object {
        private val logger = LoggerFactory.getLogger(PartnerBl::class.java.name)
    }

    fun getPartners(companyId: Long): PartnerDto {
        logger.info("Starting the API call to get partners")
        logger.info("GET /api/v1/partners/companies/${companyId}")
        // Validation of company
        companyRepository.findByCompanyIdAndStatusIsTrue(companyId) ?: throw UasException("404-05")
        // Validation of user belongs to company
        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId)
            ?: throw UasException("403-36")
        logger.info("User $kcUuid is getting partners from company $companyId")
        // Get partners
        val customers = customerRepository.findAllByCompanyIdAndStatusIsTrue(companyId.toInt())
        logger.info("${customers.size} customers found")
        val suppliers = supplierRepository.findAllByCompanyIdAndStatusIsTrue(companyId.toInt())
        logger.info("${suppliers.size} suppliers found")

        return PartnerDto(
            customers = customers.map { CustomerPartialMapper.entityToDto(it) },
            suppliers = suppliers.map { SupplierPartialMapper.entityToDto(it) }
        )
    }
}