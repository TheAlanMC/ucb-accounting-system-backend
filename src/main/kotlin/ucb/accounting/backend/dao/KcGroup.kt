package ucb.accounting.backend.dao

import javax.persistence.*

@Entity
@Table(name = "kc_group")
class KcGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "kc_group_id")
    var kcGroupId: Long = 0

    @Column(name = "kc_group_name")
    var groupName: String = ""

    @Column(name = "status")
    var status: Boolean = true

    @OneToMany(mappedBy = "kcGroup")
    var kcUserCompany: List<KcUserCompany>? = null
}