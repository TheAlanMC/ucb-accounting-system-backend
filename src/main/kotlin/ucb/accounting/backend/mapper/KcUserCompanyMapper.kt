package ucb.accounting.backend.mapper

import ucb.accounting.backend.dao.KcUserCompany
import ucb.accounting.backend.dto.UserCompanyDto

class KcUserCompanyMapper {
    companion object {
        fun entityToDto(kcUserCompany: KcUserCompany): UserCompanyDto {
            return UserCompanyDto(
                kcUuid = kcUserCompany.kcUser!!.kcUuid,
                companyId = kcUserCompany.company!!.companyId,
                groupId = kcUserCompany.kcGroup!!.kcGroupId
            )
        }
    }
}
