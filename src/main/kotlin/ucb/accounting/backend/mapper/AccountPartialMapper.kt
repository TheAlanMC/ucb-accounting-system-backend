package ucb.accounting.backend.mapper

import ucb.accounting.backend.dao.Account
import ucb.accounting.backend.dto.AccountPartialDto


class AccountPartialMapper {

    companion object {
        fun entityToDto(account: Account): AccountPartialDto {
            return AccountPartialDto(
                account.accountId,
                account.accountSubgroupId.toLong(),
                account.accountCode,
                account.accountName
            )
        }
    }
}
