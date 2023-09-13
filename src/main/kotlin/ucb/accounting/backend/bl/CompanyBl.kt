package ucb.accounting.backend.bl

import org.springframework.beans.factory.annotation.Autowired
import org.slf4j.LoggerFactory
import org.slf4j.Logger
import org.springframework.stereotype.Controller
import ucb.accounting.backend.dao.repository.CompanyRepository
import ucb.accounting.backend.dto.BusinessEntityDto
import ucb.accounting.backend.dto.CompanyDto
import ucb.accounting.backend.dto.IndustryDto
import ucb.accounting.backend.exception.UasException

@Controller
class CompanyBl @Autowired constructor(
    private val companyRepository: CompanyRepository
) {
    companion object {
        private val logger = LoggerFactory.getLogger(CompanyBl::class.java.name)
    }

    fun getCompanyInfo(companyId: Long): CompanyDto {
        logger.info("Starting the BL call to get company info")
        logger.info("BL call to get company info")
        val company = companyRepository.findByCompanyIdAndStatusTrue(companyId) ?: throw UasException("404-05")
        val industry = company?.industry
        val businessEntity = company?.businessEntity

        return CompanyDto(
            industry = IndustryDto(
                industryId = industry?.industryId!!,
                industryName = industry?.industryName!!
            ),
            businessEntity = BusinessEntityDto(
                businessEntityId = businessEntity?.businessEntityId!!,
                businessEntityName = businessEntity?.businessEntityName!!
            ),
            companyName = company.companyName,
            companyNit = company.companyNit,
            companyAddress = company.companyAddress,
            phoneNumber = company.phoneNumber,
            //TODO: Change this to the real logo
            companyLogo = "https://aidajerusalem.org/wp-content/uploads/2021/09/blank-profile-picture-973460_1280.png"
        )
    }
}