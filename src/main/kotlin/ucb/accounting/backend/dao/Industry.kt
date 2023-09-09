package ucb.accounting.backend.dao

import javax.persistence.*

@Entity
@Table(name = "industry")
class Industry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "industry_id")
    var industryId: Long = 0

    @Column(name = "industry_name")
    var industryName: String = ""

    @Column(name = "status")
    var status: Boolean = true
}