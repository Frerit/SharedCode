package api

import io.ktor.client.HttpClient
import io.ktor.client.features.*
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.request

class ApiServices {

    private val api = HttpClient() {

        defaultRequest {
            header("siteId", "exito")
         }

        HttpResponseValidator {
            validateResponse { response ->
                val statusCode = response.status.value
                print("Status: $statusCode :::")
                when (statusCode) {
                    in 201..399 -> throw NoContentResponseException(response)
                    in 400..401 -> throw RefresTokenException(response)
                    in 402..499 -> throw ClientRequestException(response)
                    in 500..599 -> throw ServerResponseException(response)
                }

                if (statusCode >= 600) {
                    throw ResponseException(response)
                }
            }
        }
        expectSuccess = true
    }

    fun instance(): HttpClient {
        return api
    }

}

class NoContentResponseException(response: HttpResponse) : ResponseException(response) {
    override val message: String? = "Client: ${response.call.request.url}. ${response.status}"
}

class RefresTokenException(response: HttpResponse) : ResponseException(response) {
    override val message: String? = "Token ha expirado :${response.request.headers}"
}
