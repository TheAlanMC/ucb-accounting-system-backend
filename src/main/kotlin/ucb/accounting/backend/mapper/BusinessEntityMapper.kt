package ucb.accounting.backend.mapper

import ucb.accounting.backend.dao.BusinessEntity
import ucb.accounting.backend.dto.BusinessEntityDto

class BusinessEntityMapper {
    companion object{
        fun entityToDto(businessEntity: BusinessEntity): BusinessEntityDto{
            return BusinessEntityDto(
                businessEntityId = businessEntity.businessEntityId,
                businessEntityName = businessEntity.businessEntityName
            )
        }
    }
}