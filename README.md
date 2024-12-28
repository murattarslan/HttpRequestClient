# HttpRequestClient

Bu sınıf giriş seviyede RestApi istekleri atmak için üçüncü taraf yazılımlardan arındırılmış olarak tasarlanmıştır. 
```Retrofit``` gibi karmaşık yapıdan olabildiğince uzak ve basit bir kullanım sunmaktadır.
içerisinde dahili olarak gelen json converter fonksiyonlar sayesinde istek cevaplarını 
objeye çevirmek için ```gson``` gibi üçüncü taraf bir yazılıma ihtiyaç duymaz.

Rehber İçeriği:
- [Kurulum](#Kurulum)
- [Kullanım](#Kullanım)
  - [İstekler](#İstekler)
    - [GET](#GET)
    - [POST](#POST)
  - [Gelişmiş](#Gelişmiş)

## Kurulum

1- app seviyesindeki ```build.gradle``` dosyasında dependencies içerisine ekleyiniz.
```gradle
    implementation("com.murattarslan:httprequestclient:$httpRequestClientVersion")
```

2- proje seviyesinde ```build.gradle``` dosyasında repositories içerisine ekleyiniz.
```gradle
maven {
            url = uri("https://maven.pkg.github.com/murattarslan/httpRequestClient")
            credentials {
                password = <!--GITHUB_TOKEN--!> 
            }
        }
```
3- projenizi senkronize edin.
4- Kullanıma hazır.

## Kullanım

1- ```HttpRequestClient``` sınıfını oluşturun.
```kotlin
val client = HttpRequestClient(
              baseUrl = baseUrl,
              isLogEnable = true
)
```
2- client objesi kullanıma hazır.

### İstekler

Oluşturduğunuz client objesi ile isteklerinizi sorunsuzca ve basit bir şekilde atabilirsiniz.
İstek fonksiyonları iki değer birden döner. bunlardan biri ```response``` diğeri ```error```.

```response``` : Fonksiyon çağırımında ```T``` ile verilecek olan, beklenen response modeli ile döner. 
İstek sırasında bir hata olmadığı sürece servisten gelen cevap bu alanla gelir.

```error``` : İstek sırasında bir hata olduğunda ve ```response``` alanı doldurulamadığında 
bu alan hatanın sebebini belirten bir ```ResultMessage``` ile döner. Bir sorun olmadığında ise bu alan ```null``` olacaktır.

#### GET

```kotlin
val (response, error) = client.get<T>(
        endpoint = endpoint,
        queryParams = queryMap
)
```
Fonksiyon çağırım sırasında istenen parametreler:

```endpoint```: restApi endpoint bilgisidir. ```BaseUrl``` alanı client oluşturulurken verilir.

```pathParams```(opsiyonel): Get isteği sırasında eklenecek ```path``` alanları bir map yardımıyla verilir. 

örnek kullanım 
```kotlin
val endpoint = "api/weather/{city}"
val pathParams = mapOf(
        "city" to "$city"
      )
```

```queryParams```(opsiyonel): Get isteği sırasında eklenecek ```query``` alanları bir map yardımıyla verilir. 

örnek kullanım 
```kotlin
val queryParams = mapOf(
        "lang" to "$lang",
        "search" to "$search"
      )
```

#### POST

```kotlin
val (response, error) = client.post<T>(
        endpoint = endpoint,
        body = body
)
```
Fonksiyon çağırım sırasında istenen parametreler:

```endpoint```: restApi endpoint bilgisidir. ```BaseUrl``` alanı client oluşturulurken verilir.

```body```: istek sırasında gönderilecek objeyi alır.

```pathParams```(opsiyonel): Get isteği sırasında eklenecek ```path``` alanları bir map yardımıyla verilir. 

örnek kullanım 
```kotlin
val endpoint = "api/weather/{city}"
val pathParams = mapOf(
        "city" to "$city"
      )
```

```queryParams```(opsiyonel): Get isteği sırasında eklenecek ```query``` alanları bir map yardımıyla verilir. 

örnek kullanım 
```kotlin
val queryParams = mapOf(
        "lang" to "$lang",
        "search" to "$search"
      )
```

### Gelişmiş

İstemci oluştuma sırasında ```baseUrl``` zorunlu alandır. Bu alan dışında opsiyonel olarak verilebilecek parametrelerle oluşturulan istemci özelleştirilebilir.

- ```header```: İstek sırasında bir ```HeaderMap``` ekleyebilirsiniz.
-  ```mSslSocketFactory```: istek atarken sertifika doğrulayıcı veya bir sslPinning ihtiyacınız varsa buradan ekleyebilirsiniz.
-  ```isLogEnable```: Debug mod sırasında gönderilen isteklerin log kayıtlarını görmek
  isterseniz bu alanı ```true``` yapmanız yeterli olacaktır. ```BuildConfig.Debug``` olarak ayarlamanız tavsiye edilir.
-  ```handler```: İstek cevaplarının ```HTTP_OK``` dışında bir kod ile dönmesi durumunda bu handler içerisindeki ilgili fonksiyonlar tetiklenir.
   ```401 Unauthorized``` gibi durumları buradan yönetebilirsiniz.
