package ucb.accounting.backend.dao.repository

import org.springframework.data.jpa.repository.JpaRepository
import ucb.accounting.backend.dao.DocumentType

interface DocumentTypeRepository: JpaRepository<DocumentType, Long> {
    fun findByDocumentTypeIdAndStatusTrue (documentTypeId: Long): DocumentType?

    fun findAllByStatusTrue(): List<DocumentType>

    fun findByDocumentTypeNameAndStatusIsTrue (documentTypeName: String): DocumentType?
}