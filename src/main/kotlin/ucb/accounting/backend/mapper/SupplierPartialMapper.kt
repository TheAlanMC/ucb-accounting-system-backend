package ucb.accounting.backend.mapper

import ucb.accounting.backend.dao.Supplier
import ucb.accounting.backend.dto.SupplierPartialDto
import java.sql.Date

class SupplierPartialMapper {

    companion object{
        fun entityToDto(supplier: Supplier): SupplierPartialDto {
            return SupplierPartialDto(
                supplierId = supplier.supplierId,
                displayName = supplier.displayName,
                companyName = supplier.companyName,
                companyPhoneNumber = supplier.companyPhoneNumber,
                creationDate = Date(supplier.txDate.time)
            )
        }
    }
}