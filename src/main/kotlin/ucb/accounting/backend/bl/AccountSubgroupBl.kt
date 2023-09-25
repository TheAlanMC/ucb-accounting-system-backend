package ucb.accounting.backend.bl

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ucb.accounting.backend.dao.AccountSubgroup
import ucb.accounting.backend.dao.repository.AccountGroupRepository
import ucb.accounting.backend.dao.repository.AccountSubgroupRepository
import ucb.accounting.backend.dao.repository.CompanyRepository
import ucb.accounting.backend.dao.repository.KcUserCompanyRepository
import ucb.accounting.backend.dto.AccoSubgroupDto
import ucb.accounting.backend.exception.UasException
import ucb.accounting.backend.util.KeycloakSecurityContextHolder

@Service
class AccountSubgroupBl @Autowired constructor(
    private val companyRepository: CompanyRepository,
    private val accountSubgroupRepository: AccountSubgroupRepository,
    private val accountGroupRepository: AccountGroupRepository,
    private val kcUserCompanyRepository: KcUserCompanyRepository
){

    companion object {
        val logger: Logger = LoggerFactory.getLogger(AccountGroupBl::class.java)
    }

    fun createAccountSubgroup(companyId: Long, accoSubgroupDto: AccoSubgroupDto) {
        logger.info("Creating account sub group")
        // Validation that the company exists
        val company = companyRepository.findByCompanyIdAndStatusIsTrue(companyId.toLong())?: throw UasException("404-05")
        val companyId = company.companyId.toInt()
        AccountingPlanBl.logger.info("Company found")
        // Validation that all the parameters were sent
        if (accoSubgroupDto.accountGroupId == null || accoSubgroupDto.accountSubgroupCode == null || accoSubgroupDto.accountSubgroupName == null) {
            throw UasException("400-09")
        }
        logger.info("Parameters found")
        // Validation that the account group exists
        val accountGroupEntity = accountGroupRepository.findByAccountGroupIdAndStatusIsTrue(accoSubgroupDto.accountGroupId.toLong())?: throw UasException("404-07")
        if (accountGroupEntity.companyId != companyId.toInt()) {
            throw UasException("403-09")
        }
        AccountingPlanBl.logger.info("Account group found")
        val accountSubgroupEntity = AccountSubgroup ()
        accountSubgroupEntity.companyId = companyId
        accountSubgroupEntity.accountGroupId = accoSubgroupDto.accountGroupId.toInt()
        accountSubgroupEntity.accountSubgroupCode = accoSubgroupDto.accountSubgroupCode
        accountSubgroupEntity.accountSubgroupName = accoSubgroupDto.accountSubgroupName
        accountSubgroupRepository.save(accountSubgroupEntity)
        logger.info("Account sub group created")
    }

    fun getAccountSubgroups(companyId: Long): List<AccoSubgroupDto> {
            logger.info("Getting account sub groups")
            // Validation that the company exists
            val company = companyRepository.findByCompanyIdAndStatusIsTrue(companyId.toLong())?: throw UasException("404-05")
            AccountingPlanBl.logger.info("Company found")
            val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
            kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId) ?: throw UasException("403-07")
            logger.info("User $kcUuid is getting account sub groups to company $companyId")
            val accountSubgroups = accountSubgroupRepository.findAllByCompanyIdAndStatusIsTrue(companyId.toInt())
            val accountSubgroupsDto = accountSubgroups.map { accountSubgroup ->
                AccoSubgroupDto(
                    accountSubgroup.accountSubgroupId,
                    accountSubgroup.accountGroupId.toLong(),
                    accountSubgroup.accountSubgroupCode,
                    accountSubgroup.accountSubgroupName
                )
            }
            logger.info("Account sub groups found")
            return accountSubgroupsDto
    }

    fun getAccountSubgroup(companyId: Long, accountSubgroupId: Long): AccoSubgroupDto {
        logger.info("Getting account sub group")
        // Validation that the company exists
        val company = companyRepository.findByCompanyIdAndStatusIsTrue(companyId.toLong())?: throw UasException("404-05")
        AccountingPlanBl.logger.info("Company found")
        // Validation that the account sub group exists
        val accountSubgroupEntity = accountSubgroupRepository.findByAccountSubgroupIdAndStatusIsTrue(accountSubgroupId)?: throw UasException("404-08")
        if (accountSubgroupEntity.companyId != companyId.toInt()) {
            throw UasException("403-10")
        }
        AccountingPlanBl.logger.info("Account sub group found")
        val accountSubgroupDto = AccoSubgroupDto(
            accountSubgroupEntity.accountSubgroupId,
            accountSubgroupEntity.accountGroupId.toLong(),
            accountSubgroupEntity.accountSubgroupCode,
            accountSubgroupEntity.accountSubgroupName
        )
        logger.info("Account sub group found")
        return accountSubgroupDto
    }

    fun updateAccountSubgroup(companyId: Long, accountSubgroupId: Long, accoSubgroupDto: AccoSubgroupDto): AccoSubgroupDto {
        logger.info("Updating account sub group")
        // Validation that the company exists
        val company = companyRepository.findByCompanyIdAndStatusIsTrue(companyId.toLong())?: throw UasException("404-05")
        AccountingPlanBl.logger.info("Company found")
        // Validation that all the parameters were sent
        if (accoSubgroupDto.accountGroupId == null || accoSubgroupDto.accountSubgroupCode == null || accoSubgroupDto.accountSubgroupName == null) {
            throw UasException("400-10")
        }
        logger.info("Parameters found")
        // Validation that the account sub group exists
        val accountSubgroupEntity = accountSubgroupRepository.findByAccountSubgroupIdAndStatusIsTrue(accountSubgroupId)?: throw UasException("404-08")
        if (accountSubgroupEntity.companyId != companyId.toInt()) {
            throw UasException("403-11")
        }
        logger.info("Account sub group found")
        accountSubgroupEntity.accountGroupId = accoSubgroupDto.accountGroupId.toInt()
        accountSubgroupEntity.accountSubgroupCode = accoSubgroupDto.accountSubgroupCode
        accountSubgroupEntity.accountSubgroupName = accoSubgroupDto.accountSubgroupName
        accountSubgroupRepository.save(accountSubgroupEntity)
        logger.info("Account sub group updated")
        return AccoSubgroupDto(
            accountSubgroupEntity.accountSubgroupId,
            accountSubgroupEntity.accountGroupId.toLong(),
            accountSubgroupEntity.accountSubgroupCode,
            accountSubgroupEntity.accountSubgroupName
        )
    }

}