package ucb.accounting.backend.bl

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ucb.accounting.backend.dao.SubaccountTaxType
import ucb.accounting.backend.dao.repository.*
import ucb.accounting.backend.dto.SubaccountTaxTypeDto
import ucb.accounting.backend.dto.SubaccountTaxTypePartialDto
import ucb.accounting.backend.dto.TaxTypeDto
import ucb.accounting.backend.exception.UasException
import ucb.accounting.backend.mapper.SubaccountTaxTypePartialMapper
import ucb.accounting.backend.mapper.TaxTypeMapper
import ucb.accounting.backend.util.KeycloakSecurityContextHolder

@Service
class TaxBl @Autowired constructor(
    private val companyRepository: CompanyRepository,
    private val kcUserCompanyRepository: KcUserCompanyRepository,
    private val subaccountRepository: SubaccountRepository,
    private val subaccountTaxTypeRepository: SubaccountTaxTypeRepository,
    private val taxTypeRepository: TaxTypeRepository,
) {
    companion object{
        private val logger = LoggerFactory.getLogger(TaxBl::class.java.name)
    }

    fun getTaxTypes(): List<TaxTypeDto> {
        logger.info("Starting the BL call to get tax types")
        val taxTypes = taxTypeRepository.findAllByStatusIsTrue()
        logger.info("Found ${taxTypes.size} tax types")
        logger.info("Finishing the BL call to get tax types")
        return taxTypes.map { TaxTypeMapper.entityToDto(it) }
    }

    fun createSubaccountTaxType(companyId: Long, subaccountTaxTypeDto: SubaccountTaxTypeDto){
        logger.info("Starting the BL call to create subaccount associated with tax type")
        // Validate that all fields are not null
        if (subaccountTaxTypeDto.taxTypeId == null || subaccountTaxTypeDto.subaccountId == null || subaccountTaxTypeDto.taxRate == null) throw UasException("400-29")

        // Validation that company exists
        companyRepository.findByCompanyIdAndStatusIsTrue(companyId) ?: throw UasException("404-05")

        // Validation of user belongs to company
        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId) ?: throw UasException("403-38")
        logger.info("User $kcUuid is uploading file to company $companyId")

        // Validation that subaccount exists
        val subaccountEntity = subaccountRepository.findBySubaccountIdAndStatusIsTrue(subaccountTaxTypeDto.subaccountId) ?: throw UasException("404-10")

        // Validation that tax type exists
        val taxTypeEntity = taxTypeRepository.findByTaxTypeIdAndStatusIsTrue(subaccountTaxTypeDto.taxTypeId) ?: throw UasException("404-16")

        // Validation that subaccount is not associated with tax type
        if (subaccountTaxTypeRepository.findBySubaccount_SubaccountIdAndTaxType_TaxTypeIdAndStatusIsTrue(subaccountTaxTypeDto.subaccountId, subaccountTaxTypeDto.taxTypeId) != null) throw UasException("409-07")



        // Validation that subaccount belongs to company
        if (subaccountEntity.companyId != companyId.toInt()) throw UasException("403-38")

        logger.info("User $kcUuid is creating a new subaccount associated with tax type")

        val subaccountTaxTypeEntity = SubaccountTaxType()
        subaccountTaxTypeEntity.companyId = companyId
        subaccountTaxTypeEntity.subaccount = subaccountEntity
        subaccountTaxTypeEntity.taxType = taxTypeEntity
        subaccountTaxTypeEntity.taxRate = subaccountTaxTypeDto.taxRate

        logger.info("Saving subaccount associated with tax type")
        subaccountTaxTypeRepository.save(subaccountTaxTypeEntity)
        logger.info("Subaccount associated with tax type saved")
    }

    fun getSubaccountTaxTypes(companyId: Long): List<SubaccountTaxTypePartialDto> {
        logger.info("Starting the BL call to get all subaccount associated with tax type")
        // Validation that company exists
        companyRepository.findByCompanyIdAndStatusIsTrue(companyId) ?: throw UasException("404-05")

        // Validation of user belongs to company
        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId) ?: throw UasException("403-39")
        logger.info("User $kcUuid is uploading file to company $companyId")

        val subaccountTaxTypes = subaccountTaxTypeRepository.findAllByCompanyIdAndStatusIsTrue(companyId)
        logger.info("Found ${subaccountTaxTypes.size} subaccount associated with tax type")

        logger.info("Finishing the BL call to get all subaccount associated with tax type")
        return subaccountTaxTypes.map { SubaccountTaxTypePartialMapper.entityToDto(it) }
    }
}
