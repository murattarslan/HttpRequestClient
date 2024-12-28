package com.murattarslan.httprequestclient

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Serializable
import java.net.URL
import java.net.URLEncoder
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLSocketFactory
/**
 * [HttpRequestClient] sınıfı android cihazdan basit bir şekilde restApi iletişimi sağlamak içindir.
 * bu sınıf ister statik olarak bir yerde tutularak ister [dependency Injection](https://developer.android.com/training/dependency-injection/hilt-android?hl=tr) ile inject edilerek kullanılabilir
 *
 * Kullanım
 * ```
 * val httpRequestClient = HttpRequestClient(baseUrl, headers)
 * ```
 *
 * @param baseUrl api isteği atılacak url bilgisini alır
 * @param headers(opsiyonel) api isteği sırasında gönderilecek header bilgisini alır. [Map] gereklidir
 * ```
 * /// örnek kullanım
 * mapOf(
 *      "Content-Type" to "application/json",
 *      "Accept" to "application/json"
 * )
 * ```
 * @param mSslSocketFactory(opsiyonel) api isteği sırasında sertifika ekleneceği zaman client oluşturulurken bu parametre ile eklenir. [SSLSocketFactory] sınıfı kullanılır
 * @param isLogEnable(opsiyonel) isteklerin log kayıtlarını görmek için client oluşturulurken bu değer true olacak
 * @param handler(opsiyonel) ResponseCode değerlerine göre farklı işlemler yapılmasını sağlar.[ResponseHandler] interface objesi kullanılır
 *
 * @property get
 * @property post
 * */
