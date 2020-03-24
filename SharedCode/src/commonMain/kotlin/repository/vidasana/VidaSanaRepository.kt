package repository.vidasana

import api.ApiServices
import base.RepositoryBase
import shared.ApplicationDispatcher
import io.ktor.client.request.post
import io.ktor.client.request.url
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import models.ValidateUserDiscountParameter

class VidaSanaRepository : RepositoryBase {

    var api = ApiServices().instance();

    fun validateUserInfo(parameters: ValidateUserDiscountParameter, success: (Unit) -> Unit, failure: (Throwable) -> Unit) {
        val json = io.ktor.client.features.json.defaultSerializer()

        GlobalScope.launch(ApplicationDispatcher) {
            try {
                val baseURL = "https://appmicompaniadevgrupoexito.azure-api.net/Dllo-"
                val urlbase =  baseURL+"UserInfo/api/v1/validateUserInfo"
                val response = api.post<Unit>() {
                    url(urlbase)
                    body = json.write(parameters)
                }
                success(response);
            } catch (ex: Exception){
                failure(ex)
            }
        }
    }
}