package ucb.accounting.backend.mapper

import ucb.accounting.backend.dao.Subaccount
import ucb.accounting.backend.dto.SubAccountDto


class SubaccountMapper {

    companion object {
        fun entityToDto(subaccount: Subaccount): SubAccountDto {
            return SubAccountDto(
                subaccountId = subaccount.subaccountId,
                subaccountCode = subaccount.subaccountCode,
                subaccountName = subaccount.subaccountName
            )
        }
    }
}