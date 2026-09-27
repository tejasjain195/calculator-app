# Calculator Application - Senior Design Document

## 1. System Architecture
This is a Full-Stack Calculator built with **React (TypeScript)** and a **Go Microservice** backend. It strictly adheres to Separation of Concerns and clean architecture principles.

### Backend (Go)
The backend is structured into `cmd/` (entrypoints) and `internal/` (private application code).
- **Service Layer (`internal/service`):** Contains pure business logic. Calculates arithmetic strictly using arbitrary precision types.
- **API Layer (`internal/api`):** Houses HTTP handlers and Middlewares. Responsible solely for request parsing, context propagation, and formatting standardized JSON error boundaries.
- **Middleware:** Includes structured JSON logging (`slog`), Timeout contexts, Panic Recovery, and CORS.
- **Graceful Shutdown:** The server intercepts OS signals (SIGTERM/SIGINT) to allow active requests to finish draining before terminating the process.

### Frontend (React)
- **Vite:** Chosen for optimal DX, instant HMR, and unbundled local development.
- **State Machine:** Uses React Hooks to manage explicit transitions between calculator states (e.g., buffering inputs vs executing immediate successive operations).
- **Resiliency:** Wraps the core logic in a top-level `ErrorBoundary` component to prevent White Screen of Death (WSOD). Includes `AbortController` timeouts for network resilience.

## 2. Trade-offs & Engineering Decisions

### FinTech-Grade Precision (`shopspring/decimal`)
Floating point arithmetic (IEEE 754) is notoriously problematic for financial systems (e.g., `0.1 + 0.2 = 0.30000000000000004`). 
- **Decision:** We migrated the backend math engine to use `shopspring/decimal`.
- **Trade-off:** Arbitrary precision libraries are significantly slower than raw CPU float instructions and consume more memory. Additionally, operations like continuous roots or fractional exponents are inherently irrational and incompatible with finite arbitrary precision.
- **Compromise:** Standard operators (`+`, `-`, `*`, `/`) execute via strict `decimal` types. Complex non-linear operations (`^`, `√`) degrade gracefully by casting down to `float64`, evaluating via standard library `math`, capturing `NaN` or `Inf` boundaries, and casting back to a precise string representation.

### DevOps & Containerization
- **Multi-Stage Build:** The Go service builds in an Alpine builder image, but deploys onto a completely empty `FROM scratch` container. 
- **Security:** `docker-compose` is hardcoded to run non-root users (`1000:1000`) and enforce strict CPU (0.5) and Memory (256MB) limits, reducing blast radius in a multi-tenant orchestration environment.

## 3. Scaling Considerations
- The Go backend is strictly stateless and maintains zero cache or session state. It can be horizontally scaled identically behind any L7 Load Balancer.
- To handle massive throughput, we would migrate from `net/http` router to a faster radix-tree router like `chi` and consider caching repetitive computations (e.g. `2^10`) in Redis if compute latency becomes the bottleneck over network latency.

## 4. Setup & Deployment (DX)

### Run via Docker (Production Config)
```bash
docker-compose up --build
```
> Navigate to http://localhost:3000

### Run Locally (Development)
**Backend:**
```bash
cd backend
go run cmd/api/main.go
# Runs on :8080
```

**Frontend:**
```bash
cd frontend
npm install
npm run dev
# Runs on :3000
```
