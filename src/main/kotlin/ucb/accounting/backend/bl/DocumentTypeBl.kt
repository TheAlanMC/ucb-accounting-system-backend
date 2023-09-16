package ucb.accounting.backend.bl

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ucb.accounting.backend.dao.repository.DocumentTypeRepository
import ucb.accounting.backend.dto.DocumentTypeDto
import ucb.accounting.backend.mapper.DocumentTypeMapper

@Service
class DocumentTypeBl @Autowired constructor(
    private val documentTypeRepository: DocumentTypeRepository
) {
    companion object{
        private val logger = LoggerFactory.getLogger(DocumentTypeBl::class.java.name)
    }

    fun getDocumentTypes(): List<DocumentTypeDto> {
        logger.info("Starting the BL call to get document types")
        val documentTypes = documentTypeRepository.findAllByStatusTrue()
        logger.info("Found ${documentTypes.size} document types")
        logger.info("Finishing the BL call to get document types")
        return documentTypes.map { DocumentTypeMapper.entityToDto(it) }
    }
}