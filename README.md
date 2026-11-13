# Tick Tracker

An MVP backend for a UK tick-sightings app, built for the Elanco placement technical task by **Rui Deng**.

It loads tick sightings from the Elanco API, cleans them, stores them in a local database and serves them through a REST API. The API supports search by location and date range, a count of sightings per region, and weekly or monthly trends.


---

## How to run it

**You need:** Java 21. Maven comes with the project (`mvnw`), so you don't need to install it.

```bash
./mvnw spring-boot:run
```

On Windows, use `mvnw.cmd spring-boot:run`.

When it starts, the log shows what happened during loading, for example:

```
Cleaning: 1000 records in, 1000 kept, 0 skipped as incomplete, 0 skipped for a bad date, 0 skipped as duplicates
Ingestion finished: fetched 1000 records, database now has 1000 sightings
```

Then open **http://localhost:8080/**, which links to every endpoint.

**To run the tests:**

```bash
./mvnw test
```

The tests don't need the internet. They use a fake API and small hand-made data.

---

## Endpoints

All endpoints are `GET` and return JSON.

| Endpoint | What it returns |
|---|---|
| `/health` | `{"status":"UP"}` if the app is running |
| `/sightings` | Sightings, newest first. All filters are optional: `location`, `from`, `to` |
| `/stats/regions` | Number of sightings per city, busiest first |
| `/stats/trends?period=monthly` | Sightings per month (or `period=weekly`, where each week is named by its Monday) |

**Examples:**

- `/sightings?location=london&from=2022-01-01&to=2022-12-31` returns London sightings in 2022. The location match ignores case, and `to` includes the whole of that day.
- `/stats/regions` returns `[{"region":"Nottingham","sightings":95},{"region":"Birmingham","sightings":82}, ...]`
- `/stats/trends?period=monthly` returns `[{"period":"2012-01","sightings":9},{"period":"2012-02","sightings":9}, ...]`

**Errors** use the standard `ProblemDetail` JSON format:

```json
{"title":"Bad Request","status":400,"detail":"'from' must be on or before 'to'","instance":"/sightings"}
```

| Request | Response |
|---|---|
| `/sightings?from=banana` | 400: invalid date |
| `/sightings?from=2023-01-01&to=2022-01-01` | 400: `from` is after `to` |
| `/stats/trends?period=yearly` | 400: period must be weekly or monthly |
| Unknown URL | 404 |
| Anything unexpected | 500 with a general message. The details go to the server log, not the user. |

The H2 database console is at `/h2-console` (JDBC URL `jdbc:h2:mem:ticks`, user `sa`, no password).

---

## How it works

```
Elanco API ──► TickApiClient ──► SightingCleaner ──► H2 database ◄── SightingService / StatsService ◄── Controllers ◄── user
              (retry, timeouts)   (validate, dedupe)                  (search, reports)
```

**At startup**, `IngestionService` fetches all sightings once, cleans them and saves them. Users are then served from **my own database**, not the Elanco API. The source API is rate limited (about 15–20 quick requests return 429), so calling it for every user request wouldn't work.

| Package | Job |
|---|---|
| `client` | Talks to the Elanco API: `TickApiClient`, `SightingApiDto`, `TickApiException` |
| `config` | Builds the HTTP client with timeouts (`TickApiConfig`) and prints the startup links |
| `model` | `Sighting`, the database table |
| `repository` | `SightingRepository`, which holds the database queries |
| `service` | The logic: ingestion, cleaning, search and reports |
| `dto` | Shapes for report responses (`RegionCount`, `TrendPoint`) |
| `controller` | The HTTP endpoints, plus `ApiExceptionHandler` for errors |

### Data handling

`SightingCleaner` checks every record before it's saved:

- **Incomplete records** (any field missing or blank) are skipped.
- **Bad dates** (not in the `2022-08-01T06:40:31` format) are skipped.
- **Duplicates** (an ID that has already been seen) are skipped, and the first copy is kept.
- Text is **trimmed**, so `" London"` and `"London"` count as the same city.

