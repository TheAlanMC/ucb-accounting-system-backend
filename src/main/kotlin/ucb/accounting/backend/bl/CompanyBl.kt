package ucb.accounting.backend.bl

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.slf4j.LoggerFactory
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service
import ucb.accounting.backend.dao.*
import ucb.accounting.backend.dao.repository.*
import ucb.accounting.backend.dto.CompanyDto
import ucb.accounting.backend.dto.CompanyPartialDto
import ucb.accounting.backend.exception.UasException
import ucb.accounting.backend.mapper.CompanyMapper
import ucb.accounting.backend.service.MinioService
import ucb.accounting.backend.util.KeycloakSecurityContextHolder
import java.math.BigDecimal

@Service
class CompanyBl @Autowired constructor(
    private val accountGroupRepository: AccountGroupRepository,
    private val accountRepository: AccountRepository,
    private val accountSubgroupRepository: AccountSubgroupRepository,
    private val businessEntityRepository: BusinessEntityRepository,
    private val companyRepository: CompanyRepository,
    private val industryRepository: IndustryRepository,
    private val kcGroupRepository: KcGroupRepository,
    private val kcUserCompanyRepository: KcUserCompanyRepository,
    private val kcUserRepository: KcUserRepository,
    private val minioService: MinioService,
    private val s3ObjectRepository: S3ObjectRepository,
    private val subaccountRepository: SubaccountRepository,
    private val taxTypeRepository: TaxTypeRepository,
    private val subaccountTaxTypeRepository: SubaccountTaxTypeRepository
) {
    companion object {
        private val logger = LoggerFactory.getLogger(CompanyBl::class.java.name)
    }

    fun getCompanyInfo(companyId: Long): CompanyDto {
        logger.info("Starting the BL call to get company info")
        logger.info("BL call to get company info")
        // Validate that the company exists
        val company = companyRepository.findByCompanyIdAndStatusIsTrue(companyId) ?: throw UasException("404-05")

        // Validation of user belongs to company
        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        logger.info("User $kcUuid is getting company info")
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId) ?: throw UasException("403-03")

        // Get s3 object for company logo
        val s3Object: S3Object = s3ObjectRepository.findByS3ObjectIdAndStatusIsTrue(company.s3CompanyLogo.toLong())!!
        val preSignedUrl: String = minioService.getPreSignedUrl(s3Object.bucket, s3Object.filename)
        logger.info("Company info retrieved successfully")
        return CompanyMapper.entityToDto(company, preSignedUrl)
    }

    fun createCompany (companyPartialDto: CompanyPartialDto){
        // TODO: USER CAN CREATE MULTIPLE COMPANIES
        logger.info("Starting the BL call to post company info")
        // Validate that not null fields are not null
        if (companyPartialDto.industryId == null || companyPartialDto.businessEntityId == null || companyPartialDto.companyName == null || companyPartialDto.companyNit == null || companyPartialDto.companyAddress == null || companyPartialDto.phoneNumber == null ) throw UasException("400-05")

        // Validate that the industry and business entity exist
        industryRepository.findByIndustryIdAndStatusIsTrue(companyPartialDto.industryId) ?: throw UasException("404-03")
        businessEntityRepository.findByBusinessEntityIdAndStatusIsTrue(companyPartialDto.businessEntityId) ?: throw UasException("404-04")

        // Validate that the user exists
        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        val kcUserEntity = kcUserRepository.findByKcUuidAndStatusIsTrue(kcUuid) ?: throw UasException("404-02")

        // Storing company
        logger.info("Saving company")
        val companyEntity = Company()
        companyEntity.industryId = companyPartialDto.industryId.toInt()
        companyEntity.businessEntityId = companyPartialDto.businessEntityId.toInt()
        companyEntity.companyName = companyPartialDto.companyName
        companyEntity.companyNit = companyPartialDto.companyNit
        companyEntity.companyAddress = companyPartialDto.companyAddress
        companyEntity.phoneNumber = companyPartialDto.phoneNumber
        companyEntity.s3CompanyLogo = 1 //default logo
        val savedCompanyEntity = companyRepository.save(companyEntity)
        logger.info("Company saved successfully")

        // Storing user-company relation
        logger.info("Saving user-company relation")
        val kcUserCompanyEntity = KcUserCompany()
        kcUserCompanyEntity.kcUser = kcUserEntity
        kcUserCompanyEntity.company = savedCompanyEntity
        kcUserCompanyEntity.kcGroupId = kcGroupRepository.findByGroupNameAndStatusIsTrue("Contador")!!.kcGroupId
        kcUserCompanyRepository.save(kcUserCompanyEntity)
        logger.info("User-company relation saved successfully")

        // Create accounting plan
        createAccountingPlan(savedCompanyEntity.companyId)

        // Create subaccount-tax_type relationships
        createSubaccountTaxTypeRelationships(savedCompanyEntity.companyId)
    }

    fun updateCompany (companyPartialDto: CompanyPartialDto, companyId: Long): CompanyDto {
        logger.info("Starting the BL call to put company info")
        // Validate that at least one field is not null
        if (companyPartialDto.industryId == null && companyPartialDto.businessEntityId == null && companyPartialDto.companyName == null && companyPartialDto.companyNit == null && companyPartialDto.companyAddress == null && companyPartialDto.phoneNumber == null && companyPartialDto.s3CompanyLogoId == null) throw UasException("400-06")

        // Validate that the industry and business entity exist
        if (companyPartialDto.industryId != null) industryRepository.findByIndustryIdAndStatusIsTrue(companyPartialDto.industryId) ?: throw UasException("404-03")
        if (companyPartialDto.businessEntityId != null) businessEntityRepository.findByBusinessEntityIdAndStatusIsTrue(companyPartialDto.businessEntityId) ?: throw UasException("404-04")

        // Validate that the company exists
        val companyEntity = companyRepository.findByCompanyIdAndStatusIsTrue(companyId) ?: throw UasException("404-05")

        // Validate that the user belongs to the company
        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId) ?: throw UasException("403-04")
        logger.info("User $kcUuid is updating a company")

        // If s3CompanyLogoId is not null, update s3CompanyLogo in Company
        if (companyPartialDto.s3CompanyLogoId != null) {
            // Validation that the s3CompanyLogoId exists
            s3ObjectRepository.findByS3ObjectIdAndStatusIsTrue(companyPartialDto.s3CompanyLogoId) ?: throw UasException("404-13")
            companyEntity.s3CompanyLogo = companyPartialDto.s3CompanyLogoId.toInt()
        }

        // Update company
        logger.info("Updating company")
        companyEntity.industryId = (companyPartialDto.industryId ?: companyEntity.industryId).toInt()
        companyEntity.businessEntityId = (companyPartialDto.businessEntityId ?: companyEntity.businessEntityId).toInt()
        companyEntity.companyName = companyPartialDto.companyName ?: companyEntity.companyName
        companyEntity.companyNit = companyPartialDto.companyNit ?: companyEntity.companyNit
        companyEntity.companyAddress = companyPartialDto.companyAddress ?: companyEntity.companyAddress
        companyEntity.phoneNumber = companyPartialDto.phoneNumber ?: companyEntity.phoneNumber
        val updatedCompany = companyRepository.save(companyEntity)

        val s3Object: S3Object = s3ObjectRepository.findByS3ObjectIdAndStatusIsTrue(companyEntity.s3CompanyLogo.toLong())!!
        val preSignedUrl: String = minioService.getPreSignedUrl(s3Object.bucket, s3Object.filename)

        logger.info("Company updated successfully")
        return CompanyMapper.entityToDto(updatedCompany, preSignedUrl)
    }

    fun createAccountingPlan(companyId: Long){

        logger.info("Starting the BL call to create accounting plan for a new company")

        val resource = ClassPathResource("default_chart_of_accounts.json")
        val objectMapper = ObjectMapper()
        val jsonNode = objectMapper.readTree(resource.inputStream)

        val accountGroups = jsonNode.get("account_groups")
        for (group in accountGroups){
            logger.info("Creating account group ${group.get("account_group_name")} for company $companyId")
            val accountGroup = AccountGroup()
            accountGroup.companyId = companyId.toInt()
            accountGroup.accountCategoryId = group.get("account_category_id").toString().toInt()
            accountGroup.accountGroupCode = group.get("account_group_code").toString().toInt()
            accountGroup.accountGroupName = group.get("account_group_name").toString().replace("\"", "")
            val savedAccountGroup = accountGroupRepository.save(accountGroup)
            val accountSubGroups = group.get("accounts_subgroups")
            for (subgroup in accountSubGroups){
                logger.info("Creating account subgroup ${subgroup.get("account_subgroup_name")} for company $companyId")
                val accountSubGroup = AccountSubgroup()
                accountSubGroup.accountGroupId = savedAccountGroup.accountGroupId.toInt()
                accountSubGroup.companyId = companyId.toInt()
                accountSubGroup.accountSubgroupCode = subgroup.get("account_subgroup_code").toString().toInt()
                accountSubGroup.accountSubgroupName = subgroup.get("account_subgroup_name").toString().replace("\"", "")
                val savedAccountSubGroup = accountSubgroupRepository.save(accountSubGroup)
                val accounts = subgroup.get("accounts")
                for(account in accounts){
                    logger.info("Creating account ${account.get("account_name")} for company $companyId")
                    val accountEntity = Account()
                    accountEntity.accountSubgroupId = savedAccountSubGroup.accountSubgroupId.toInt()
                    accountEntity.companyId = companyId.toInt()
                    accountEntity.accountCode = account.get("account_code").toString().toInt()
                    accountEntity.accountName = account.get("account_name").toString().replace("\"", "")
                    val savedAccount = accountRepository.save(accountEntity)
                    val subAccounts = account.get("subaccounts")
                    for (subAccount in subAccounts){
                        logger.info("Creating subaccount ${subAccount.get("subaccount_name")} for company $companyId")
                        val subAccountEntity = Subaccount()
                        subAccountEntity.accountId = savedAccount.accountId.toInt()
                        subAccountEntity.companyId = companyId.toInt()
                        subAccountEntity.subaccountCode = subAccount.get("subaccount_code").toString().toInt()
                        subAccountEntity.subaccountName = subAccount.get("subaccount_name").toString().replace("\"", "")
                        subaccountRepository.save(subAccountEntity)
                    }
                }
            }
        }
    }

    fun createSubaccountTaxTypeRelationships(companyId: Long) {
        // Obtener todos los subaccounts para la empresa con companyId
        val subaccounts = subaccountRepository.findByCompanyId(companyId)

        // Obtener todos los tax_types
        val taxTypes = taxTypeRepository.findAll()

        // Crear un mapa de tax_type_name a taxTypeId para facilitar la búsqueda
        val taxTypeMap = taxTypes.associateBy { it.taxTypeName }

        // Definir el valor por defecto para tax_rate (13%)
        val defaultTaxRate = BigDecimal("13")

        // Valor por defecto para status
        val defaultStatus = true

        // Recorrer los subaccounts
        for (subaccount in subaccounts) {
            val subaccountName = subaccount.subaccountName

            // Buscar el tax_type correspondiente en el mapa
            val taxType = taxTypeMap[subaccountName]

            // Si se encuentra un tax_type correspondiente, crear la relación en subaccount_tax_type
            if (taxType != null) {
                // Verificar si los nombres son iguales
                if (subaccountName == taxType.taxTypeName) {
                    val subaccountTaxType = SubaccountTaxType()
                    subaccountTaxType.companyId = companyId.toInt()

                    // Obtener los IDs correspondientes
                    val subaccountId = subaccount.subaccountId
                    val taxTypeId = taxType.taxTypeId

                    // Establecer los valores en la entidad SubaccountTaxType
                    subaccountTaxType.subaccountId = subaccountId.toInt()
                    subaccountTaxType.taxTypeId = taxTypeId.toInt()
                    subaccountTaxType.companyId = companyId.toInt()
                    subaccountTaxType.taxRate = defaultTaxRate
                    subaccountTaxType.status = defaultStatus

                    // Guardar la relación en subaccount_tax_type
                    subaccountTaxTypeRepository.save(subaccountTaxType)
                }
            }
        }
    }

}