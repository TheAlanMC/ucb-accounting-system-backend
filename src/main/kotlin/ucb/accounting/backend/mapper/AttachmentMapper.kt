package ucb.accounting.backend.mapper

import ucb.accounting.backend.dao.Attachment
import ucb.accounting.backend.dto.AttachmentDto

class AttachmentMapper {

    companion object{
        fun entityToDto(attachment: Attachment): AttachmentDto {
            return AttachmentDto(
                attachmentId = attachment.attachmentId,
                contentType = attachment.contentType,
                filename = attachment.filename
            )
        }
    }

}