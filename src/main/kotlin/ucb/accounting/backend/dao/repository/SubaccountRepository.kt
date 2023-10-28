package ucb.accounting.backend.dao.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import ucb.accounting.backend.dao.Subaccount
import java.util.*

interface SubaccountRepository: JpaRepository<Subaccount, Long>{
    fun findAllByCompanyIdAndStatusIsTrue(companyId: Int): List<Subaccount>

    fun findBySubaccountIdAndStatusIsTrue(subaccountId: Long): Subaccount?

    fun findAllByCompanyIdAndAccountIdAndStatusIsTrue(companyId: Int, accountId: Int): List<Subaccount>

    fun findAllByAccountAccountSubgroupAccountGroupAccountCategoryAccountCategoryNameAndCompanyIdAndStatusIsTrueOrderBySubaccountIdAsc(accountCategoryName: String, companyId: Int): List<Subaccount>
    fun findByCompanyId(companyId: Long): List<Subaccount>

    fun findFirstByCompanyIdAndSubaccountNameAndStatusIsTrue(companyId: Int, subaccountName: String): Subaccount?
    fun findAllByCompanyIdAndAccountIdAndStatusIsTrueOrderBySubaccountIdAsc(companyId: Int, toInt: Int): List<Subaccount>
    fun findFirstByAccountIdAndCompanyIdAndStatusIsTrueOrderBySubaccountCodeDesc(toInt: Int, toInt1: Int): Subaccount?

    fun findAllByAccountAccountSubgroupAccountSubgroupNameAndCompanyIdAndStatusIsTrueOrderBySubaccountIdAsc(accountSubgroupName: String, companyId: Int): List<Subaccount>
    fun findAllByCompanyIdAndStatusIsTrueOrderBySubaccountIdAsc(toInt: Int): List<Subaccount>

    @Query(value = "SELECT " +
            "t.transaction_date AS fecha, " +
            "s.subaccount_code AS codigoDeCuenta, " +
            "s.subaccount_name AS nombreDeCuenta, " +
            "t.description AS descripcion, " +
            "td.debit_amount_bs AS debe, " +
            "td.credit_amount_bs AS haber, " +
            "SUM(td.debit_amount_bs - td.credit_amount_bs) OVER (PARTITION BY s.subaccount_id ORDER BY t.transaction_date) AS saldo " +
            "FROM subaccount s " +
            "INNER JOIN transaction_detail td ON s.subaccount_id = td.subaccount_id " +
            "INNER JOIN transaction t ON td.transaction_id = t.transaction_id " +
            "WHERE s.company_id = :companyId " +
            "AND s.status = true " +
            "AND t.status = true " +
            "AND td.status = true " +
            "AND t.transaction_date BETWEEN :startDate AND :endDate " +
            "AND s.subaccount_id IN :subaccountIds " +
            "ORDER BY t.transaction_date ASC"
        , nativeQuery = true)
    fun getJournalBookData(
        @Param("companyId") companyId: Int,
        @Param("startDate") startDate: Date,
        @Param("endDate") endDate: Date,
        @Param("subaccountIds") subaccountIds: List<Int>
    ): List<Map<String, Any>>

}
