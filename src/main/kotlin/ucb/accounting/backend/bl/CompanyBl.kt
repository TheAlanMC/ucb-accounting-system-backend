package ucb.accounting.backend.bl

import org.springframework.beans.factory.annotation.Autowired
import org.slf4j.LoggerFactory
import org.slf4j.Logger
import org.springframework.stereotype.Controller
import org.springframework.stereotype.Service
import ucb.accounting.backend.dao.S3Object
import ucb.accounting.backend.dao.repository.CompanyRepository
import ucb.accounting.backend.dao.repository.KcUserCompanyRepository
import ucb.accounting.backend.dao.repository.S3ObjectRepository
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
) {
    companion object {
        private val logger = LoggerFactory.getLogger(CompanyBl::class.java.name)
    }

    fun getCompanyInfo(companyId: Long): CompanyDto {
        logger.info("Starting the BL call to get company info")
        logger.info("BL call to get company info")
        val company = companyRepository.findByCompanyIdAndStatusIsTrue(companyId) ?: throw UasException("404-05")

        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
        logger.info("User $kcUuid is getting company info")
        // Validation of user belongs to company
        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId) ?: throw UasException("403-03")

        // Get s3 object
        val s3Object: S3Object = s3ObjectRepository.findByS3ObjectIdAndStatusIsTrue(company.s3CompanyLogo.toLong())!!
        val preSignedUrl: String = minioService.getPreSignedUrl(s3Object.bucket, s3Object.filename)

        return CompanyMapper.entityToDto(company, preSignedUrl)
    }
}