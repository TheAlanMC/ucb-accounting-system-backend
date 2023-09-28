package ucb.accounting.backend.bl

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ucb.accounting.backend.dao.repository.BusinessEntityRepository
import ucb.accounting.backend.dto.BusinessEntityDto
import ucb.accounting.backend.mapper.BusinessEntityMapper

@Service
class BusinessEntityBl @Autowired constructor(
    private val businessEntityRepository: BusinessEntityRepository
) {
    companion object{
        private val logger = LoggerFactory.getLogger(BusinessEntityBl::class.java.name)
    }

    fun getBusinessEntities(): List<BusinessEntityDto> {
        logger.info("Starting the BL call to get business entities")
        val businessEntitiesEntities = businessEntityRepository.findAllByStatusIsTrue()
        logger.info("Found ${businessEntitiesEntities.size} business entities")
        logger.info("Finishing the BL call to get business entities")
        return businessEntitiesEntities.map { BusinessEntityMapper.entityToDto(it) }
    }
}