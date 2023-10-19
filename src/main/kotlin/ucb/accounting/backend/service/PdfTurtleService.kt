package ucb.accounting.backend.service

import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import ucb.accounting.backend.dto.pdf_turtle.ReportOptions

@Service
class PdfTurtleService {

    @Value("\${pdf-turtle.url}")
    private lateinit var pdfTurtleUrl: String

    private val client = OkHttpClient()
    private val objectMapper = ObjectMapper()

    fun generatePdf(
        footerHtmlTemplate: String,
        headerHtmlTemplate: String,
        htmlTemplate: String,
        model: Map<String, Any>,
        options: ReportOptions,
        templateEngine: String
    ): ByteArray {
       val jsonRequest = generateJsonRequest(
              footerHtmlTemplate,
              headerHtmlTemplate,
              htmlTemplate,
              model,
              options,
              templateEngine
         )

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = jsonRequest.toRequestBody(mediaType)

        val request = Request.Builder()
            .url("$pdfTurtleUrl/api/pdf/from/html-template/render")
            .post(requestBody)
            .build()

        val response = client.newCall(request).execute()

        return response.body!!.bytes()
    }

    private fun generateJsonRequest(
        footerHtmlTemplate: String,
        headerHtmlTemplate: String,
        htmlTemplate: String,
        model: Map<String, Any>,
        options: ReportOptions,
        templateEngine: String
    ):String {
        val requestMap = mapOf(
            "footerHtmlTemplate" to footerHtmlTemplate,
            "headerHtmlTemplate" to headerHtmlTemplate,
            "htmlTemplate" to htmlTemplate,
            "model" to model,
            "options" to options,
            "templateEngine" to templateEngine
        )
        return objectMapper.writeValueAsString(requestMap)
    }
}