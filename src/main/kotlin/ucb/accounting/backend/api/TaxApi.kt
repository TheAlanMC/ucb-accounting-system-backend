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

}
