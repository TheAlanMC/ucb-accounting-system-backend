package ucb.accounting.backend.mapper

import ucb.accounting.backend.dao.AccountSubgroup
import ucb.accounting.backend.dto.AccountSubgroupPartialDto

class AccountSubgroupPartialMapper {
    companion object {
        fun entityToDto(accountSubgroup: AccountSubgroup): AccountSubgroupPartialDto {
            return AccountSubgroupPartialDto(
                accountSubgroup.accountSubgroupId,
                accountSubgroup.accountGroupId.toLong(),
                accountSubgroup.accountSubgroupCode,
                accountSubgroup.accountSubgroupName
            )
        }
    }
}