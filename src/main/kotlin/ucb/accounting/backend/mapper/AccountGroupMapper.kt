package ucb.accounting.backend.mapper

import ucb.accounting.backend.dao.AccountGroup
import ucb.accounting.backend.dto.AccountGroupDto

class AccountGroupMapper {

    companion object {
        fun entityToDto(group: AccountGroup): AccountGroupDto {
            return AccountGroupDto(
                accountGroupId = group.accountGroupId,
                accountGroupCode = group.accountGroupCode,
                accountGroupName = group.accountGroupName,
                accountSubgroups = group.accountSubgroups?.map { AccountSubgroupMapper.entityToDto(it) }
            )
        }
    }

}