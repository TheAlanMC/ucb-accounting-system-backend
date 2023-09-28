package ucb.accounting.backend.mapper

import ucb.accounting.backend.dao.Subaccount

import ucb.accounting.backend.dto.SubaccountPartialDto

class SubaccountPartialMapper {

    companion object {
        fun entityToDto(subaccount: Subaccount): SubaccountPartialDto {
            return SubaccountPartialDto(
                subaccount.subaccountId,
                subaccount.accountId.toLong(),
                subaccount.subaccountCode,
                subaccount.subaccountName
            )
        }
    }
}