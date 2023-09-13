package ucb.accounting.backend.dao.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ucb.accounting.backend.dao.Attachment

@Repository
interface AttachmentRepository: JpaRepository<Attachment, Long> {
}
