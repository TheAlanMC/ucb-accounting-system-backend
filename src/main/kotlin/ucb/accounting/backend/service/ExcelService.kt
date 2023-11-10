package ucb.accounting.backend.service

import org.apache.poi.ss.usermodel.*
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.stereotype.Service
import java.io.FileOutputStream
import java.io.ByteArrayOutputStream
import java.math.BigDecimal

class ExcelService(private val sheetName: String = "Sheet1") {

    private val workbook = XSSFWorkbook()
    private val sheet = workbook.createSheet(sheetName)
    private var rowNum = 0

    private val headerStyle: XSSFCellStyle = createHeaderStyle()

    private fun createHeaderStyle(): XSSFCellStyle {
        val style = workbook.createCellStyle() as XSSFCellStyle
        val font = workbook.createFont()
        font.bold = true
        font.color = IndexedColors.BLACK.index
        font.fontHeightInPoints = 14
        style.fillForegroundColor = IndexedColors.LEMON_CHIFFON.index
        style.fillPattern = FillPatternType.SOLID_FOREGROUND
        style.setFont(font)
        return style
    }

    fun addHeader(headers: List<String>) {
        val headerRow = sheet.createRow(rowNum++)
        headers.forEachIndexed { index, header ->
            val cell = headerRow.createCell(index)
            cell.setCellValue(header)
            cell.cellStyle = headerStyle // Apply header style
//            sheet.autoSizeColumn(index)
        }
    }


    private val rowStyle: XSSFCellStyle = createRowStyle()

    private fun createRowStyle(): XSSFCellStyle {
        val style = workbook.createCellStyle() as XSSFCellStyle
        val font = workbook.createFont()
        font.fontHeightInPoints = 12
        style.fillForegroundColor = IndexedColors.WHITE.index
        style.fillPattern = FillPatternType.SOLID_FOREGROUND
        style.alignment = HorizontalAlignment.RIGHT
        style.setFont(font)
        return style
    }

    private val customRowStyle: XSSFCellStyle = createCustomRowStyle()

    private fun createCustomRowStyle(): XSSFCellStyle {
        val style = workbook.createCellStyle() as XSSFCellStyle
        val font = workbook.createFont()
        font.fontHeightInPoints = 12
        style.fillForegroundColor = IndexedColors.WHITE.index
        style.fillPattern = FillPatternType.SOLID_FOREGROUND
        style.wrapText = true
        style.setFont(font)
        return style
    }

    private val currencyStyle: XSSFCellStyle = createCurrencyStyle()

    private fun createCurrencyStyle(): XSSFCellStyle {
        val style = workbook.createCellStyle() as XSSFCellStyle
        val font = workbook.createFont()
        font.fontHeightInPoints = 12
        style.fillForegroundColor = IndexedColors.WHITE.index
        style.fillPattern = FillPatternType.SOLID_FOREGROUND
        style.alignment = HorizontalAlignment.RIGHT
        style.dataFormat = workbook.createDataFormat().getFormat("#,##0.00")
        style.setFont(font)
        return style
    }

    fun addRow(data: List<Any>) {
        val row = sheet.createRow(rowNum++)
        data.forEachIndexed { index, field ->
            val cell = row.createCell(index)
            when (field) {
                is String -> {
                    cell.setCellValue(field)
                    if (index == 1)
                        cell.cellStyle = customRowStyle // Apply row style
                    else
                        cell.cellStyle = rowStyle // Apply row style
                }
                is Int -> {
                    cell.setCellValue(field.toDouble())
                    cell.cellStyle = rowStyle // Apply row style
                }
                is Double -> {
                    cell.setCellValue(field)
                    cell.cellStyle = rowStyle // Apply row style
                }
                is BigDecimal -> {
                    cell.setCellValue(field.toDouble())
                    cell.cellStyle = currencyStyle // Apply currency style
                }
            }
//            sheet.autoSizeColumn(index)
        }
    }


    fun autoSizeColumns() {
        for (columnIndex in 0..sheet.getRow(0).lastCellNum) {
            sheet.autoSizeColumn(columnIndex)
        }
    }


    fun toByteArray(): ByteArray {
        return try {
            this.autoSizeColumns()
            val outputStream = ByteArrayOutputStream()
            workbook.write(outputStream)
            workbook.close()
            outputStream.toByteArray()
        } catch (e: Exception) {
            e.printStackTrace()
            byteArrayOf()
        }
    }

    fun save(filePath: String) {
        try {
            this.autoSizeColumns()
            FileOutputStream(filePath).use { fileOut ->
                workbook.write(fileOut)
                workbook.close()
            }
            println("Excel file created successfully at: $filePath")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
