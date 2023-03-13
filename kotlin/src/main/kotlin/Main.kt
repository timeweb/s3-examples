import aws.sdk.kotlin.runtime.auth.credentials.StaticCredentialsProvider
import aws.sdk.kotlin.runtime.endpoint.AwsEndpoint
import aws.sdk.kotlin.runtime.endpoint.StaticEndpointResolver
import aws.sdk.kotlin.services.s3.*
import aws.smithy.kotlin.runtime.auth.awscredentials.Credentials
import aws.smithy.kotlin.runtime.content.asByteStream
import aws.smithy.kotlin.runtime.http.Url
import aws.smithy.kotlin.runtime.http.endpoints.Endpoint
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import java.io.File
import kotlin.system.exitProcess

private val logger = LoggerFactory.getLogger("S3_Example")
fun main() {
    runBlocking {
        val client = S3Client.fromEnvironment {
            this.region = "ru-1"
            this.endpointResolver = StaticEndpointResolver(
                AwsEndpoint(
                    endpoint = Endpoint(
                        uri = Url.parse("https://s3.timeweb.com")
                    )
                )
            )
            this.credentialsProvider = StaticCredentialsProvider(
                credentials = Credentials(
                    accessKeyId = Consts.AccessKey,
                    secretAccessKey = Consts.SecretKey
                )
            )
        }

        logger.debug("Create bucket")
        runCatching {
            val response =
                client.createBucket { this.bucket = Consts.BucketName }
            logger.debug(response.toString())
        }.onFailure {
            logger.error(it.message)
        }

        logger.debug("Get bucket metadata")
        runCatching {
            val response = client.headBucket { this.bucket = Consts.BucketName }
            logger.debug(response.toString())
        }.onFailure {
            logger.error(it.message)
        }

        logger.debug("Get bucket location")
        runCatching {
            val response = client.headBucket { this.bucket = Consts.BucketName }
            logger.debug(response.toString())
        }.onFailure {
            logger.error(it.message)
        }

        logger.debug("Get list of buckets")
        runCatching {
            val response = client.listBuckets()
            logger.debug(response.toString())
        }.onFailure {
            logger.error(it.message)
        }

        logger.debug("Upload file to bucket")
        runCatching {
            val file = File("kotlin/test.txt")
            val response = client.uploadPart {
                this.bucket = Consts.BucketName
                this.key = file.name
                this.body = file.asByteStream()
            }
            logger.debug(response.toString())
        }.onFailure {
            logger.error(it.message)
        }

        logger.debug("Get list of bucket objects")
        runCatching {
            val response =
                client.listObjects { this.bucket = Consts.BucketName }
            logger.debug(response.toString())
        }.onFailure {
            logger.error(it.message)
        }

    }
    exitProcess(1)
}
