# React Calculator Frontend

A modern, responsive React frontend for the Java Spring Boot calculator backend, built with Vite and TypeScript.

## Architecture

This frontend adheres to the strict REST API contract defined by the backend `/api/calculate` endpoint. It sends well-typed polymorphic request payloads:
- **Binary Operations:** `{ operator: 'add', operandA: '5', operandB: '3' }` (Supports: add, subtract, multiply, divide, power)
- **Unary Operations:** `{ operator: 'sqrt', operandA: '9' }` (Supports: sqrt, percentage)
- **Expressions:** `{ operator: 'expression', expression: '5 + 5 * 2' }`

## Features

- **Intelligent Auto-Calculation:** (Chained mode) Calculates basic expressions instantly from left-to-right when pressing a subsequent operator (e.g., `5 + 3 *` will instantly evaluate `5+3` and transition to `8 *`).
- **Bracket Mode:** Any expression containing brackets `()` will bypass auto-calculation and be fully evaluated via the backend's standard PEMDAS rules when pressing `=`.
- **Percentage Support:** Quickly calculate percentages with the `%` button.
- **Robust Input Guardrails:** Automatically blocks double-dots, unmatched closing brackets, and duplicate operators.
- **Concurrency Safety:** All buttons are disabled while calculating to prevent race conditions during slow network requests.

## Setup & Running

Install dependencies:
```bash
npm ci
```

Start the development server (runs on **port 3000**):
```bash
npm run dev
```

Build for production:
```bash
npm run build
```

Run Tests & Coverage:
```bash
npm run test:coverage
```

## Configuration

The backend URL is configurable via environment variables. Create a `.env` file at the root of this frontend folder:
```env
VITE_API_URL=http://localhost:8080
```
By default, it will gracefully fallback to `http://localhost:8080` (the backend's default port) if this environment variable is missing.
