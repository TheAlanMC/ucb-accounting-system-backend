package ucb.accounting.backend.bl

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ucb.accounting.backend.dao.repository.SaleTransactionRepository
import ucb.accounting.backend.dto.SaleDashboardDto
import ucb.accounting.backend.dto.SalesDto
import java.math.BigDecimal

@Service
class SaleDashboardBl @Autowired constructor(
    private val saleTransactionRepository: SaleTransactionRepository
){

    companion object {
        private val logger = LoggerFactory.getLogger(SaleDashboardBl::class.java.name)
    }

    fun getSaleByClient(companyId: Long): SaleDashboardDto {
        val clientTotalList = saleTransactionRepository.countSalesByClients(companyId.toInt())
        val saleClient: List<SalesDto> = clientTotalList.map {
            SalesDto(it["name"].toString(), BigDecimal.valueOf(it["total"].toString().toDouble()))
        }
        return SaleDashboardDto(saleClient)
    }

    fun getSaleBySubaccount(companyId: Long): SaleDashboardDto {
        val subaccountTotalList = saleTransactionRepository.countSalesBySubaccounts(companyId.toInt())
        val saleSubaccount: List<SalesDto> = subaccountTotalList.map {
            SalesDto(it["name"].toString(), BigDecimal.valueOf(it["total"].toString().toDouble()))
        }
        return SaleDashboardDto(saleSubaccount)
    }

}