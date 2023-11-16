package ucb.accounting.backend.dao.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.data.repository.query.Param
import ucb.accounting.backend.dao.*
import java.util.Date

interface AccountPlanRepository: JpaRepository<AccountPlan, Long> {

    @Query(value =
    """
          SELECT  
                    ROW_NUMBER() OVER (ORDER BY s.subaccount_id, ac.account_category_id, ag.account_group_id, ASg.account_subgroup_id, a.account_id, s.subaccount_id) AS id,
                    ac.account_category_id AS account_category_id,
                    ac.account_category_code AS account_category_code,
                    ac.account_category_name AS account_category_name,
                    ag.account_group_id AS account_group_id,
                    ag.account_group_code AS account_group_code,
                    ag.account_group_name AS account_group_name,
                    asg.account_subgroup_id AS account_subgroup_id,
                    asg.account_subgroup_code AS account_subgroup_code,
                    asg.account_subgroup_name AS account_subgroup_name,
                    a.account_id AS account_id,
                    a.account_code AS account_code,
                    a.account_name AS account_name,
                    s.subaccount_id AS subaccount_id,
                    s.subaccount_code AS subaccount_code,
                    s.subaccount_name AS subaccount_name
            FROM account_category ac
            JOIN account_group ag ON ac.account_category_id = ag.account_category_id
            JOIN account_subgroup asg ON ag.account_group_id = asg.account_group_id
            JOIN account a ON asg.account_subgroup_id = a.account_subgroup_id
            JOIN subaccount s ON a.account_id = s.account_id
            WHERE ac.status = TRUE
            AND ag.status = TRUE
            AND asg.status = TRUE
            AND a.status = TRUE
            AND s.status = TRUE
            AND ac.account_category_name IN (:accountCategoryNames)
            AND ag.company_id = :companyId
            AND asg.company_id = :companyId
            AND a.company_id = :companyId
            AND s.company_id = :companyId
            GROUP BY ac.account_category_id, ac.account_category_code, ac.account_category_name, ag.account_group_id, ag.account_group_code, ag.account_group_name, ASg.account_subgroup_id, ASg.account_subgroup_code, ASg.account_subgroup_name, a.account_id, a.account_code, a.account_name, s.subaccount_id, s.subaccount_code, s.subaccount_name
            ORDER BY ac.account_category_id, ag.account_group_id, ASg.account_subgroup_id, a.account_id, s.subaccount_id
    """, nativeQuery = true)
    fun findAllByCompanyIdAndStatusIsTrue (
        @Param("companyId") companyId: Int,
        @Param("accountCategoryNames") accountCategoryNames: List<String>
    ): List<AccountPlan>
}