package feature

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.features.HttpClientFeature
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.request
import io.ktor.client.request.takeFrom
import io.ktor.client.statement.HttpReceivePipeline
import io.ktor.client.statement.HttpResponse
import io.ktor.util.AttributeKey

typealias NeedRetryHandler = suspend (response: HttpResponse) -> Boolean

class NeedRetry(
        private val retryHandlers: List<NeedRetryHandler>
) {
    class Config {
        internal val retryHandlers: MutableList<NeedRetryHandler> = mutableListOf()

        fun needRetryHandler(block: NeedRetryHandler) {
            retryHandlers += block
        }
    }

    companion object : HttpClientFeature<Config, NeedRetry> {
        override val key: AttributeKey<NeedRetry> = AttributeKey("NeedRetry")

        override fun prepare(block: Config.() -> Unit): NeedRetry {
            val config = Config().apply(block)

            config.retryHandlers.reversed()

            return NeedRetry(config.retryHandlers)
        }

        override fun install(feature: NeedRetry, scope: HttpClient) {
            scope.receivePipeline.intercept(HttpReceivePipeline.After) {
                try {
                    val isRetryNeeded = feature.retryHandlers.map { it(context.response) }.contains(true)

                    if (isRetryNeeded) {
                        context.client.request<HttpResponse>(HttpRequestBuilder().takeFrom(context.request))
                    }

                    proceedWith(it)
                } catch (cause: Throwable) {
                    throw cause
                }
            }
        }
    }
}

fun HttpClientConfig<*>.NeedRetryHandler(block: NeedRetry.Config.() -> Unit) {
    install(NeedRetry, block)
}