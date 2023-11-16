package ucb.accounting.backend.api

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ucb.accounting.backend.bl.SupplierBl
import ucb.accounting.backend.dto.SupplierDto
import ucb.accounting.backend.dto.SupplierPartialDto
import ucb.accounting.backend.dto.ResponseDto
import ucb.accounting.backend.util.ResponseCodeUtil

@RestController
@RequestMapping("/api/v1/suppliers")
class SupplierApi @Autowired constructor(private val supplierBl: SupplierBl){

    companion object {
        private val logger = LoggerFactory.getLogger(SupplierApi::class.java.name)
    }

    @PostMapping("/companies/{companyId}")
    fun createSupplier(@PathVariable companyId: Long,
                       @RequestBody supplierDto: SupplierDto
    ): ResponseEntity<ResponseDto<Nothing>> {
        logger.info("Starting the API call to create supplier")
        logger.info("POST /api/v1/suppliers/companies/$companyId")
        supplierBl.createSupplier(companyId, supplierDto)
        logger.info("Sending response")
        val code = "201-12"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, null), responseInfo.httpStatus)
    }

    @GetMapping("/companies/{companyId}")
    fun getSuppliers(
        @PathVariable companyId: Long,
        @RequestParam(defaultValue = "supplierId") sortBy: String,
        @RequestParam(defaultValue = "asc") sortType: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(required = false) keyword: String?,
        ): ResponseEntity<ResponseDto<List<SupplierPartialDto>>> {
        logger.info("Starting the API call to get suppliers")
        logger.info("GET /api/v1/suppliers/companies/$companyId")
        val suppliersPage: Page<SupplierPartialDto> = supplierBl.getSuppliers(companyId, sortBy, sortType, page, size, keyword)
        logger.info("Sending response")
        val code = "200-30"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, suppliersPage.content, suppliersPage.totalElements), responseInfo.httpStatus)
    }

    @GetMapping("/{supplierId}/companies/{companyId}")
    fun getSupplier(@PathVariable supplierId: Long, @PathVariable companyId: Long): ResponseEntity<ResponseDto<SupplierDto>> {
        logger.info("Starting the API call to get supplier")
        logger.info("GET /api/v1/suppliers/$supplierId/companies/$companyId")
        val supplier: SupplierDto = supplierBl.getSupplier(supplierId, companyId)
        logger.info("Sending response")
        val code = "200-30"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, supplier), responseInfo.httpStatus)
    }

    @PutMapping("/{supplierId}/companies/{companyId}")
    fun updateSupplier(@PathVariable supplierId: Long,
                       @PathVariable companyId: Long,
                       @RequestBody supplierDto: SupplierDto
    ): ResponseEntity<ResponseDto<SupplierDto>> {
        logger.info("Starting the API call to update supplier")
        logger.info("PUT /api/v1/suppliers/$supplierId/companies/$companyId")
        val supplier: SupplierDto = supplierBl.updateSupplier(supplierId, companyId, supplierDto)
        logger.info("Sending response")
        val code = "200-31"
        val responseInfo = ResponseCodeUtil.getResponseInfo(code)
        logger.info("Code: $code - ${responseInfo.message}")
        return ResponseEntity(ResponseDto(code, responseInfo.message!!, supplier), responseInfo.httpStatus)
    }
}