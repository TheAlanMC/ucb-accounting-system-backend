package ucb.accounting.backend.mapper

import ucb.accounting.backend.dao.Subaccount
import ucb.accounting.backend.dto.SubaccountDto


class SubaccountMapper {

    companion object {
        fun entityToDto(subaccount: Subaccount): SubaccountDto {
            return SubaccountDto(
                subaccountId = subaccount.subaccountId,
                subaccountCode = subaccount.subaccountCode,
                subaccountName = subaccount.subaccountName
            )
        }
    }
}