package ucb.accounting.backend.dao

import javax.persistence.*

@Entity
@Table(name = "business_entity")
class BusinessEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "business_entity_id")
    var businessEntityId: Long = 0

    @Column(name = "business_entity_name")
    var businessEntityName: String = ""

    @Column(name = "description")
    var description: String = ""

    @Column(name = "status")
    var status: Boolean = true
}