package repository.security

import api.ApiServices
import base.EnviromentConfig
import base.RepositoryBase
import io.ktor.client.request.accept
import shared.ApplicationDispatcher
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.http.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import models.message.MessageResponse
import models.security.EnableLoginModel
import models.security.EnableLoginParameter

class SecurityRepository: RepositoryBase {

    var api = ApiServices().instance()

    /**
     * A group of *members*.
     *
     * This class has no useful logic; it's just a documentation example.
     *
     * @param T null.
     * @return vss y expiracion.
     */
    fun enableLogin(enviromen: EnviromentConfig, success: (EnableLoginModel) -> Unit, failure: (Throwable) -> Unit) {
        GlobalScope.launch(ApplicationDispatcher) {
            try {
                val jsonResponse = api.get<String>(urlString = enviromen.url() + API_SECURITY + ENABLELOGIN)
                val json = Json(JsonConfiguration.Stable.copy(strictMode = false))
                json.parse(EnableLoginModel.serializer(), jsonResponse)
                        .also(success)
            } catch (ex: Exception){
                failure(ex)
            }
        }
    }

    fun generateOTP( enviromen: EnviromentConfig, parameter: EnableLoginParameter, success: (MessageResponse) -> Unit, failure: (Throwable) -> Unit) {
        GlobalScope.launch(ApplicationDispatcher) {
            try {
                val json = Json(JsonConfiguration.Stable.copy(strictMode = false))
                val response = api.post<String>(urlString = enviromen.url() + API_SECURITY + GENERATEOTP) {
                    accept(ContentType.Application.Json)
                    contentType(ContentType.Application.Json)
                    body = json.stringify(EnableLoginParameter.serializer(), parameter)
                }
                if (response == "true") {
                    success( MessageResponse(
                        response = response,
                        message = "Mensaje enviado"
                    ))
                } else {
                    success( MessageResponse(
                        response = response,
                        message = "Mensaje no enviado")
                    )
                }
            } catch (ex: Exception){
                failure(ex)
            }
        }
    }

}