package ucb.accounting.backend.dao

import javax.persistence.*

@Entity
@Table(name = "document_type")
class DocumentType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "document_type_id")
    var documentTypeId: Long = 0

    @Column(name = "document_type_name")
    var documentTypeName: String = ""

    @Column(name = "status")
    var status: Boolean = true

    @OneToMany(mappedBy = "documentType")
    var journalEntries: List<JournalEntry>? = null
}
