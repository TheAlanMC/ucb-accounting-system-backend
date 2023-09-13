package ucb.accounting.backend.mapper

import ucb.accounting.backend.dao.AccountSubgroup
import ucb.accounting.backend.dto.AccountSubgroupDto

class AccountSubgroupMapper {

    companion object {
        fun entityToDto(subgroup: AccountSubgroup): AccountSubgroupDto {
            return AccountSubgroupDto(
                accountSubgroupId = subgroup.accountSubgroupId,
                accountSubgroupCode = subgroup.accountSubgroupCode,
                accountSubgroupName = subgroup.accountSubgroupName,
                accounts = subgroup.accounts?.map { AccountMapper.entityToDto(it) }
            )
        }
    }

}