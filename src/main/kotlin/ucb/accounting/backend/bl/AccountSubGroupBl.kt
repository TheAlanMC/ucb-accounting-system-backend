package ucb.accounting.backend.bl

import org.keycloak.admin.client.Keycloak
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ucb.accounting.backend.dao.AccountSubgroup
import ucb.accounting.backend.dao.repository.AccountGroupRepository
import ucb.accounting.backend.dao.repository.AccountSubGroupRepository
import ucb.accounting.backend.dao.repository.CompanyRepository
import ucb.accounting.backend.dao.repository.KcUserCompanyRepository
import ucb.accounting.backend.dto.AccoSubGroupDto
import ucb.accounting.backend.exception.UasException
import ucb.accounting.backend.util.KeycloakSecurityContextHolder

@Service
class AccountSubGroupBl @Autowired constructor(
    private val companyRepository: CompanyRepository,
    private val accountSubGroupRepository: AccountSubGroupRepository,
    private val accountGroupRepository: AccountGroupRepository,
    private val kcUserCompanyRepository: KcUserCompanyRepository
){

    companion object {
        val logger: Logger = LoggerFactory.getLogger(AccountGroupBl::class.java)
    }

    fun createAccountSubGroup(companyId: Long, accoSubGroupDto: AccoSubGroupDto) {
        logger.info("Creating account sub group")
        // Validation that the company exists
        val company = companyRepository.findByCompanyIdAndStatusTrue(companyId.toLong())?: throw UasException("404-05")
        val companyId = company.companyId.toInt()
        AccountingPlanBl.logger.info("Company found")
        // Validation that all the parameters were sent
        if (accoSubGroupDto.accountGroupId == null || accoSubGroupDto.accountSubGroupCode == null || accoSubGroupDto.accountSubGroupName == null) {
            throw UasException("400-07")
        }
        logger.info("Parameters found")
        // Validation that the account group exists
        val accountGroupEntity = accountGroupRepository.findByAccountGroupIdAndStatusIsTrue(accoSubGroupDto.accountGroupId.toLong())?: throw UasException("404-07")
        if (accountGroupEntity.companyId != companyId.toInt()) {
            throw UasException("403-09")
        }
        AccountingPlanBl.logger.info("Account group found")
        val accountSubGroupEntity = AccountSubgroup ()
        accountSubGroupEntity.companyId = companyId
        accountSubGroupEntity.accountGroupId = accoSubGroupDto.accountGroupId.toInt()
        accountSubGroupEntity.accountSubgroupCode = accoSubGroupDto.accountSubGroupCode
        accountSubGroupEntity.accountSubgroupName = accoSubGroupDto.accountSubGroupName
        accountSubGroupRepository.save(accountSubGroupEntity)
        logger.info("Account sub group created")
    }

    fun getAccountSubGroups(companyId: Long): List<AccoSubGroupDto> {
            logger.info("Getting account sub groups")
            // Validation that the company exists
            val company = companyRepository.findByCompanyIdAndStatusTrue(companyId.toLong())?: throw UasException("404-05")
            AccountingPlanBl.logger.info("Company found")
            val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
            kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId) ?: throw UasException("403-07")
            logger.info("User $kcUuid is getting account sub groups to company $companyId")
            val accountSubGroups = accountSubGroupRepository.findAllByCompanyIdAndStatusIsTrue(companyId.toInt())
            val accountSubGroupsDto = accountSubGroups.map { accountSubGroup ->
                AccoSubGroupDto(
                    accountSubGroup.accountGroupId.toLong(),
                    accountSubGroup.accountSubgroupCode,
                    accountSubGroup.accountSubgroupName
                )
            }
            logger.info("Account sub groups found")
            return accountSubGroupsDto
    }

    fun getAccountSubGroup(companyId: Long, accountSubGroupId: Long): AccoSubGroupDto {
        logger.info("Getting account sub group")
        // Validation that the company exists
        val company = companyRepository.findByCompanyIdAndStatusTrue(companyId.toLong())?: throw UasException("404-05")
        AccountingPlanBl.logger.info("Company found")
        // Validation that the account sub group exists
        val accountSubGroupEntity = accountSubGroupRepository.findByAccountSubgroupIdAndStatusIsTrue(accountSubGroupId)?: throw UasException("404-08")
        if (accountSubGroupEntity.companyId != companyId.toInt()) {
            throw UasException("403-10")
        }
        AccountingPlanBl.logger.info("Account sub group found")
        val accountSubGroupDto = AccoSubGroupDto(
            accountSubGroupEntity.accountGroupId.toLong(),
            accountSubGroupEntity.accountSubgroupCode,
            accountSubGroupEntity.accountSubgroupName
        )
        logger.info("Account sub group found")
        return accountSubGroupDto
    }


}