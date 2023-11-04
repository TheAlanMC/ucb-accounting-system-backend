package ucb.accounting.backend.api

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ucb.accounting.backend.bl.ReportBl
import ucb.accounting.backend.dto.*
import ucb.accounting.backend.util.ResponseCodeUtil

@RestController
@RequestMapping("/api/v1/reports")
class ReportApi  @Autowired constructor(private val reportBl: ReportBl) {
    companion object {
        private val logger = LoggerFactory.getLogger(ReportApi::class.java.name)
    }

    @GetMapping("/report-types")
    fun getReportTypes(): ResponseEntity<ResponseDto<List<ReportTypeDto>>> {
        logger.info("Starting the API call to get report types")
        logger.info("GET /api/v1/reports/report-types")
        val reportTypes: List<ReportTypeDto> = reportBl.getReportTypes()
        logger.info("Sending response")
        val code = "200-14"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, reportTypes), responseInfo.httpStatus)
    }

    @GetMapping("/journal-books/companies/{companyId}")
    fun getJournalEntries(
        @PathVariable("companyId") companyId: Long,
        @RequestParam(required = true) dateFrom: String,
        @RequestParam(required = true) dateTo: String,
    ): ResponseEntity<ResponseDto<ReportDto<List<JournalBookReportDto>>>> {
        logger.info("Starting the API call to get journal entries")
        val journalBook: ReportDto<List<JournalBookReportDto>> = reportBl.getJournalBook(companyId, dateFrom, dateTo)
        logger.info("Sending response")
        val code = "200-22"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Finishing the API call to get journal entries")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, journalBook), responseInfo.httpStatus)
    }

    @GetMapping("/general-ledgers/companies/{companyId}/subaccounts")
    fun getAvailableSubaccounts(
        @PathVariable("companyId") companyId: Long,
        @RequestParam(required = true) dateFrom: String,
        @RequestParam(required = true) dateTo: String,
    ): ResponseEntity<ResponseDto<List<SubaccountDto>>> {
        logger.info("Starting the API call to get available subaccounts")
        logger.info("GET /api/v1/reports/general-ledgers/companies/$companyId/subaccounts")
        val subaccounts: List<SubaccountDto> = reportBl.getAvailableSubaccounts(companyId, dateFrom, dateTo)
        logger.info("Sending response")
        val code = "200-22"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, subaccounts), responseInfo.httpStatus)
    }


    @GetMapping("/general-ledgers/companies/{companyId}")
    fun getGeneralLedgers(
        @PathVariable("companyId") companyId: Long,
        @RequestParam(required = true) dateFrom: String,
        @RequestParam(required = true) dateTo: String,
        @RequestParam(required = true) subaccountIds: List<String>
    ): ResponseEntity<ResponseDto<ReportDto<List<GeneralLedgerReportDto>>>> {
        logger.info("Starting the API call to get journal book report")
        logger.info("GET /api/v1/reports/general-ledgers/companies/$companyId")
        val journalBook: ReportDto<List<GeneralLedgerReportDto>> = reportBl.getGeneralLedger(companyId, dateFrom, dateTo, subaccountIds)
        logger.info("Sending response")
        val code = "200-23"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, journalBook ), responseInfo.httpStatus)
    }

    @GetMapping("/trial-balances/companies/{companyId}")
    fun getTrialBalances(
        @PathVariable("companyId") companyId: Long,
        @RequestParam(required = true) dateFrom: String,
        @RequestParam(required = true) dateTo: String,
    ): ResponseEntity<ResponseDto<ReportDto<List<TrialBalanceReportDto>>>> {
        logger.info("Starting the API call to get trial balance report")
        logger.info("GET /api/v1/reports/trial-balances/companies/$companyId")
        val trialBalance: ReportDto<List<TrialBalanceReportDto>> = reportBl.getTrialBalance(companyId, dateFrom, dateTo)
        logger.info("Sending response")
        val code = "200-24"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, trialBalance), responseInfo.httpStatus)
    }


    @GetMapping("/worksheets/companies/{companyId}")
    fun getWorksheet(
        @PathVariable("companyId") companyId: Long,
        @RequestParam(required = true) dateFrom: String,
        @RequestParam(required = true) dateTo: String,
    ): ResponseEntity<ResponseDto<ReportDto<WorksheetReportDto>>> {
        logger.info("Starting the API call to get worksheet report")
        logger.info("GET /api/v1/reports/worksheet/companies/$companyId")
        val worksheet: ReportDto<WorksheetReportDto> = reportBl.getWorksheet(companyId, dateFrom, dateTo)
        logger.info("Sending response")
        val code = "200-25"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, worksheet), responseInfo.httpStatus)
    }

    @GetMapping("/income-statements/companies/{companyId}")
    fun getIncomeStatements(
        @PathVariable("companyId") companyId: Long,
        @RequestParam(required = true) dateFrom: String,
        @RequestParam(required = true) dateTo: String,
    ): ResponseEntity<ResponseDto<ReportDto<FinancialStatementReportDto>>> {
        logger.info("Starting the API call to get income statement report")
        logger.info("GET /api/v1/reports/income-statements/companies/$companyId")
        val incomeStatement: ReportDto<FinancialStatementReportDto> = reportBl.getIncomeStatement(companyId, dateFrom, dateTo)
        logger.info("Sending response")
        val code = "200-26"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, incomeStatement), responseInfo.httpStatus)
    }

//    @GetMapping("/balance-sheets/companies/{companyId}")
//    fun getBalanceSheets(
//        @PathVariable("companyId") companyId: Long,
//        @RequestParam(required = true) dateFrom: String,
//        @RequestParam(required = true) dateTo: String,
//    ): ResponseEntity<ResponseDto<ReportDto<FinancialStatementReportDto>>> {
//        logger.info("Starting the API call to get balance sheet report")
//        logger.info("GET /api/v1/reports/balance-sheets/companies/$companyId")
//        val balanceSheet: ReportDto<FinancialStatementReportDto> = reportBl.getBalanceSheet(companyId, dateFrom, dateTo)
//        logger.info("Sending response")
//        val code = "200-27"
//        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
//        logger.info("Code: $code - ${responseInfo.message}")
//        return ResponseEntity(ResponseDto(code, responseInfo.message!!, balanceSheet), responseInfo.httpStatus)
//    }
}
