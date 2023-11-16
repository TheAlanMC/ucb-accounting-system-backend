package ucb.accounting.backend.dao.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import ucb.accounting.backend.dao.FinancialStatement
import java.util.*

interface FinancialStatementRepository: JpaRepository<FinancialStatement, Long> {

    @Query(value =
    """
          SELECT  DISTINCT ON (s.subaccount_id)
                    ROW_NUMBER() OVER (ORDER BY s.subaccount_id) AS id,
                    ac.account_category_id As account_category_id,
                    ac.account_category_code As account_category_code,
                    ac.account_category_name As account_category_name,
                    ag.account_group_id as account_group_id,
                    ag.account_group_code as account_group_code,
                    ag.account_group_name as account_group_name,
                    asg.account_subgroup_id as account_subgroup_id,
                    asg.account_subgroup_code as account_subgroup_code,
                    asg.account_subgroup_name as account_subgroup_name,
                    a.account_id as account_id,
                    a.account_code as account_code,
                    a.account_name as account_name,
                    s.subaccount_code as subaccount_code,
                    s.subaccount_id as subaccount_id,
                    s.subaccount_code as subaccount_code,
                    s.subaccount_name as subaccount_name,
                    SUM(td.debit_amount_bs) AS debit_amount_bs,
                    SUM(td.credit_amount_bs) AS credit_amount_bs
            FROM subaccount s
            JOIN transaction_detail td ON s.subaccount_id = td.subaccount_id
            JOIN transaction t ON td.transaction_id = t.transaction_id
            JOIN journal_entry je ON t.journal_entry_id = je.journal_entry_id
            JOIN account a ON s.account_id = a.account_id
            JOIN account_subgroup asg ON a.account_subgroup_id = asg.account_subgroup_id
            JOIN account_group ag ON asg.account_group_id = ag.account_group_id
            JOIN account_category ac ON ag.account_category_id = ac.account_category_id
            AND je.journal_entry_accepted = TRUE
            AND je.status = TRUE
            AND je.company_id = :companyId
            AND ac.account_category_name IN (:accountCategoryNames)
            AND (t.transaction_date BETWEEN :dateFrom AND :dateTo OR CAST(:dateFrom AS DATE) IS NULL OR CAST(:dateTo AS DATE) IS NULL)
            GROUP BY s.subaccount_id, ac.account_category_id, ac.account_category_code, ac.account_category_name, ag.account_group_id, ag.account_group_code, ag.account_group_name, asg.account_subgroup_id, asg.account_subgroup_code, asg.account_subgroup_name, a.account_id, a.account_code, a.account_name, s.subaccount_id, s.subaccount_code, s.subaccount_name
            ORDER BY s.subaccount_id, ac.account_category_id, ag.account_group_id, asg.account_subgroup_id, a.account_id, s.subaccount_id
    """, nativeQuery = true)
    fun findAllByCompanyIdAndStatusIsTrue (
        @Param("companyId") companyId: Int,
        @Param("dateFrom") dateFrom: Date?,
        @Param("dateTo") dateTo: Date?,
        @Param("accountCategoryNames") accountCategoryNames: List<String>
    ): List<FinancialStatement>
}