package ucb.accounting.backend.bl

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ucb.accounting.backend.dao.repository.SaleTransactionRepository
import ucb.accounting.backend.dto.SaleDashboardDto
import ucb.accounting.backend.dto.SalesDto
import ucb.accounting.backend.exception.UasException
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*

@Service
class SaleDashboardBl @Autowired constructor(
    private val saleTransactionRepository: SaleTransactionRepository
){

    companion object {
        private val logger = LoggerFactory.getLogger(SaleDashboardBl::class.java.name)
    }

    fun getSaleByClient(companyId: Long, dateFrom: String, dateTo: String): SaleDashboardDto {
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
        val clientTotalList = saleTransactionRepository.countSalesByClients(companyId.toInt(), newDateFrom, newDateTo)
        val saleClient: List<SalesDto> = clientTotalList.map {
            SalesDto(it["name"].toString(), BigDecimal.valueOf(it["total"].toString().toDouble()))
        }
        return SaleDashboardDto(saleClient)
    }

    fun getSaleBySubaccount(companyId: Long, dateFrom: String, dateTo: String): SaleDashboardDto {
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
        val subaccountTotalList = saleTransactionRepository.countSalesBySubaccounts(companyId.toInt(), newDateFrom, newDateTo)
        val saleSubaccount: List<SalesDto> = subaccountTotalList.map {
            SalesDto(it["name"].toString(), BigDecimal.valueOf(it["total"].toString().toDouble()))
        }
        return SaleDashboardDto(saleSubaccount)
    }

}