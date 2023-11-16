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
import java.math.BigDecimal

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
        logger.info("User $kcUuid is creating a new subaccount associated with tax type from company $companyId")

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
        subaccountTaxTypeEntity.companyId = companyId.toInt()
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
        logger.info("User $kcUuid is getting all subaccount associated with tax type from company $companyId")

        val subaccountTaxTypes = subaccountTaxTypeRepository.findAllByCompanyIdAndStatusIsTrueOrderByTaxTypeIdAsc(companyId.toInt())
        // Remove some of the tax types
        logger.info("Found ${subaccountTaxTypes.size} subaccount associated with tax type")

        logger.info("Finishing the BL call to get all subaccount associated with tax type")
        return subaccountTaxTypes.map { SubaccountTaxTypePartialMapper.entityToDto(it) }
    }

    fun updateSubaccountTaxTypeRate(companyId: Long, subaccountTaxTypeDto: SubaccountTaxTypeDto){
        logger.info("Starting the BL call to update subaccount associated with tax type")
        // Validate that all fields are not null
        if (subaccountTaxTypeDto.taxTypeId == null || subaccountTaxTypeDto.subaccountId == null || subaccountTaxTypeDto.taxRate == null) throw UasException("400-29")

        // Validation that company exists
        companyRepository.findByCompanyIdAndStatusIsTrue(companyId) ?: throw UasException("404-05")

        // Validation of user belongs to company
        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId) ?: throw UasException("403-48")
        logger.info("User $kcUuid is updating subaccount associated with tax type from company $companyId")

        // Validation that subaccount exists
        val subaccountEntity = subaccountRepository.findBySubaccountIdAndStatusIsTrue(subaccountTaxTypeDto.subaccountId) ?: throw UasException("404-10")

        // Validation that tax type exists
        taxTypeRepository.findByTaxTypeIdAndStatusIsTrue(subaccountTaxTypeDto.taxTypeId) ?: throw UasException("404-16")

        // Validation that subaccount is associated with tax type
        val subaccountTaxTypeEntity = subaccountTaxTypeRepository.findBySubaccount_SubaccountIdAndTaxType_TaxTypeIdAndStatusIsTrue(subaccountTaxTypeDto.subaccountId, subaccountTaxTypeDto.taxTypeId) ?: throw UasException("404-20")

        // Validation that subaccount belongs to company
        if (subaccountEntity.companyId != companyId.toInt()) throw UasException("403-48")

        // Validation that taxRate is less than 100
        if (subaccountTaxTypeDto.taxRate > BigDecimal(100)) throw UasException("400-30")

        logger.info("User $kcUuid is updating subaccount associated with tax type")
        if (!(subaccountTaxTypeEntity.taxType!!.taxTypeName == "Impuesto a las Transacciones" || subaccountTaxTypeEntity.taxType!!.taxTypeName == "Impuesto I.T. por Pagar")){
            subaccountTaxTypeEntity.taxRate = subaccountTaxTypeDto.taxRate
            subaccountTaxTypeRepository.save(subaccountTaxTypeEntity)
        } else {
            logger.error("User $kcUuid is trying to update a tax type that is not allowed")
            throw UasException("400-30")
        }

        logger.info("Saving subaccount associated with tax type")
        logger.info("Subaccount associated with tax type saved")
    }

    fun getSaleTaxType(
        companyId: Long
    ): List<TaxTypeDto>{
        logger.info("Starting the BL call to get sales tax")

        // Validation that company exists
        companyRepository.findByCompanyIdAndStatusIsTrue(companyId) ?: throw UasException("404-05")

        // Validation of user belongs to company
        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId) ?: throw UasException("403-39")
        logger.info("User $kcUuid is getting all sales tax from company $companyId")

        val subaccountTaxTypeEntities = subaccountTaxTypeRepository.findAllByCompanyIdAndStatusIsTrueOrderByTaxTypeIdAsc(companyId.toInt())
        val ivaTaxRate = subaccountTaxTypeEntities.first { it.taxType!!.taxTypeName == "I.V.A. - Debito Fiscal" }
        val itTaxRate = subaccountTaxTypeEntities.first { it.taxType!!.taxTypeName == "Impuesto a las Transacciones" }
        val taxType = TaxTypeDto(
            taxTypeId = 1,
            taxTypeName = ivaTaxRate.taxType!!.description,
            description = "${ivaTaxRate.taxType!!.taxTypeName} - ${ivaTaxRate.taxRate}%, ${itTaxRate.taxType!!.taxTypeName} - ${itTaxRate.taxRate}%"
        )
        logger.info("Found sales tax")

        logger.info("Finishing the BL call to get sales tax")
        return listOf(taxType)
    }

    fun getExpenseTaxType(
        companyId: Long
    ): List<TaxTypeDto>{
        logger.info("Starting the BL call to get expense tax")

        // Validation that company exists
        companyRepository.findByCompanyIdAndStatusIsTrue(companyId) ?: throw UasException("404-05")

        // Validation of user belongs to company
        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId) ?: throw UasException("403-39")
        logger.info("User $kcUuid is getting all expense tax from company $companyId")

        val subaccountTaxTypeEntities = subaccountTaxTypeRepository.findAllByCompanyIdAndStatusIsTrueOrderByTaxTypeIdAsc(companyId.toInt())
        // Credito Fiscal IVA
        val ivaTaxRate = subaccountTaxTypeEntities.first { it.taxType!!.taxTypeName == "I.V.A. - Credito Fiscal" }
        // Retencion por alquileres
        val rentalRcIvaRetentionTaxRate = subaccountTaxTypeEntities.first { it.taxType!!.taxTypeName == "R.C. - I.V.A. Regimen Complementario IVA" }
        val rentalItRetentionTaxRate = subaccountTaxTypeEntities.first { it.taxType!!.taxTypeName == "I.T. - Transacciones por Pagar Retenciones" }
        // Retencion por compras
        val purchaseIueRetentionTaxRate = subaccountTaxTypeEntities.first { it.taxType!!.taxTypeName == "I.U.E. Retenciones - Compras" }
        val purchaseItRetentionTaxRate = subaccountTaxTypeEntities.first { it.taxType!!.taxTypeName == "I.T. Retenciones - Compras" }
        // Retencion por servicios
        val serviceIueRetentionTaxRate = subaccountTaxTypeEntities.first { it.taxType!!.taxTypeName == "I.U.E. Retenciones - Servicios" }
        val serviceItRetentionTaxRate = subaccountTaxTypeEntities.first { it.taxType!!.taxTypeName == "I.T. Retenciones - Servicios" }
        // Retencion RC-IVA
        val rcIvaRetentionTaxRate = subaccountTaxTypeEntities.first { it.taxType!!.taxTypeName == "R.C. - I.V.A. Retenciones" }
        // Beneficiarios del exterior
        val exteriorIueRetentionTaxRate = subaccountTaxTypeEntities.first { it.taxType!!.taxTypeName == "I.U.E. Beneficiario del Exterior" }

        val ivaTaxType = TaxTypeDto(
            taxTypeId = 1,
            taxTypeName = ivaTaxRate.taxType!!.description,
            description = "${ivaTaxRate.taxType!!.taxTypeName} - ${ivaTaxRate.taxRate}%"
        )
        val rentalDetainedTaxType = TaxTypeDto(
            taxTypeId = 2,
            taxTypeName = "RETENIDO - ${rentalRcIvaRetentionTaxRate.taxType!!.description}",
            description = "${rentalRcIvaRetentionTaxRate.taxType!!.taxTypeName} - ${rentalRcIvaRetentionTaxRate.taxRate}%, ${rentalItRetentionTaxRate.taxType!!.taxTypeName} - ${rentalItRetentionTaxRate.taxRate}%"
        )
        val rentalTakenTaxType = TaxTypeDto(
            taxTypeId = 3,
            taxTypeName = "ASUMIDO - ${rentalRcIvaRetentionTaxRate.taxType!!.description}",
            description = "${rentalRcIvaRetentionTaxRate.taxType!!.taxTypeName} - ${rentalRcIvaRetentionTaxRate.taxRate}%, ${rentalItRetentionTaxRate.taxType!!.taxTypeName} - ${rentalItRetentionTaxRate.taxRate}%"
        )
        val purchaseDetainedTaxType = TaxTypeDto(
            taxTypeId = 4,
            taxTypeName = "RETENIDO - ${purchaseIueRetentionTaxRate.taxType!!.description}",
            description = "${purchaseIueRetentionTaxRate.taxType!!.taxTypeName} - ${purchaseIueRetentionTaxRate.taxRate}%, ${purchaseItRetentionTaxRate.taxType!!.taxTypeName} - ${purchaseItRetentionTaxRate.taxRate}%"
        )
        val purchaseTakenTaxType = TaxTypeDto(
            taxTypeId = 5,
            taxTypeName = "ASUMIDO - ${purchaseIueRetentionTaxRate.taxType!!.description}",
            description = "${purchaseIueRetentionTaxRate.taxType!!.taxTypeName} - ${purchaseIueRetentionTaxRate.taxRate}%, ${purchaseItRetentionTaxRate.taxType!!.taxTypeName} - ${purchaseItRetentionTaxRate.taxRate}%"
        )
        val serviceDetainedTaxType = TaxTypeDto(
            taxTypeId = 6,
            taxTypeName = "RETENIDO - ${serviceIueRetentionTaxRate.taxType!!.description}",
            description = "${serviceIueRetentionTaxRate.taxType!!.taxTypeName} - ${serviceIueRetentionTaxRate.taxRate}%, ${serviceItRetentionTaxRate.taxType!!.taxTypeName} - ${serviceItRetentionTaxRate.taxRate}%"
        )
        val serviceTakenTaxType = TaxTypeDto(
            taxTypeId = 7,
            taxTypeName = "ASUMIDO - ${serviceIueRetentionTaxRate.taxType!!.description}",
            description = "${serviceIueRetentionTaxRate.taxType!!.taxTypeName} - ${serviceIueRetentionTaxRate.taxRate}%, ${serviceItRetentionTaxRate.taxType!!.taxTypeName} - ${serviceItRetentionTaxRate.taxRate}%"
        )
        val rcIvaDetainedTaxType = TaxTypeDto(
            taxTypeId = 8,
            taxTypeName = "RETENIDO - ${rcIvaRetentionTaxRate.taxType!!.description}",
            description = "${rcIvaRetentionTaxRate.taxType!!.taxTypeName} - ${rcIvaRetentionTaxRate.taxRate}%"
        )
        val rcIvaTakenTaxType = TaxTypeDto(
            taxTypeId = 9,
            taxTypeName = "ASUMIDO - ${rcIvaRetentionTaxRate.taxType!!.description}",
            description = "${rcIvaRetentionTaxRate.taxType!!.taxTypeName} - ${rcIvaRetentionTaxRate.taxRate}%"
        )
        val exteriorDetainedTaxType = TaxTypeDto(
            taxTypeId = 10,
            taxTypeName = "RETENIDO - ${exteriorIueRetentionTaxRate.taxType!!.description}",
            description = "${exteriorIueRetentionTaxRate.taxType!!.taxTypeName} - ${exteriorIueRetentionTaxRate.taxRate}%"
        )
        val exteriorTakenTaxType = TaxTypeDto(
            taxTypeId = 11,
            taxTypeName = "ASUMIDO - ${exteriorIueRetentionTaxRate.taxType!!.description}",
            description = "${exteriorIueRetentionTaxRate.taxType!!.taxTypeName} - ${exteriorIueRetentionTaxRate.taxRate}%"
        )

        logger.info("Found sales tax")
        logger.info("Finishing the BL call to get sales tax")
        return listOf(ivaTaxType, rentalDetainedTaxType, rentalTakenTaxType, purchaseDetainedTaxType, purchaseTakenTaxType, serviceDetainedTaxType, serviceTakenTaxType, rcIvaDetainedTaxType, rcIvaTakenTaxType, exteriorDetainedTaxType, exteriorTakenTaxType)
    }
}
