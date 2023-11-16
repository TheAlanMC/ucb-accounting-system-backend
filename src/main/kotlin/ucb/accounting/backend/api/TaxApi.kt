package ucb.accounting.backend.api

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ucb.accounting.backend.bl.TaxBl
import ucb.accounting.backend.dto.ResponseDto
import ucb.accounting.backend.dto.SubaccountTaxTypeDto
import ucb.accounting.backend.dto.SubaccountTaxTypePartialDto
import ucb.accounting.backend.dto.TaxTypeDto
import ucb.accounting.backend.util.ResponseCodeUtil

@RestController
@RequestMapping("/api/v1/taxes")
class TaxApi @Autowired constructor(private val taxBl: TaxBl) {

    private val logger = LoggerFactory.getLogger(TaxApi::class.java.name)

    @GetMapping("/tax-types")
    fun getTaxTypes(): ResponseEntity<ResponseDto<List<TaxTypeDto>>> {
        logger.info("Starting the API call to get tax types")
        logger.info("GET /api/v1/taxes/tax-types")
        val taxTypes: List<TaxTypeDto> = taxBl.getTaxTypes()
        logger.info("Sending response")
        val code = "200-35"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, taxTypes), responseInfo.httpStatus)
    }

    @PostMapping("companies/{companyId}")
    fun createSubaccountTaxType(
        @PathVariable("companyId") companyId: Long,
        @RequestBody subaccountTaxTypeDto: SubaccountTaxTypeDto
    ): ResponseEntity<ResponseDto<TaxTypeDto>> {
        logger.info("Starting the API call to create subaccount associated with tax type")
        logger.info("POST /api/v1/taxes/companies/$companyId")
        taxBl.createSubaccountTaxType(companyId, subaccountTaxTypeDto)
        val code = "201-14"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, null), responseInfo.httpStatus)
    }

    @GetMapping("/companies/{companyId}")
    fun getSubaccountTaxTypes(
        @PathVariable("companyId") companyId: Long
    ): ResponseEntity<ResponseDto<List<SubaccountTaxTypePartialDto>>> {
        logger.info("Starting the API call to get all subaccount associated with tax type")
        logger.info("GET /api/v1/taxes/companies/$companyId")
        val subaccountTaxTypes: List<SubaccountTaxTypePartialDto> = taxBl.getSubaccountTaxTypes(companyId)
        logger.info("Sending response")
        val code = "200-36"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, subaccountTaxTypes), responseInfo.httpStatus)
    }

    @PutMapping("/companies/{companyId}")
    fun updateSubaccountTaxTypeRate(
        @PathVariable("companyId") companyId: Long,
        @RequestBody subaccountTaxTypeDto: SubaccountTaxTypeDto
    ): ResponseEntity<ResponseDto<TaxTypeDto>> {
        logger.info("Starting the API call to update subaccount associated with tax type")
        logger.info("PUT /api/v1/taxes/companies/$companyId")
        taxBl.updateSubaccountTaxTypeRate(companyId, subaccountTaxTypeDto)
        val code = "200-45"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, null), responseInfo.httpStatus)
    }

    @GetMapping("sales/companies/{companyId}")
    fun getSaleTaxTypes(
        @PathVariable("companyId") companyId: Long,
    ): ResponseEntity<ResponseDto<List<TaxTypeDto>>> {
        logger.info("Starting the API call to get sales tax")
        logger.info("GET /api/v1/taxes/sales")
        val taxTypes: List<TaxTypeDto> = taxBl.getSaleTaxType(companyId)
        logger.info("Sending response")
        val code = "200-46"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, taxTypes), responseInfo.httpStatus)
    }

    @GetMapping("expenses/companies/{companyId}")
    fun getExpenseTaxTypes(
        @PathVariable("companyId") companyId: Long,
    ): ResponseEntity<ResponseDto<List<TaxTypeDto>>> {
        logger.info("Starting the API call to get expense tax")
        logger.info("GET /api/v1/taxes/expenses")
        val taxTypes: List<TaxTypeDto> = taxBl.getExpenseTaxType(companyId)
        logger.info("Sending response")
        val code = "200-47"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, taxTypes), responseInfo.httpStatus)
    }
}
