package ucb.accounting.backend.dao

import javax.persistence.*

@Entity
@Table(name = "client")
class Client {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "client_id")
    var clientId: Long = 0

    @Column(name = "kc_uuid")
    var kcUuid: String = ""

    @Column(name = "client_name")
    var clientName: String = ""

    @Column(name = "s3_profile_picture")
    var s3ProfilePicture: Int = 0

    @Column(name = "status")
    var status: Boolean = true
}