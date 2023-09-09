package ucb.accounting.backend.dao

import java.sql.Date
import javax.persistence.*

@Entity
@Table(name = "transaction")
class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    var transactionId: Long = 0

    @Column(name = "company_id")
    var companyId: Int = 0

    @Column(name = "journal_entry_id")
    var journalEntryId: Int = 0

    @Column(name = "transaction_date")
    var transactionDate: Date = Date(System.currentTimeMillis())

    @Column(name = "description")
    var description: String = ""

    @Column(name = "status")
    var status: Boolean = true

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", referencedColumnName = "company_id", insertable = false, updatable = false)
    var company: Company? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journal_entry_id", referencedColumnName = "journal_entry_id", insertable = false, updatable = false)
    var journalEntry: JournalEntry? = null

    @OneToMany(mappedBy = "transaction")
    var transactionDetails: List<TransactionDetail>? = null

    @OneToMany(mappedBy = "transaction")
    var transactionAttachments: List<TransactionAttachment>? = null
}