package ucb.accounting.backend.config

import io.minio.MinioClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration
class MinioClientConfig {
    @Value("\${minio.access.key}")
    private lateinit var minioAccessKey: String

    @Value("\${minio.access.secret}")
    private lateinit var minioAccessSecret: String

    @Value("\${minio.url}")
    private lateinit var minioUrl: String

    @Bean
    @Primary
    fun minioClient(): MinioClient {
        return MinioClient.Builder()
            .endpoint(minioUrl)
            .credentials(minioAccessKey, minioAccessSecret)
            .build()
    }
}