package ucb.accounting.backend.mapper

import ucb.accounting.backend.dao.TaxType
import ucb.accounting.backend.dto.TaxTypeDto

class TaxTypeMapper {
    companion object{
        fun entityToDto(taxType: TaxType): TaxTypeDto {
            return TaxTypeDto(
                taxTypeId = taxType.taxTypeId,
                taxTypeName = taxType.taxTypeName,
                description = taxType.description
            )
        }
    }
}