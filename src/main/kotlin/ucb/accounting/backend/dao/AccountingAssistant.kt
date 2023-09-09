package ucb.accounting.backend.dao

import javax.persistence.*

@Entity
@Table(name = "accounting_assistant")
class AccountingAssistant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "accounting_assistant_id")
    var accountingAssistantId: Long = 0

    @Column(name = "kc_uuid")
    var kcUuid: String = ""

    @Column(name = "assistant_name")
    var assistantName: String = ""

    @Column(name = "s3_profile_picture")
    var s3ProfilePicture: Int = 0

    @Column(name = "status")
    var status: Boolean = true
}
