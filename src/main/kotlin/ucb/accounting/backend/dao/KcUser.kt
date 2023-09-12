package ucb.accounting.backend.dao

import javax.persistence.*

@Entity
@Table(name = "kc_user")
class KcUser {
    @Id
    @Column(name = "kc_uuid")
    var kcUuid: String = ""

    @Column(name = "s3_profile_picture")
    var s3ProfilePicture: Int = 0

    @Column(name = "status")
    var status: Boolean = true

    @OneToMany(mappedBy = "kcUser")
    var kcUserCompany: List<KcUserCompany>? = null
}