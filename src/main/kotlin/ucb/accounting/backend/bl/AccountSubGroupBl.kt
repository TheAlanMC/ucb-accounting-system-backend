package ucb.accounting.backend.bl

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ucb.accounting.backend.dao.AccountSubgroup
import ucb.accounting.backend.dao.repository.AccountGroupRepository
import ucb.accounting.backend.dao.repository.AccountSubGroupRepository
import ucb.accounting.backend.dao.repository.CompanyRepository
import ucb.accounting.backend.dto.AccoSubGroupDto
import ucb.accounting.backend.exception.UasException

@Service
class AccountSubGroupBl @Autowired constructor(
    private val companyRepository: CompanyRepository,
    private val accountSubGroupRepository: AccountSubGroupRepository,
    private val accountGroupRepository: AccountGroupRepository
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
        accountGroupRepository.findByAccountGroupIdAndStatusIsTrue(accoSubGroupDto.accountGroupId.toLong())?: throw UasException("404-07")
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
            val accountSubGroupList = accountSubGroupRepository.findAllByCompanyIdAndStatusIsTrue(companyId.toInt())
            val accountSubGroups = accountSubGroupList.map { accountSubgroup ->
                AccoSubGroupDto(
                    accountSubgroup.accountGroupId,
                    accountSubgroup.accountSubgroupCode,
                    accountSubgroup.accountSubgroupName
                )
            }
            logger.info("Account sub groups found")
            return accountSubGroups
    }


}