package ucb.accounting.backend.mapper

import ucb.accounting.backend.dao.Company
import ucb.accounting.backend.dto.CompanyDto

class CompanyMapper {
    companion object{
        fun entityToDto(company: Company, preSignedUrl: String): CompanyDto {
            return CompanyDto(
                industry = IndustryMapper.entityToDto(company.industry!!),
                businessEntity = BusinessEntityMapper.entityToDto(company.businessEntity!!),
                companyName = company.companyName,
                companyNit = company.companyNit,
                companyAddress = company.companyAddress,
                phoneNumber = company.phoneNumber,
                companyLogo = preSignedUrl
            )
        }
    }
}