data class HttpRequestClient(
    val baseUrl: String,
    val headers: Map<String, String> = mapOf(
        "Content-Type" to "application/json",
        "Accept" to "application/json",
        "User-Agent" to "AndroidOS",

        ),
    val mSslSocketFactory: SSLSocketFactory? = null,
    val isLogEnable: Boolean = false,
    val handler: ResponseHandler? = null
) {

    /**
     * [HttpRequestClient.get] fonksiyonu GET request için kullanılır.
     * Client oluşturulduktan sonra çağırılır ve baseUrl, header ve diğer bilgileri [HttpRequestClient] sınıfından alır
     *
     * Kullanımı
     *
     * ```
     * val (response, error) = httpRequestClient.get<T>(endpoint, queryParams, pathParams)
     * ```
     *
     * [T] sınıfı "response" olarak beklenecek sınıftır. eğer gelen response bu sınıfa uyum sağlamazsa "error" döner
     *
     * @param endpoint post isteği atılacak endpoint bilgisini alır
     * @param queryParams post isteği sırasında query gönderilecekse queryMap alır ve url e ekler
     * @param pathParams post isteği sırasında url üzerinden id gibi bir bilgi gönderilecekse Map olarak alır ve url e ekler
     *
     * @return Pair<[T]?,[Response]?> şeklinde döndürür.
     *
     * [T]: Eğer istek sorunsuz şekilde yapıldı ve gelen response değeri beklenen response modeli ile uyum sağlamışsa dolu gelir, aksi durumda null döner
     *
     * [Response]: Eğer istek sırasında bir sorun oluşmuşsa dolu gelir, aksi durumda null döner
     *
     * **/
    suspend inline fun <reified T: Any> get(
        endpoint: String,
        queryParams: Map<String, String>? = null,
        pathParams: Map<String, String>? = null,
    ): Pair<T?, Response?> = withContext(Dispatchers.IO) {
        val modifiedUrl = buildUrlWithParams(endpoint, queryParams, pathParams)
        val urlObject = URL(modifiedUrl)

        val connection = urlObject.openConnection() as HttpsURLConnection
        val responseJson = connection.create(RequestType.GET).request()

        responseJson.toResponse()
    }
    /**
     * [HttpRequestClient.post] fonksiyonu POST request için kullanılır.
     * Client oluşturulduktan sonra çağırılır ve baseUrl, header ve diğer bilgileri [HttpRequestClient] sınıfından alır
     *
     * Kullanımı
     *
     * ```
     * val (response, error) = httpRequestClient.post<T>(endpoint, body, queryParams, pathParams)
     * ```
     *
     * [T] sınıfı "response" olarak beklenecek sınıftır. eğer gelen response bu sınıfa uyum sağlamazsa "error" döner
     *
     * @param endpoint post isteği atılacak endpoint bilgisini alır
     * @param body post isteği sırasında gönderilecek body bilgisini alır
     * @param queryParams post isteği sırasında query gönderilecekse queryMap alır ve url e ekler
     * @param pathParams post isteği sırasında url üzerinden id gibi bir bilgi gönderilecekse Map olarak alır ve url e ekler
     *
     * @return Pair<[T]?,[Response]?> şeklinde döndürür.
     *
     * [T]: Eğer istek sorunsuz şekilde yapıldı ve gelen response değeri beklenen response modeli ile uyum sağlamışsa dolu gelir, aksi durumda null döner
     *
     * [Response]: Eğer istek sırasında bir sorun oluşmuşsa dolu gelir, aksi durumda null döner
     *
     * **/
    suspend inline fun <reified T: Any> post(
        endpoint: String,
        body: Serializable,
        queryParams: Map<String, String>? = null,
        pathParams: Map<String, String>? = null,
    ): Pair<T?, Response?> = withContext(Dispatchers.IO) {
        val modifiedUrl = buildUrlWithParams(endpoint, queryParams, pathParams)
        val urlObject = URL(modifiedUrl)

        val connection = urlObject.openConnection() as HttpsURLConnection
        val responseJson = connection.create(RequestType.POST, body).request()

        responseJson.toResponse()
    }

    private fun HttpsURLConnection.addHeader(key: String, value: String) {
        if (isLogEnable)
            Log.d("HttpRequestClient", "$key: $value")
        setRequestProperty(key, value)
    }

    private fun HttpsURLConnection.addBody(body: Serializable) {
        if (isLogEnable)
            Log.d("HttpRequestClient", "Body: \n${body.toJson()}")
        doOutput = true
        doInput = true
        outputStream.write(body.toJson().toByteArray(Charsets.UTF_8))
    }

    fun HttpsURLConnection.create(
        requestType: RequestType,
        body: Serializable? = null
    ): HttpsURLConnection = apply {
        if (isLogEnable)
            Log.d("HttpRequestClient", "${requestType.name} -- $url")
        headers.apply {
            if (isLogEnable)
                Log.d("HttpRequestClient", "Headers")
            forEach { (key, value) -> addHeader(key, value) }
        }
        requestMethod = requestType.name
        body?.let { addBody(it) }
        mSslSocketFactory?.let { this.sslSocketFactory = it }
    }

    fun HttpsURLConnection.request(): String {
        var response: String
        try {
            val responseCode = responseCode
            if (isLogEnable)
                Log.d("HttpRequestClient", "$requestMethod <$responseCode> -- $url")
            response = when (responseCode) {
                HttpsURLConnection.HTTP_OK -> {
                    inputStream.toJsonObject()
                }
                HttpsURLConnection.HTTP_BAD_REQUEST -> {
                    handler?.onBadRequest()
                    Response(null, ResultMessage("HTTP_BAD_REQUEST", "error", responseMessage)).toJson()
                }
                HttpsURLConnection.HTTP_UNAUTHORIZED -> {
                    handler?.onUnauthorized()
                    Response(null, ResultMessage("HTTP_UNAUTHORIZED", "error", responseMessage)).toJson()
                }
                HttpsURLConnection.HTTP_NOT_FOUND -> {
                    handler?.onNotFound()
                    Response(null, ResultMessage("HTTP_NOT_FOUND", "error", responseMessage)).toJson()
                }
                HttpsURLConnection.HTTP_GATEWAY_TIMEOUT -> {
                    handler?.onGatewayTimeout()
                    Response(null, ResultMessage("HTTP_GATEWAY_TIMEOUT", "error", responseMessage)).toJson()
                }
                HttpsURLConnection.HTTP_INTERNAL_ERROR -> {
                    handler?.onInternalError()
                    Response(null, ResultMessage("HTTP_INTERNAL_ERROR", "error", responseMessage)).toJson()
                }
                HttpsURLConnection.HTTP_UNAVAILABLE -> {
                    handler?.onUnavailable()
                    Response(null, ResultMessage("HTTP_UNAVAILABLE", "error", responseMessage)).toJson()
                }
                else -> {
                    Response(null, ResultMessage("UNKNOWN_ERROR", "error", responseMessage)).toJson()
                }
            }
        } catch (e: Exception) {
            Log.e("HttpRequestClient", "Exception: \n ${e.message}", e)
            response = Response(
                null,
                ResultMessage("Exception", "exception", e.message.toString())
            ).toJson()
        } finally {
            disconnect()
        }
        if (isLogEnable)
            Log.d("HttpRequestClient", "Response: \n $response")
        return response
    }

    inline fun <reified T: Any>String.toResponse(): Pair<T?, Response?> {
        val r = fromJson(T::class)
        val response = fromJson(Response::class)
        return Pair(r, response)
    }

    private fun InputStream.toJsonObject(): String {
        val reader = BufferedReader(InputStreamReader(this))
        val stringBuilder = StringBuilder()
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            stringBuilder.append(line)
        }
        reader.close()
        return stringBuilder.toString()
    }

    fun buildUrlWithParams(
        endpoint: String,
        queryParams: Map<String, String>?,
        pathParams: Map<String, String>?,
    ): String {
        var url = baseUrl + endpoint

        pathParams?.forEach { (key, value) ->
            url = url.replace("{$key}", URLEncoder.encode(value, "UTF-8"))
        }

        queryParams?.let {
            val queryString = it.map { (key, value) ->
                "${URLEncoder.encode(key, "UTF-8")}=${URLEncoder.encode(value, "UTF-8")}"
            }.joinToString("&")

            if (queryString.isNotEmpty()) {
                url = if (url.contains("?")) "$url&$queryString" else "$url?$queryString"
            }
        }

        return url
    }

    data class Response(
        val result: JSONObject?,
        val resultMessage: ResultMessage?
    ) : Serializable {
        fun isSuccess(): Boolean {
            return resultMessage?.type == "success"
        }
    }

    data class ResultMessage(
        val title: String,
        val type: String,
        val message: String,
    ) : Serializable

    enum class RequestType {
        GET,
        POST,
        PUT,
        DELETE
    }
    /**
     * Farklı responseCode değerlerine göre farklı fonksiyonlar çalıştırmak için kullanılır
     * @property onNotFound 404 ise çalışır
     * @property onBadRequest 400 ise çalışır
     * @property onUnauthorized 401 ise çalışır
     * @property onGatewayTimeout 504 ise çalışır
     * @property onInternalError 500 ise çalışır
     * @property onUnavailable 503 ise çalışır
     * */
    interface ResponseHandler{
        /** response code: 404 */
        fun onNotFound(){}
        /** response code: 400 */
        fun onBadRequest(){}
        /** response code: 401 */
        fun onUnauthorized(){}
        /** response code: 504 */
        fun onGatewayTimeout(){}
        /** response code: 500 */
        fun onInternalError(){}
        /** response code: 503 */
        fun onUnavailable(){}
    }
}