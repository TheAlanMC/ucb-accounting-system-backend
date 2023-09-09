package ucb.accounting.backend.dao

import javax.persistence.*

@Entity
@Table(name = "company")
class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "company_id")
    var companyId: Long = 0

    @Column(name = "industry_id")
    var industryId: Int = 0

    @Column(name = "business_entity_id")
    var businessEntityId: Int = 0

    @Column(name = "accountant_id")
    var accountantId: Int = 0

    @Column(name = "company_name")
    var companyName: String = ""

    @Column(name = "company_nit")
    var companyNit: String = ""

    @Column(name = "company_address")
    var companyAddress: String = ""

    @Column(name = "phone_number")
    var phoneNumber: String = ""

    @Column(name = "s3_company_logo")
    var s3CompanyLogo: Int = 0

    @Column(name = "status")
    var status: Boolean = true

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "industry_id", referencedColumnName = "industry_id", insertable = false, updatable = false)
    var industry: Industry? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_entity_id", referencedColumnName = "business_entity_id", insertable = false, updatable = false)
    var businessEntity: BusinessEntity? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accountant_id", referencedColumnName = "accountant_id", insertable = false, updatable = false)
    var accountant: Accountant? = null

    @OneToMany(mappedBy = "company")
    var companyAccountingAssistants: List<CompanyAccountingAssistant>? = null

    @OneToMany(mappedBy = "company")
    var companyClients: List<CompanyClient>? = null

}
