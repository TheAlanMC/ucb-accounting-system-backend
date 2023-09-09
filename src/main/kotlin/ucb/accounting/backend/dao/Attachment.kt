package ucb.accounting.backend.dao

import javax.persistence.*

@Entity
@Table(name = "attachment")
class Attachment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attachment_id")
    var attachmentId: Long = 0

    @Column(name = "company_id")
    var companyId: Int = 0

    @Column(name = "content_type")
    var contentType: String = ""

    @Column(name = "filename")
    var filename: String = ""

    @Column(name = "file_data")
    var fileData: ByteArray = ByteArray(0)

    @Column(name = "status")
    var status: Boolean = true

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", referencedColumnName = "company_id", insertable = false, updatable = false)
    var company: Company? = null
}