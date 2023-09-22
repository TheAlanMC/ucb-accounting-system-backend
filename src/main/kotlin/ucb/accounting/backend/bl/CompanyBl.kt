package ucb.accounting.backend.bl

import org.springframework.beans.factory.annotation.Autowired
import org.slf4j.LoggerFactory
import org.slf4j.Logger
import org.springframework.stereotype.Controller
import org.springframework.stereotype.Service
import ucb.accounting.backend.dao.Company
import ucb.accounting.backend.dao.S3Object
import ucb.accounting.backend.dao.repository.*
import ucb.accounting.backend.dto.BusinessEntityDto
import ucb.accounting.backend.dto.CompanyDto
import ucb.accounting.backend.dto.IndustryDto
import ucb.accounting.backend.exception.UasException
import ucb.accounting.backend.mapper.CompanyMapper
import ucb.accounting.backend.service.MinioService
import ucb.accounting.backend.util.KeycloakSecurityContextHolder

@Service
class CompanyBl @Autowired constructor(
    private val s3ObjectRepository: S3ObjectRepository,
    private val companyRepository: CompanyRepository,
    private val minioService: MinioService,
    private val kcUserCompanyRepository: KcUserCompanyRepository,
    private val industryRepository: IndustryRepository,
    private val businessEntityRepository: BusinessEntityRepository
) {
    companion object {
        private val logger = LoggerFactory.getLogger(CompanyBl::class.java.name)
    }

    fun getCompanyInfo(companyId: Long): CompanyDto {
        logger.info("Starting the BL call to get company info")
        logger.info("BL call to get company info")
        val company = companyRepository.findByCompanyIdAndStatusTrue(companyId) ?: throw UasException("404-05")

        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        logger.info("User $kcUuid is getting company info")
        // Validation of user belongs to company
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId) ?: throw UasException("403-03")

        // Get s3 object
        val s3Object: S3Object = s3ObjectRepository.findByS3ObjectIdAndStatusIsTrue(company.s3CompanyLogo.toLong())!!
        val preSignedUrl: String = minioService.getPreSignedUrl(s3Object.bucket, s3Object.filename)

        return CompanyMapper.entityToDto(company, preSignedUrl)
    }

    fun createCompany (companyDto: CompanyDto){
        logger.info("Starting the BL call to post company info")

        industryRepository.findByIndustryIdAndStatusTrue(companyDto.industry.industryId) ?: throw UasException("404-03")

        businessEntityRepository.findByBusinessEntityIdAndStatusTrue(companyDto.businessEntity.businessEntityId) ?: throw UasException("404-04")

        // Validation that company fields are not null
        if (companyDto.companyName.isEmpty()) throw UasException("400-05")
        if (companyDto.companyNit.isEmpty()) throw UasException("400-05")
        if (companyDto.companyAddress.isEmpty()) throw UasException("400-05")
        if (companyDto.phoneNumber.isEmpty()) throw UasException("400-05")
        if (companyDto.companyLogo.isEmpty()) throw UasException("400-05")

        /*logger.info("Saving s3 object")
        val s3ObjectEntity = S3Object()
        s3ObjectEntity.filename = "companyLogo"
        s3ObjectEntity.bucket = "ucb-accounting"
        s3ObjectEntity.filename = companyDto.companyLogo
        val savedS3Object = s3ObjectRepository.save(s3ObjectEntity)*/

        logger.info("Saving company")
        val companyEntity = Company()
        companyEntity.industryId = companyDto.industry.industryId.toInt()
        companyEntity.businessEntityId = companyDto.businessEntity.businessEntityId.toInt()
        companyEntity.companyName = companyDto.companyName
        companyEntity.companyNit = companyDto.companyNit
        companyEntity.companyAddress = companyDto.companyAddress
        companyEntity.phoneNumber = companyDto.phoneNumber
        // TODO: Change this to the s3 object id
        companyEntity.s3CompanyLogo = 1//savedS3Object.s3ObjectId.toInt()
        val savedCompany = companyRepository.save(companyEntity)
        logger.info("Company saved successfully")
    }

    fun updateCompany (companyDto: CompanyDto, companyId: Long): CompanyDto {
        logger.info("Starting the BL call to put company info")

        industryRepository.findByIndustryIdAndStatusTrue(companyDto.industry.industryId)
            ?: throw UasException("404-03")

        businessEntityRepository.findByBusinessEntityIdAndStatusTrue(companyDto.businessEntity.businessEntityId)
                ?: throw UasException("404-04")

        val company = companyRepository.findByCompanyIdAndStatusTrue(companyId) ?: throw UasException("404-05")

        // Validation that company fields are not null
        if (companyDto.companyName.isEmpty()) throw UasException("400-06")
        if (companyDto.companyNit.isEmpty()) throw UasException("400-06")
        if (companyDto.companyAddress.isEmpty()) throw UasException("400-06")
        if (companyDto.phoneNumber.isEmpty()) throw UasException("400-06")
        if (companyDto.companyLogo.isEmpty()) throw UasException("400-06")

        /*val kcUuid = KeycloakSecurityContextHolder.getSubject()!!

        logger.info("User $kcUuid is updating a company")
        // Validation of user belongs to company
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId)
            ?: throw UasException("403-04")*/

        /*logger.info("Saving s3 object")
        val s3ObjectEntity = S3Object()
        s3ObjectEntity.filename = "companyLogo"
        s3ObjectEntity.bucket = "ucb-accounting"
        s3ObjectEntity.filename = companyDto.companyLogo
        val savedS3Object = s3ObjectRepository.save(s3ObjectEntity)*/

        logger.info("Updating company")

        company.industryId = companyDto.industry.industryId.toInt()
        company.businessEntityId = companyDto.businessEntity.businessEntityId.toInt()
        company.companyName = companyDto.companyName
        company.companyNit = companyDto.companyNit
        company.companyAddress = companyDto.companyAddress
        company.phoneNumber = companyDto.phoneNumber
        //TODO: Change this to the s3 object id
        company.s3CompanyLogo = 1//savedS3Object.s3ObjectId.toInt()

        val updatedCompany = companyRepository.save(company)

        val s3Object: S3Object = s3ObjectRepository.findByS3ObjectIdAndStatusIsTrue(company.s3CompanyLogo.toLong())!!
        val preSignedUrl: String = minioService.getPreSignedUrl(s3Object.bucket, s3Object.filename)

        logger.info("Company updated successfully")
        return CompanyMapper.entityToDto(updatedCompany, preSignedUrl)
    }
}