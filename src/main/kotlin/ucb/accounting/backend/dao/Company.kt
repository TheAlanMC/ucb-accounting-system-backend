package ucb.accounting.backend.dao

import ucb.accounting.backend.util.HttpUtil
import ucb.accounting.backend.util.KeycloakSecurityContextHolder
import java.sql.Timestamp
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

    @Column(name = "tx_date")
    var txDate: Timestamp = Timestamp(System.currentTimeMillis())

    @Column(name = "tx_user")
    var txUser: String = KeycloakSecurityContextHolder.getSubject() ?: "admin"

    @Column(name = "tx_host")
    var txHost: String = HttpUtil.getRequestHost() ?: "localhost"

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "industry_id", referencedColumnName = "industry_id", insertable = false, updatable = false)
    var industry: Industry? = null

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_entity_id", referencedColumnName = "business_entity_id", insertable = false, updatable = false)
    var businessEntity: BusinessEntity? = null

    @OneToMany(mappedBy = "company")
    var kcUserCompanies: List<KcUserCompany>? = null

    @OneToMany(mappedBy = "company")
    var exchangeRates: List<ExchangeRate>? = null

    @OneToMany(mappedBy = "company")
    var accountGroups: List<AccountGroup>? = null

    @OneToMany(mappedBy = "company")
    var accountSubgroups: List<AccountSubgroup>? = null

    @OneToMany(mappedBy = "company")
    var accounts: List<Account>? = null

    @OneToMany(mappedBy = "company")
    var subaccounts: List<Subaccount>? = null

    @OneToMany(mappedBy = "company")
    var subaccountTaxTypes: List<SubaccountTaxType>? = null

    @OneToMany(mappedBy = "company")
    var customers: List<Customer>? = null

    @OneToMany(mappedBy = "company")
    var saleTransactions: List<SaleTransaction>? = null

    @OneToMany(mappedBy = "company")
    var suppliers: List<Supplier>? = null

    @OneToMany(mappedBy = "company")
    var expenseTransactions: List<ExpenseTransaction>? = null

    @OneToMany(mappedBy = "company")
    var journalEntries: List<JournalEntry>? = null

    @OneToMany(mappedBy = "company")
    var attachments: List<Attachment>? = null

    @OneToMany(mappedBy = "company")
    var reports: List<Report>? = null
}
