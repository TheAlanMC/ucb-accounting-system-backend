package ucb.accounting.backend.mapper

import ucb.accounting.backend.dao.Industry
import ucb.accounting.backend.dto.IndustryDto

class IndustryMapper {
    companion object{
        fun entityToDto(industry: Industry): IndustryDto {
            return IndustryDto(
                industryId = industry.industryId,
                industryName = industry.industryName
            )
        }
    }
}