# Calculator App

A fullstack calculator application featuring a React/Vite frontend and a Java Spring Boot backend.

Note: the backend is Java/Spring Boot rather than the preferred Go - see 'Why Java instead of Go' below.

## Quick Start
**Prerequisites:** JDK 17, Maven, Node.js (v24.14.0)

1. Clone the repository:
```bash
git clone <repository_url>
cd <repository_directory>
```

2. **Backend:**
Navigate to the `backend/` directory and run the application:
```bash
cd backend
mvn spring-boot:run
```
The backend will run on `http://localhost:8080`. *(Note: `./mvnw` is not available in this repo.)*

3. **Frontend:**
Navigate to the `frontend/` directory, install dependencies, and start the UI:
```bash
cd frontend
npm ci
npm run dev
```
The frontend will be accessible at `http://localhost:3000`.

**Frontend Environment Setup:**
The frontend connects to the backend using the `VITE_API_URL` environment variable (defaults to `http://localhost:8080`). To customize this, create a `.env` file in the `frontend/` directory:
```env
VITE_API_URL=http://localhost:8080
```

## API

The backend exposes a single POST endpoint `/api/calculate`.

| Operator | Required Fields |
|----------|-----------------|
| `add`, `subtract`, `multiply`, `divide`, `power` | `operandA`, `operandB` |
| `sqrt`, `percentage` | `operandA` |
| `expression` | `expression` |

Operands are securely passed as JSON strings to avoid floating point precision loss.

### Curl Examples & Responses

**Success Example Body:** `{"result":"8"}`
**Error Example Body:** `{"error":"cannot divide by zero","code":400}`

```bash
# Add
curl -X POST http://localhost:8080/api/calculate -H "Content-Type: application/json" -d '{"operator":"add","operandA":"5","operandB":"3"}'
# Response: {"result":"8"}

# Subtract
curl -X POST http://localhost:8080/api/calculate -H "Content-Type: application/json" -d '{"operator":"subtract","operandA":"10","operandB":"4"}'
# Response: {"result":"6"}

# Multiply
curl -X POST http://localhost:8080/api/calculate -H "Content-Type: application/json" -d '{"operator":"multiply","operandA":"5","operandB":"5"}'
# Response: {"result":"25"}

# Divide
curl -X POST http://localhost:8080/api/calculate -H "Content-Type: application/json" -d '{"operator":"divide","operandA":"20","operandB":"5"}'
# Response: {"result":"4"}

# Power
curl -X POST http://localhost:8080/api/calculate -H "Content-Type: application/json" -d '{"operator":"power","operandA":"2","operandB":"3"}'
# Response: {"result":"8"}

# Square Root (Unary)
curl -X POST http://localhost:8080/api/calculate -H "Content-Type: application/json" -d '{"operator":"sqrt","operandA":"9"}'
# Response: {"result":"3"}

# Percentage (Unary)
curl -X POST http://localhost:8080/api/calculate -H "Content-Type: application/json" -d '{"operator":"percentage","operandA":"50"}'
# Response: {"result":"0.5"}

# Shunting-Yard Expression
curl -X POST http://localhost:8080/api/calculate -H "Content-Type: application/json" -d '{"operator":"expression","expression":"(5+5)*2"}'
# Response: {"result":"20"}
```

### Error Codes
| HTTP Status | Trigger |
| ----------- | ------- |
| 400 Bad Request | Missing operands, division by zero, malformed JSON, unknown operators, missing expressions, out-of-bounds exponents, invalid operand format, sqrt of a negative number, result out of range. |
| 404 Not Found | Calling an unknown API path. |
| 405 Method Not Allowed | Calling the API with GET/PUT/DELETE instead of POST. |
| 415 Unsupported Media Type | Calling the API without `application/json` Content-Type. |
| 500 Internal Error | Unhandled server crashes (logged via SLF4J). |

## Chained mode vs expression mode
The backend never chains; it is stateless. The UI does: without brackets, each time an operator is pressed after two operands, it calls the API for that one operation and feeds the result back as the next operand (left-to-right, like a handheld calculator). With brackets, the UI sends the whole string as one `expression` request, evaluated with standard operator precedence. Example: typing `2 + 3 × 4 =` gives 20 (chained), while `2 + (3 × 4) =` gives 14 (expression). This is intentional.

Note: Percentage (`%`) is calculated instantly as `value/100` and is not supported inside expression strings.

## Design Decisions
- **Single polymorphic endpoint**: Implemented via Jackson `@JsonTypeInfo`, a sealed interface, and one record per operation, so each type strictly validates its own required fields.
- **String operands**: Eliminates precision loss at the JSON serialization layer.
- **Layering**: Clean separation of concerns between controller, service, DTO records, and a global exception handler.
- **Input validation**: Handled explicitly using `@Pattern`, `@Size`, `@NotNull`, and a result-range guard for safety.
- **JDK 17 pattern matching**: Uses `instanceof` pattern matching chains to stay perfectly compatible with JDK 17 (a sealed exhaustive switch is the Java 21 upgrade path).
- **Client-side state**: The frontend intentionally keeps chaining logic client-side while the backend stays stateless.

## Precision, Limits & Edge Cases
- **Precision:** Exact decimal arithmetic using `BigDecimal` with `MathContext.DECIMAL128` (34 significant digits, `HALF_EVEN`), which avoids binary floating-point errors (e.g. `0.1 + 0.2 = 0.3`).
- **Exponents:** Whole-number exponents in `[-1000, 1000]` use `BigDecimal.pow`, rounded to 34 significant digits (exact whenever the result fits, e.g. `3^40`); fractional exponents fall back to double precision (~16-17 digits, e.g. `2^0.5`).
- **Results:** Results whose magnitude is beyond about 1e+-100 (including very small values) are rejected with 'Result out of supported range'.
- **Limits:** Operands max 150 chars, expressions max 500 chars, JDK 17 requirement.
- **Unary minus:** Matches standard mathematical convention (and Python): `-2^2 = -4`; `^` is right-associative: `2^3^2 = 512`.
- **Whitespace:** Ignored between tokens; `2 3` is invalid.
- **Edge cases:** `0^0 = 1`, `0^-1` -> "cannot divide by zero", sqrt of a negative -> "Not a Real Number", percentage = `value/100`.

## Testing & Coverage

**Backend:**
```bash
mvn clean verify
```
*Note: Due to environmental limits, `mvn clean verify` failed locally because Maven (`mvn`) was not found in the PATH. The JaCoCo test coverage report will normally be generated at `backend/target/site/jacoco/index.html`.*

**Frontend:**
```bash
npm run test
npm run test:coverage
```
*Frontend line coverage runs at **82.01%**.*

## Why Java instead of Go
The brief prefers Go. I chose Java/Spring Boot because it is the stack I am strongest in, which let me spend the time budget on correctness, validation and tests. The design (stateless service, single JSON endpoint, pure calculation core) ports directly to Go, and I am happy to discuss how I would do it.

## What I'd Do Next
- Docker (multi-stage)
- OpenAPI docs
- Rate limiting
- A Go port

## AI Usage
Prompts used are in [PROMPTS.md](PROMPTS.md)
