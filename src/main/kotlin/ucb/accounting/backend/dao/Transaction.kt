package ucb.accounting.backend.dao

import ucb.accounting.backend.util.HttpUtil
import ucb.accounting.backend.util.KeycloakSecurityContextHolder
import java.sql.Date
import java.sql.Timestamp
import javax.persistence.*

@Entity
@Table(name = "transaction")
class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    var transactionId: Long = 0

    @Column(name = "journal_entry_id")
    var journalEntryId: Int = 0

    @Column(name = "transaction_date")
    var transactionDate: Date = Date(System.currentTimeMillis())

    @Column(name = "description")
    var description: String = ""

    @Column(name = "status")
    var status: Boolean = true

    @Column(name = "tx_date")
    var txDate: Timestamp = Timestamp(System.currentTimeMillis())

    @Column(name = "tx_user")
    var txUser: String = KeycloakSecurityContextHolder.getSubject() ?: "admin"

    @Column(name = "tx_host")
    var txHost: String = HttpUtil.getRequestHost() ?: "localhost"

    @OneToMany(mappedBy = "transaction")
    var transactionDetails: List<TransactionDetail>? = null

    @OneToMany(mappedBy = "transaction")
    var transactionAttachments: List<TransactionAttachment>? = null

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journal_entry_id", referencedColumnName = "journal_entry_id", insertable = false, updatable = false)
    var journalEntry: JournalEntry? = null

}