It logs how many records were skipped and why. One bad record doesn't stop the other 999 from loading. The current Elanco data is clean (0 skipped), but the brief asks for this and the data could change.

### API failures

`TickApiClient` retries the errors that might fix themselves:

- **Retried:** 429 (rate limited), any 5xx (server error), and network errors or timeouts. It makes up to 4 attempts, waiting 1s, 2s and then 4s between them (exponential backoff).
- **Not retried:** other 4xx errors such as 404. These won't change if you wait, so it fails straight away.
- **Timeouts:** 5 seconds to connect and 10 seconds to read, so a hanging API can't freeze startup.

If the API is still down after retrying, the app **logs the error and keeps running** instead of crashing.

---

## Tests

There are 23 tests, run with `./mvnw test`.

| Test class | What it proves |
|---|---|
| `TickApiClientTest` | Retries a 429 then succeeds; doesn't retry a 404; gives up after 4 server errors. Uses a fake API (`MockRestServiceServer`) with no delay. |
| `SightingCleanerTest` | Keeps and converts a valid record; skips missing fields, bad dates and duplicates; doesn't remember IDs between runs |
| `SightingServiceTest` | Search with no filters, location ignoring case, date range including the whole last day, and combined filters. Runs against a real H2 database (`@DataJpaTest`). |
| `StatsServiceTest` | Per-region counts, monthly and weekly grouping (including a week that crosses a month boundary), and rejecting an unknown period |
| `ErrorResponsesTest` | Sends real HTTP requests (`MockMvc`) and checks the status codes and error messages |
| `TickTrackerApplicationTests` | The whole app starts. Ingestion is switched off so it doesn't call the real API. |

I tested the classes that make decisions. Controllers that only pass values through are covered by `ErrorResponsesTest` and by trying the endpoints.

---

## Decisions

| Decision | Why | Trade-off |
|---|---|---|
| **Java + Spring Boot** | I know Java. Spring Boot is widely used for REST APIs, and gives me the web server, database access and testing tools in one place. | More to learn than a small framework like Javalin |
| **Load once, then serve from my own database** | The Elanco API is rate limited and could go down. My endpoints never depend on it after startup. | Data is only as fresh as the last startup |
| **H2 in-memory database** | No setup, so anyone can run the project with one command | Data is lost on restart, so if the API is down at startup the app has 0 sightings |
| **Hand-written retry loop** instead of Spring's `@Retryable` | There's only one place that needs it. It's easy to read and easy to unit test without Spring. | With more API calls, `@Retryable` or Resilience4j would avoid repeating the loop |
| **Separate `SightingCleaner`** | Keeps the validation rules in one place and makes them testable without an API or database | One more class |
| **One optional-filter query** (`:param IS NULL OR ...`) | One query handles every combination of filters | Gets messy with many filters; Spring Data Specifications would scale better |
| **Region counts in the database, trends in Java** | `GROUP BY` is fast and standard for counts. Grouping by week is written differently in every database, so I did it in Java to keep it portable. | Trends load all sightings into memory, which is fine for 1000 but not for millions |
| **`ProblemDetail` for errors** | A standard error format (RFC 9457) that's built into Spring | None really |

---

## What I'd improve with more time

- **Fill empty periods in trends with 0.** At the moment, weeks or months with no sightings are left out, so a chart would hide quiet periods.
- **Keep data across restarts** by using a file-based or real database (e.g. PostgreSQL), so the app can serve the last good data if the API is down.
- **Refresh the data on a schedule** (e.g. daily) instead of only at startup.
- **Paging on `/sightings`.** It currently returns every match, which is fine for 1000 records but not for a large dataset.
- **Group trends in the database** for large datasets.
- **Filter by species**, and add trends per city.
- **Better duplicate detection.** At the moment only identical IDs are caught, not the same sighting reported twice with different IDs.
- **Normalise the data:** a separate species table instead of repeating names on every row.
- **AI/ML insights (the optional extension):** for example, flagging cities where sightings are rising faster than usual.
