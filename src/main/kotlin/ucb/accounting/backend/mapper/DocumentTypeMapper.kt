package ucb.accounting.backend.mapper

import ucb.accounting.backend.dao.DocumentType
import ucb.accounting.backend.dto.DocumentTypeDto

class DocumentTypeMapper {

    companion object{
        fun entityToDto(documentType: DocumentType): DocumentTypeDto {
            return DocumentTypeDto(
                documentTypeId = documentType.documentTypeId,
                documentTypeName = documentType.documentTypeName
            )
        }
    }
}