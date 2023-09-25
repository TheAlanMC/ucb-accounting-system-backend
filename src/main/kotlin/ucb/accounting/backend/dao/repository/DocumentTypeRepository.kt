package ucb.accounting.backend.dao.repository

import org.springframework.data.jpa.repository.JpaRepository
import ucb.accounting.backend.dao.DocumentType

interface DocumentTypeRepository: JpaRepository<DocumentType, Long> {
    fun findByDocumentTypeIdAndStatusIsTrue (documentTypeId: Long): DocumentType?

    fun findAllByStatusIsTrue(): List<DocumentType>

    fun findByDocumentTypeNameAndStatusIsTrue (documentTypeName: String): DocumentType?
}