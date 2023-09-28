package ucb.accounting.backend.mapper

import ucb.accounting.backend.dao.AccountGroup
import ucb.accounting.backend.dto.AccountGroupPartialDto

class AccountGroupPartialMapper {

    companion object {
        fun entityToDto(accountGroup: AccountGroup): AccountGroupPartialDto {
            return AccountGroupPartialDto(
                accountGroupId = accountGroup.accountGroupId,
                accountCategoryId = accountGroup.accountCategoryId.toLong(),
                accountGroupCode = accountGroup.accountGroupCode,
                accountGroupName = accountGroup.accountGroupName
            )
        }
    }
}