package ucb.accounting.backend.dao

import javax.persistence.*
import java.sql.Timestamp

@Entity
@Table(name = "journal_entry")
class JournalEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "journal_entry_id")
    var journalEntryId: Long = 0

    @Column(name = "company_id")
    var companyId: Int = 0

    @Column(name = "document_type_id")
    var documentTypeId: Int = 0

    @Column(name = "journal_entry_number")
    var journalEntryNumber: Int = 0

    @Column(name = "entry_date")
    var entryDate: Timestamp = Timestamp(System.currentTimeMillis())

    @Column(name = "gloss")
    var gloss: String = ""

    @Column(name = "journal_entry_accepted")
    var journalEntryAccepted: Boolean = false

    @Column(name = "status")
    var status: Boolean = true

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", referencedColumnName = "company_id", insertable = false, updatable = false)
    var company: Company? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_type_id", referencedColumnName = "document_type_id", insertable = false, updatable = false)
    var documentType: DocumentType? = null
}