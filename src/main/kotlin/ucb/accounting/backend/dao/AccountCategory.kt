package ucb.accounting.backend.dao

import javax.persistence.*

@Entity
@Table(name = "account_category")
class AccountCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_category_id")
    var accountCategoryId: Long = 0

    @Column(name = "account_category_code")
    var accountCategoryCode: Int = 0

    @Column(name = "account_category_name")
    var accountCategoryName: String = ""

    @Column(name = "status")
    var status: Boolean = true
}
