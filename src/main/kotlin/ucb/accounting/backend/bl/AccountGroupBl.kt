package ucb.accounting.backend.bl

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ucb.accounting.backend.dao.AccountGroup
import ucb.accounting.backend.dao.repository.AccountCategoryRepository
import ucb.accounting.backend.dao.repository.AccountGroupRepository
import ucb.accounting.backend.dao.repository.CompanyRepository
import ucb.accounting.backend.dao.repository.KcUserCompanyRepository
import ucb.accounting.backend.dto.AccoGroupDto
import ucb.accounting.backend.exception.UasException
import ucb.accounting.backend.mapper.AccountGroupMapper
import ucb.accounting.backend.util.KeycloakSecurityContextHolder

@Service
class AccountGroupBl @Autowired constructor(
    private val companyRepository: CompanyRepository,
    private val accountCategoryRepository: AccountCategoryRepository,
    private val accountGroupRepository: AccountGroupRepository,
    private val kcUserCompanyRepository: KcUserCompanyRepository
){

    companion object {
        val logger: Logger = LoggerFactory.getLogger(AccountGroupBl::class.java)
    }

    fun createAccountGroup(id: Long, accoGroupDto: AccoGroupDto) {
        logger.info("Creating account group")
        // Validation that the company exists
        val company = companyRepository.findByCompanyIdAndStatusTrue(id.toLong())?: throw UasException("404-05")
        val companyId = company.companyId.toInt()
        AccountingPlanBl.logger.info("Company found")
        // Validation that all the parameters were sent
        if (accoGroupDto.accountCategoryId == null || accoGroupDto.accountGroupCode == null || accoGroupDto.accountGroupName == null) {
            throw UasException("400-07")
        }
        logger.info("Parameters found")
        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, id) ?: throw UasException("403-18")
        logger.info("User $kcUuid is creating account group to company $id")
        // Validation that the account category exists
        accountCategoryRepository.findByAccountCategoryIdAndStatusIsTrue(accoGroupDto.accountCategoryId.toLong())?: throw UasException("404-06")
        AccountingPlanBl.logger.info("Account category found")
        val accountGroupEntity = AccountGroup ()
        accountGroupEntity.companyId = id.toInt()
        accountGroupEntity.accountCategoryId = accoGroupDto.accountCategoryId.toInt()
        accountGroupEntity.accountGroupCode = accoGroupDto.accountGroupCode
        accountGroupEntity.accountGroupName = accoGroupDto.accountGroupName
        accountGroupRepository.save(accountGroupEntity)
        logger.info("Account group created")
    }

    fun getAccountGroups(companyId: Long): List<AccoGroupDto> {

        logger.info("Getting account groups")
        // Validation that the company exists
        val company = companyRepository.findByCompanyIdAndStatusTrue(companyId.toLong())?: throw UasException("404-05")
        AccountingPlanBl.logger.info("Company found")
        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId) ?: throw UasException("403-18")
        logger.info("User $kcUuid is getting account groups from company $companyId")
        val accountGroupEntities = accountGroupRepository.findAllByStatusIsTrue()
        val accountGroups = accountGroupEntities.map { accountGroup ->
            AccoGroupDto(
                accountGroup.accountGroupId,
                accountGroup.accountGroupCode,
                accountGroup.accountGroupName
            )
        }
        logger.info("Account groups found")
        return accountGroups
    }

}