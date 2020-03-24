package base

import app.shared.appcompania.BuildKonfig

interface IBaseConfig {
    fun url(): String
    fun token1(value: String?): String
}


interface RepositoryBase  {
    val API_SECURITY: String get()  = "/app/v1/QDCJPONYTEDR"

    val ENABLELOGIN: String get()   = "/enableLogin"
    val GENERATEOTP: String get()   = "/generateOtp"
}


enum class EnviromentConfig: IBaseConfig {
    DEV {
        override fun url() = "https://wolframio.grupo-exito.com/apigw"
        override fun token1(value: String?): String {
            return value ?: ""
        }
    },
    QA {
        override fun url() = "https://wolframio.grupo-exito.com/apigw"
        override fun token1(value: String?): String {
            return value ?: ""
        }
    },
    PROD {
        override fun url() = "https://wolframio.grupo-exito.com/apigw"
        override fun token1(value: String?): String {
            return value ?: ""
        }
    }
}
