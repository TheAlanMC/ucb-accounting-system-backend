package ucb.accounting.backend.mapper

import ucb.accounting.backend.dao.Supplier
import ucb.accounting.backend.dto.SupplierDto

class SupplierMapper {

    companion object{
        fun entityToDto(supplier: Supplier): SupplierDto {
            return SupplierDto(
                supplierId = supplier.supplierId,
                prefix = supplier.prefix,
                displayName = supplier.displayName,
                firstName = supplier.firstName,
                lastName = supplier.lastName,
                companyName = supplier.companyName,
                companyEmail = supplier.companyEmail,
                companyPhoneNumber = supplier.companyPhoneNumber,
                companyAddress = supplier.companyAddress
            )
        }
    }
}