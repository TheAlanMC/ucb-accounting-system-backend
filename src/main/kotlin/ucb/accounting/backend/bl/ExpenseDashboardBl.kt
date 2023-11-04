package ucb.accounting.backend.bl

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ucb.accounting.backend.dao.repository.CompanyRepository
import ucb.accounting.backend.dao.repository.ExpenseTransactionRepository
import ucb.accounting.backend.dao.repository.KcUserCompanyRepository
import ucb.accounting.backend.dto.ExpenseDashboardDto
import ucb.accounting.backend.dto.ExpensesDto
import ucb.accounting.backend.exception.UasException
import ucb.accounting.backend.util.KeycloakSecurityContextHolder
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*

@Service
class ExpenseDashboardBl @Autowired constructor(
    private val expenseTransactionRepository: ExpenseTransactionRepository,
    private val companyRepository: CompanyRepository,
    private val kcUserCompanyRepository: KcUserCompanyRepository
){

    companion object {
        private val logger = LoggerFactory.getLogger(ExpenseDashboardBl::class.java.name)
    }

    fun getExpenseBySupplier(companyId: Long, dateFrom: String, dateTo: String): ExpenseDashboardDto {
        // Validation of user belongs to company
//        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
//        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId)
//            ?: throw UasException("403-22")
//        logger.info("User $kcUuid is trying to get journal book report from company $companyId")

        // Convert dateFrom and dateTo to Date
        val format: java.text.DateFormat = SimpleDateFormat("yyyy-MM-dd")
        val newDateFrom: Date = format.parse(dateFrom)
        val newDateTo: Date = format.parse(dateTo)

        // Validation of dateFrom and dateTo
        if (newDateFrom.after(newDateTo)) {
            throw UasException("400-15")
        }
        val supplierTotalList = expenseTransactionRepository.countExpensesBySupplier(companyId.toInt(), newDateFrom, newDateTo)
        val expenseSupplier: List<ExpensesDto> = supplierTotalList.map {
            ExpensesDto(it["name"].toString(), BigDecimal.valueOf(it["total"].toString().toDouble()))
        }
        return ExpenseDashboardDto(expenseSupplier)
    }

    fun getExpenseBySubaccount(companyId: Long, dateFrom: String, dateTo: String): ExpenseDashboardDto {
//        // Validation of user belongs to company
//        val kcUuid = KeycloakSecurityContextHolder.getSubject()!!
//        kcUserCompanyRepository.findAllByKcUser_KcUuidAndCompany_CompanyIdAndStatusIsTrue(kcUuid, companyId)
//            ?: throw UasException("403-22")
//        logger.info("User $kcUuid is trying to get journal book report from company $companyId")

        // Convert dateFrom and dateTo to Date
        val format: java.text.DateFormat = SimpleDateFormat("yyyy-MM-dd")
        val newDateFrom: Date = format.parse(dateFrom)
        val newDateTo: Date = format.parse(dateTo)

        // Validation of dateFrom and dateTo
        if (newDateFrom.after(newDateTo)) {
            throw UasException("400-15")
        }
        val subaccountTotalList = expenseTransactionRepository.countExpensesBySubaccount(companyId.toInt(), newDateFrom, newDateTo)
        val expenseSubaccount: List<ExpensesDto> = subaccountTotalList.map {
            ExpensesDto(it["name"].toString(), BigDecimal.valueOf(it["total"].toString().toDouble()))
        }
        return ExpenseDashboardDto(expenseSubaccount)
    }

}