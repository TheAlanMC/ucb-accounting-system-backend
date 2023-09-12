package ucb.accounting.backend.dao

import javax.persistence.*

@Entity
@Table(name = "group")
class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_id")
    var groupId: Long = 0

    @Column(name = "group_name")
    var groupName: String = ""

    @Column(name = "status")
    var status: Boolean = true
}