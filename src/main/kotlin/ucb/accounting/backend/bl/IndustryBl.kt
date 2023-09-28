package ucb.accounting.backend.bl

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ucb.accounting.backend.dao.repository.IndustryRepository
import ucb.accounting.backend.dto.IndustryDto
import ucb.accounting.backend.mapper.IndustryMapper

@Service
class IndustryBl @Autowired constructor(
    private val industryRepository: IndustryRepository
) {
    companion object{
        private val logger = LoggerFactory.getLogger(IndustryBl::class.java.name)
    }

    fun getIndustries(): List<IndustryDto> {
        logger.info("Starting the BL call to get industries")
        val industryEntities = industryRepository.findAllByStatusIsTrue()
        logger.info("Found ${industryEntities.size} industries")
        logger.info("Finishing the BL call to get industries")
        return industryEntities.map { IndustryMapper.entityToDto(it) }
    }
}