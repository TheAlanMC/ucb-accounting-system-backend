package ucb.accounting.backend.bl

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ucb.accounting.backend.dao.Subaccount
import ucb.accounting.backend.dao.repository.AccountRepository
import ucb.accounting.backend.dao.repository.CompanyRepository
import ucb.accounting.backend.dao.repository.KcUserCompanyRepository
import ucb.accounting.backend.dao.repository.SubaccountRepository
import ucb.accounting.backend.dto.SubaccountPartialDto
import ucb.accounting.backend.exception.UasException
import ucb.accounting.backend.mapper.SubaccountPartialMapper
import ucb.accounting.backend.util.KeycloakSecurityContextHolder

@Service
class SubaccountBl @Autowired constructor(
    private val accountRepository: AccountRepository,
    private val companyRepository: CompanyRepository,
    private val kcUserCompanyRepository: KcUserCompanyRepository,
    private val subaccountRepository: SubaccountRepository,
){

    companion object {
        private val logger = LoggerFactory.getLogger(SubaccountBl::class.java.name)
    }

    fun createCompanySubaccount(companyId: Long, subaccountDto: SubaccountPartialDto){
        logger.info("Starting the business logic to post a subaccount")
        logger.info("BL call to post a subaccount")
        // Validation that all the parameters were sent
        if (subaccountDto.accountId == null || subaccountDto.subaccountCode == null || subaccountDto.subaccountName == null) {
            throw UasException("400-13")
        }

        // Validation that the company exists
        companyRepository.findByCompanyIdAndStatusIsTrue(companyId) ?: throw  UasException("404-05")

        // Validation that the user belongs to the company
        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId) ?: throw UasException("403-15")
        logger.info("User $kcUuid is posting a subaccount")

        // Validation that the account exists
        val accountEntity = accountRepository.findByAccountIdAndStatusIsTrue(subaccountDto.accountId.toLong()) ?: throw UasException("404-09")
        if (accountEntity.companyId != companyId.toInt()) {
            throw UasException("403-15")
        }

        logger.info("Creating Subaccount")
        val subaccountEntity = Subaccount()
        subaccountEntity.companyId = companyId.toInt()
        subaccountEntity.accountId = subaccountDto.accountId.toInt()
        subaccountEntity.subaccountCode = subaccountDto.subaccountCode
        subaccountEntity.subaccountName = subaccountDto.subaccountName
        subaccountRepository.save(subaccountEntity)
        logger.info("Subaccount saved successfully")
    }

    fun getCompanySubaccounts(companyId: Long): List<SubaccountPartialDto>{
        logger.info("Starting the business logic to get company subaccounts")
        logger.info("BL call to get company subaccounts")
        // Validation that the company exists
        companyRepository.findByCompanyIdAndStatusIsTrue(companyId) ?: throw UasException("404-05")

        // Validation that the user belongs to the company
        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId) ?: throw UasException("403-16")
        logger.info("User $kcUuid is getting subaccounts")

        // Get subaccounts
        val subaccountEntities = subaccountRepository.findAllByCompanyIdAndStatusIsTrue(companyId.toInt())
        val subaccountsDto = subaccountEntities.map { SubaccountPartialMapper.entityToDto(it) }
        logger.info("Finishing the business logic to get company subaccounts")
        return subaccountsDto
    }

    fun getCompanySubaccount(companyId: Long, subaccountId: Long): SubaccountPartialDto{
        logger.info("Starting the business logic to get a company subaccount")
        logger.info("BL call to get a company subaccount")
        // Validation that the company exists
        companyRepository.findByCompanyIdAndStatusIsTrue(companyId) ?: throw UasException("404-05")

        // Validation that the user belongs to the company
        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId) ?: throw UasException("403-16")
        logger.info("User $kcUuid is getting a subaccount")

        // Get subaccount
        val subaccountEntity = subaccountRepository.findBySubaccountIdAndStatusIsTrue(subaccountId) ?: throw UasException("404-10")

        // Validation that the subaccount belongs to the company
        if (subaccountEntity.companyId != companyId.toInt()) {
            throw UasException("403-16")
        }
        logger.info("Finishing the business logic to get a company subaccount")
        return SubaccountPartialMapper.entityToDto(subaccountEntity)
    }

    fun updateSubaccount(companyId: Long, subaccountId: Long, subaccountPartialDto: SubaccountPartialDto): SubaccountPartialDto{
        logger.info("Starting the business logic to put a subaccount")
        logger.info("BL call to put a subaccount")
        // validation that at least one parameter was sent
        if (subaccountPartialDto.accountId == null && subaccountPartialDto.subaccountCode == null && subaccountPartialDto.subaccountName == null) {
            throw UasException("400-14")
        }

        // Validation that the company exists
        companyRepository.findByCompanyIdAndStatusIsTrue(companyId) ?: throw  UasException("404-05")

        // Validation that the user belongs to the company
        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId) ?: throw UasException("403-17")
        logger.info("User $kcUuid is updating a subaccount")

        // Validation that the subaccount exists
        val subaccountEntity = subaccountRepository.findBySubaccountIdAndStatusIsTrue(subaccountId) ?: throw UasException("404-10")

        // Validation that the subaccount belongs to the company
        if (subaccountEntity.companyId != companyId.toInt()) {
            throw UasException("403-17")
        }

        // Validation that the account exists and belongs to the company
        if (subaccountPartialDto.accountId != null) {
            val accountEntity = accountRepository.findByAccountIdAndStatusIsTrue(subaccountPartialDto.accountId.toLong()) ?: throw UasException("404-09")
            if (accountEntity.companyId != companyId.toInt()) {
                throw UasException("403-17")
            }
        }

        subaccountEntity.accountId = (subaccountPartialDto.accountId ?: subaccountEntity.accountId).toInt()
        subaccountEntity.subaccountCode = subaccountPartialDto.subaccountCode ?: subaccountEntity.subaccountCode
        subaccountEntity.subaccountName = subaccountPartialDto.subaccountName ?: subaccountEntity.subaccountName
        subaccountRepository.save(subaccountEntity)

        logger.info("Finishing the business logic to put a subaccount")
        return SubaccountPartialMapper.entityToDto(subaccountEntity)
    }

}