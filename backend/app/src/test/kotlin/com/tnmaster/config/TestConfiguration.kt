package com.tnmaster.config

import io.github.truenine.composeserver.oss.ObjectStorageService
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@TestConfiguration
class TestWebMvcConfiguration {

    @Bean
    @Primary
    fun testWebMvcConfigurer(): WebMvcConfigurer {
        return object : WebMvcConfigurer {
            // 空的配置器，不添加任何拦截器
        }
    }

    @Bean
    @Primary
    fun mockObjectStorageService(): ObjectStorageService {
        // 创建一个简单的 Mock 实现，只实现必要的方法
        return object : ObjectStorageService {
            override val exposedBaseUrl: String = "http://mock-storage.test"

            override suspend fun isHealthy(): Boolean = true

            override fun <T : Any> getNativeClient(): T? = null

            // 其他方法都抛出 UnsupportedOperationException，表示测试中不应该调用这些方法
            override suspend fun createBucket(request: io.github.truenine.composeserver.oss.CreateBucketRequest) =
                throw UnsupportedOperationException("Mock implementation")

            override suspend fun bucketExists(bucketName: String) =
                throw UnsupportedOperationException("Mock implementation")

            override suspend fun deleteBucket(bucketName: String) =
                throw UnsupportedOperationException("Mock implementation")

            override suspend fun listBuckets() =
                throw UnsupportedOperationException("Mock implementation")

            override suspend fun setBucketPublicRead(bucketName: String) =
                throw UnsupportedOperationException("Mock implementation")

            override suspend fun getBucketPolicy(bucketName: String) =
                throw UnsupportedOperationException("Mock implementation")

            override suspend fun setBucketPolicy(bucketName: String, policy: String) =
                throw UnsupportedOperationException("Mock implementation")

            override suspend fun putObject(request: io.github.truenine.composeserver.oss.PutObjectRequest) =
                throw UnsupportedOperationException("Mock implementation")

            override suspend fun putObject(
                bucketName: String,
                objectName: String,
                inputStream: java.io.InputStream,
                size: Long,
                contentType: String?,
                metadata: Map<String, String>
            ) = throw UnsupportedOperationException("Mock implementation")

            override suspend fun getObjectInfo(bucketName: String, objectName: String) =
                throw UnsupportedOperationException("Mock implementation")

            override suspend fun getObject(bucketName: String, objectName: String) =
                throw UnsupportedOperationException("Mock implementation")

            override suspend fun getObject(bucketName: String, objectName: String, offset: Long, length: Long) =
                throw UnsupportedOperationException("Mock implementation")

            override suspend fun objectExists(bucketName: String, objectName: String) =
                throw UnsupportedOperationException("Mock implementation")

            override suspend fun deleteObject(bucketName: String, objectName: String) =
                throw UnsupportedOperationException("Mock implementation")

            override suspend fun deleteObjects(bucketName: String, objectNames: List<String>) =
                throw UnsupportedOperationException("Mock implementation")

            override suspend fun copyObject(request: io.github.truenine.composeserver.oss.CopyObjectRequest) =
                throw UnsupportedOperationException("Mock implementation")

            override suspend fun listObjects(request: io.github.truenine.composeserver.oss.ListObjectsRequest) =
                throw UnsupportedOperationException("Mock implementation")

            override fun listObjectsFlow(request: io.github.truenine.composeserver.oss.ListObjectsRequest) =
                throw UnsupportedOperationException("Mock implementation")

            override suspend fun generatePresignedUrl(
                bucketName: String,
                objectName: String,
                expiration: java.time.Duration,
                method: io.github.truenine.composeserver.enums.HttpMethod
            ) = throw UnsupportedOperationException("Mock implementation")

            override suspend fun initiateMultipartUpload(request: io.github.truenine.composeserver.oss.InitiateMultipartUploadRequest) =
                throw UnsupportedOperationException("Mock implementation")

            override suspend fun uploadPart(request: io.github.truenine.composeserver.oss.UploadPartRequest) =
                throw UnsupportedOperationException("Mock implementation")

            override suspend fun completeMultipartUpload(request: io.github.truenine.composeserver.oss.CompleteMultipartUploadRequest) =
                throw UnsupportedOperationException("Mock implementation")

            override suspend fun abortMultipartUpload(uploadId: String, bucketName: String, objectName: String) =
                throw UnsupportedOperationException("Mock implementation")

            override suspend fun listParts(uploadId: String, bucketName: String, objectName: String) =
                throw UnsupportedOperationException("Mock implementation")

            override suspend fun generateShareLink(request: io.github.truenine.composeserver.oss.ShareLinkRequest) =
                throw UnsupportedOperationException("Mock implementation")

            override suspend fun uploadWithLink(request: io.github.truenine.composeserver.oss.UploadWithLinkRequest) =
                throw UnsupportedOperationException("Mock implementation")

            override suspend fun downloadFromShareLink(shareUrl: String, password: String?) =
                throw UnsupportedOperationException("Mock implementation")

            override suspend fun validateShareLink(shareUrl: String, password: String?) =
                throw UnsupportedOperationException("Mock implementation")

            override suspend fun revokeShareLink(shareUrl: String) =
                throw UnsupportedOperationException("Mock implementation")
        }
    }
}
