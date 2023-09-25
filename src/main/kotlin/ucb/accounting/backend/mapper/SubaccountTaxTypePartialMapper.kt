package ucb.accounting.backend.mapper

import ucb.accounting.backend.dao.SubaccountTaxType
import ucb.accounting.backend.dto.SubaccountTaxTypePartialDto

class SubaccountTaxTypePartialMapper {
    companion object{
        fun entityToDto(subaccountTaxType: SubaccountTaxType): SubaccountTaxTypePartialDto {
            return SubaccountTaxTypePartialDto(
                taxType = TaxTypeMapper.entityToDto(subaccountTaxType.taxType!!),
                subaccount = SubaccountMapper.entityToDto(subaccountTaxType.subaccount!!),
                taxRate = subaccountTaxType.taxRate
            )
        }
    }
}