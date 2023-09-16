package ucb.accounting.backend.mapper

import ucb.accounting.backend.dao.Account
import ucb.accounting.backend.dto.AccountDto


class AccountMapper {

    companion object {
        fun entityToDto(account: Account): AccountDto {
            return AccountDto(
                accountId = account.accountId,
                accountCode = account.accountCode,
                accountName = account.accountName,
                subaccounts = account.accountSubaccounts?.map { SubaccountMapper.entityToDto(it) }
            )
        }
    }

}