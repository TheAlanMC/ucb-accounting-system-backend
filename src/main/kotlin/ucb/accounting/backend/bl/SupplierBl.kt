package ucb.accounting.backend.bl

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.slf4j.LoggerFactory
import ucb.accounting.backend.dao.Supplier
import ucb.accounting.backend.dao.repository.*
import ucb.accounting.backend.dto.SupplierDto
import ucb.accounting.backend.dto.SupplierPartialDto
import ucb.accounting.backend.exception.UasException
import ucb.accounting.backend.mapper.SupplierMapper
import ucb.accounting.backend.mapper.SupplierPartialMapper
import ucb.accounting.backend.util.KeycloakSecurityContextHolder

@Service
class SupplierBl @Autowired constructor(
    private val supplierRepository: SupplierRepository,
    private val companyRepository: CompanyRepository,
    private val subaccountRepository: SubaccountRepository,
    private val kcUserCompanyRepository: KcUserCompanyRepository,
) {

    companion object{
        private val logger = LoggerFactory.getLogger(SupplierBl::class.java.name)
    }
    fun createSupplier(companyId: Long, supplierDto: SupplierDto) {
        logger.info("Starting the BL call to create supplier")
        // Validation that all fields are sent
        if (supplierDto.subaccountId == null || supplierDto.prefix == null || supplierDto.firstName == null || supplierDto.lastName == null ||
            supplierDto.displayName == null || supplierDto.companyName == null || supplierDto.companyAddress == null || supplierDto.companyPhoneNumber == null ||
            supplierDto.companyEmail== null) throw UasException("400-26")
        // Validation of company
        companyRepository.findByCompanyIdAndStatusIsTrue(companyId) ?: throw UasException("404-05")
        // Validation that subaccount exists
        val subaccountEntity = subaccountRepository.findBySubaccountIdAndStatusIsTrue(supplierDto.subaccountId) ?: throw UasException("404-10")
        // Validation of user belongs to company
        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId) ?: throw UasException("403-30")
        logger.info("User $kcUuid is uploading file to company $companyId")
        // Validation that subaccount belongs to company
        if (subaccountEntity.companyId != companyId.toInt()) throw UasException("403-30")

        logger.info("User $kcUuid is creating a new supplier")

        val supplierEntity = Supplier()
        supplierEntity.companyId = companyId.toInt()
        supplierEntity.subaccountId = supplierDto.subaccountId.toInt()
        supplierEntity.prefix = supplierDto.prefix
        supplierEntity.displayName = supplierDto.displayName
        supplierEntity.firstName = supplierDto.firstName
        supplierEntity.lastName = supplierDto.lastName
        supplierEntity.companyName = supplierDto.companyName
        supplierEntity.companyEmail = supplierDto.companyEmail
        supplierEntity.companyPhoneNumber = supplierDto.companyPhoneNumber
        supplierEntity.companyAddress = supplierDto.companyAddress

        logger.info("Saving supplier")
        supplierRepository.save(supplierEntity)
        logger.info("Supplier saved")
    }

    fun getSuppliers(companyId: Long): List<SupplierPartialDto> {
        logger.info("Starting the BL call to get suppliers")
        // Validation of company
        companyRepository.findByCompanyIdAndStatusIsTrue(companyId) ?: throw UasException("404-05")
        // Validation of user belongs to company
        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId) ?: throw UasException("403-31")
        logger.info("User $kcUuid is getting suppliers from company $companyId")
        // Get suppliers
        val suppliers = supplierRepository.findAllByCompanyIdAndStatusIsTrue(companyId.toInt())
        logger.info("${suppliers.size} suppliers found")
        return suppliers.map { SupplierPartialMapper.entityToDto(it) }
    }

    fun getSupplier(supplierId:Long, companyId: Long): SupplierDto{
        logger.info("Starting the BL call to get supplier")
        // Validation of company
        companyRepository.findByCompanyIdAndStatusIsTrue(companyId) ?: throw UasException("404-05")
        // Validation of supplier
        val supplierEntity = supplierRepository.findBySupplierIdAndStatusIsTrue(supplierId) ?: throw UasException("404-15")
        // Validation of user belongs to company
        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId) ?: throw UasException("403-31")
        logger.info("User $kcUuid is getting supplier $supplierId from company $companyId")
        // Validation that supplier belongs to company
        if (supplierEntity.companyId != companyId.toInt()) throw UasException("403-31")
        logger.info("Supplier found")
        return SupplierMapper.entityToDto(supplierEntity)
    }

    fun updateSupplier(supplierId: Long, companyId:Long, supplierDto: SupplierDto): SupplierDto{
        logger.info("Starting the BL call to update supplier")
        // Validation that at least one field is sent to update
        if (supplierDto.subaccountId == null && supplierDto.prefix == null && supplierDto.firstName == null && supplierDto.lastName == null &&
            supplierDto.displayName == null && supplierDto.companyName == null && supplierDto.companyAddress == null && supplierDto.companyPhoneNumber == null &&
            supplierDto.companyEmail== null) throw UasException("400-27")
        // Validation of company
        companyRepository.findByCompanyIdAndStatusIsTrue(companyId) ?: throw UasException("404-05")
        // Validation of supplier
        val supplierEntity = supplierRepository.findBySupplierIdAndStatusIsTrue(supplierId) ?: throw UasException("404-15")
        // Validation of user belongs to company
        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId) ?: throw UasException("403-32")
        logger.info("User $kcUuid is updating supplier $supplierId from company $companyId")
        // Validation that supplier belongs to company
        if (supplierEntity.companyId != companyId.toInt()) throw UasException("403-32")
        // Validation that subaccount exists
        if (supplierDto.subaccountId != null) {
            val subaccountEntity = subaccountRepository.findBySubaccountIdAndStatusIsTrue(supplierDto.subaccountId) ?: throw UasException("404-10")
            // Validation that subaccount belongs to company
            if (subaccountEntity.companyId != companyId.toInt()) throw UasException("403-32")
        }

        logger.info("User $kcUuid is updating supplier $supplierId from company $companyId")

        supplierEntity.subaccountId = (supplierDto.subaccountId ?: supplierEntity.subaccountId).toInt()
        supplierEntity.prefix = supplierDto.prefix ?: supplierEntity.prefix
        supplierEntity.displayName = supplierDto.displayName ?: supplierEntity.displayName
        supplierEntity.firstName = supplierDto.firstName ?: supplierEntity.firstName
        supplierEntity.lastName = supplierDto.lastName ?: supplierEntity.lastName
        supplierEntity.companyName = supplierDto.companyName ?: supplierEntity.companyName
        supplierEntity.companyEmail = supplierDto.companyEmail ?: supplierEntity.companyEmail
        supplierEntity.companyPhoneNumber = supplierDto.companyPhoneNumber ?: supplierEntity.companyPhoneNumber
        supplierEntity.companyAddress = supplierDto.companyAddress ?: supplierEntity.companyAddress

        logger.info("Saving supplier")
        supplierRepository.save(supplierEntity)
        logger.info("Supplier saved")
        return SupplierMapper.entityToDto(supplierEntity)
    }
}