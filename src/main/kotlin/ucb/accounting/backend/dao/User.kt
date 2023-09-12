package ucb.accounting.backend.dao

import javax.persistence.*

@Entity
@Table(name = "accountant")
class User {
    @Id
    @Column(name = "kc_uuid")
    var kcUuid: String = ""

    @Column(name = "s3_profile_picture")
    var s3ProfilePicture: Int = 0

    @Column(name = "status")
    var status: Boolean = true
}