package ucb.accounting.backend.dao

import javax.persistence.*

@Entity
@Table(name = "accountant")
class Accountant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "accountant_id")
    var accountantId: Long = 0

    @Column(name = "kc_uuid")
    var kcUuid: String = ""

    @Column(name = "accountant_name")
    var accountantName: String = ""

    @Column(name = "s3_profile_picture")
    var s3ProfilePicture: Int = 0

    @Column(name = "status")
    var status: Boolean = true
}