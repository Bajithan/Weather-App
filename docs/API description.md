# API Usage — Weather App

## API Used

**OpenWeatherMap — Current Weather Data API**

- **Base URL:** `https://api.openweathermap.org/data/2.5/`
- **Endpoint:** `weather`
- **HTTP Method:** `GET`
- **Full sample request:**
  ```
  GET https://api.openweathermap.org/data/2.5/weather?q=Colombo&appid=YOUR_API_KEY&units=metric
  ```
- **Authentication:** API key required, passed as a query parameter (`appid`). Free tier, obtained from https://home.openweathermap.org/users/sign_up. New keys can take up to ~1 hour to activate.

---

## Why GET

`GET` is used because the app only **reads** weather data — it never creates, updates, or deletes anything on the server. GET requests are read-only and stateless: every search is a fresh, independent request with no memory of previous searches.

Unlike `POST`, a GET request has no request body — all input data (city name, API key, unit system) is sent as part of the URL itself, in the **query string** (everything after the `?`, joined with `&`).

---

## Request Parameters

| Parameter | Query Key | Example | Purpose |
|---|---|---|---|
| City name | `q` | `Colombo` | The location to fetch weather for |
| API key | `appid` | `d4b8da1226...` | Authenticates the request against the developer account |
| Units | `units` | `metric` / `imperial` | `metric` = °C + km/h, `imperial` = °F + mph |

These are declared via Retrofit's `@Query` annotations in `WeatherApiService.kt`:

```kotlin
interface WeatherApiService {
    @GET("weather")
    suspend fun getWeather(
        @Query("q") city: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): WeatherResponse
}
```

Retrofit combines the `BASE_URL` (from `RetrofitClient.kt`) + `"weather"` + each `@Query` value to build the final request URL automatically — no manual string concatenation.

---

## Response

**Status code `200 OK`** with a JSON body, e.g.:

```json
{
  "name": "Colombo",
  "main": { "temp": 29.34, "humidity": 78 },
  "weather": [ { "main": "Clouds", "description": "scattered clouds" } ],
  "wind": { "speed": 3.6 }
}
```

This JSON is automatically converted into a Kotlin object by **Gson** (registered as a converter on the Retrofit client), matching JSON keys to properties in `WeatherResponse.kt`:

```kotlin
data class WeatherResponse(
    val main: Main,
    val weather: List<Weather>,
    val wind: Wind,
    val name: String
)
data class Main(val temp: Double, val humidity: Int)
data class Weather(val main: String, val description: String)
data class Wind(val speed: Double)
```

No manual JSON parsing is written anywhere in the app — Retrofit + Gson handle it entirely based on this data class structure.

---

## Full Request Flow

1. User types a city name and taps **Search Weather**
2. Input is validated (non-empty); a coroutine (`lifecycleScope.launch`) is started so the network call doesn't block the UI
3. `RetrofitClient.api.getWeather(city, apiKey, units)` is called
4. Retrofit builds the full GET URL from the `@Query` parameters and sends the request
5. OpenWeatherMap's server responds with a status code + JSON body
6. Gson automatically parses the JSON into a `WeatherResponse` object
7. The coroutine resumes with the parsed `response`, and the UI (`TextView`s) is updated directly from it

```
User taps Search
      │
      ▼
Coroutine starts (non-blocking)
      │
      ▼
RetrofitClient.api.getWeather(city, apiKey, units)
      │
      ▼
Retrofit builds URL from @Query params ──► GET request sent over internet
      │
      ▼
OpenWeatherMap server responds (status code + JSON)
      │
      ▼
Gson parses JSON → WeatherResponse object
      │
      ▼
Coroutine resumes → UI updated with response.name, response.main.temp, etc.
```

---

## Error Handling

| Scenario | Exception Type | Cause | User-facing message |
|---|---|---|---|
| Invalid/unknown city | `HttpException`, code `404` | Server responds with an error status | "City not found. Please check the spelling." |
| No internet / DNS failure / timeout | `IOException` | Request never reaches the server at all | "Network error. Please check your internet connection." |
| Anything else unexpected | `Exception` (generic) | Safety net | "Something went wrong. Please try again." |

```kotlin
try {
    val response = RetrofitClient.api.getWeather(city, apiKey, unitSystem)
    // update UI with response
} catch (e: HttpException) {
    if (e.code() == 404) { /* city not found */ } else { /* other server error */ }
} catch (e: IOException) {
    // network error
} catch (e: Exception) {
    // generic fallback
}
```

`HttpException` means the server *did* respond, just with an error status code. `IOException` means the request never made it to the server at all — an important distinction, since they represent two completely different failure points in the request/response cycle.

---

## Key Files Involved

| File | Role |
|---|---|
| `network/WeatherApiService.kt` | Declares the GET endpoint and its query parameters (the "request contract") |
| `network/RetrofitClient.kt` | Builds the singleton Retrofit client with the base URL and Gson converter |
| `network/WeatherResponse.kt` | Data classes mirroring the JSON response shape, used by Gson to auto-parse |
| `fragments/DashboardFragment.kt` | Triggers the request on button click, handles the coroutine, updates the UI, and catches errors |
