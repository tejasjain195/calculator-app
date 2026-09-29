# Prompts Used



AI tooling (Claude, as reviewer/prompt-writer) and Antigravity (as the coding agent

implementing changes) were used throughout development. Claude reviewed each round of

code, identified concrete bugs/gaps, and wrote the implementation prompts below, which

were passed to Antigravity to execute. This is the full sequence, in order.



\## 1. Initial Migration

"Can you rewrite all the things using Java Spring boot application now. It will be easier

for me to understand. Do not delete the existing code base, rename that to backend-go and

then create a new folder with name backend and then proceed. Maven. Use Maven. Keep it.

Do not delete"



\## 2. Polymorphic Request Pivot

"Use Jackson Request deserializer to achieve this. No need to use instanceof. Keep the

existing also. Make UI understand that if any brackets are present in the payload, then

UI can use the existing method of sending the string. This string request will also be of

same model which is a polymorphic model."



\## 3. First Backend Review \& Fix Prompt (Claude)

Reviewed the initial Spring Boot backend against the assessment brief. Flagged: the

architecture mismatch (expression parser vs. simple operand/operator endpoints), the

Go-vs-Java deviation, missing Bean Validation, a broad exception handler leaking internals,

non-idiomatic DTOs (no records), `Math.pow` precision loss on `^`, malformed-JSON requests

returning 500 instead of 400, and a `assertThrows` misuse in tests that meant error

assertions weren't actually checking exception messages. Wrote a fix prompt covering all

of the above plus a full frontend calculator flow (chained left-to-right mode vs.

parenthesis/expression mode).



\## 4. Polymorphic Request Model, JDK 21 Version

Proposed replacing the flat request DTO with a sealed interface + Jackson `@JsonTypeInfo`/

`@JsonSubTypes`, one record per operation, dispatched via an exhaustive `switch` pattern

match — initially targeting Java 21 for the exhaustive sealed switch.



\## 5. JDK 17 Correction

User clarified the project runs on JDK 17 only. Rewrote the dispatch logic to use an

`instanceof` pattern-matching chain instead of a sealed switch expression (stable since

Java 16, avoids the `--enable-preview` flag Java 21's exhaustive switch would require),

with a comment noting the sealed-switch upgrade path for a future JDK 21 move.



\## 6. Backend Review Round 2 \& Comprehensive Refactoring Plan

Reviewed the polymorphic-model implementation. Found: business logic still living inside

the DTO records instead of the service (layering issue), the `assertThrows` bug still

unfixed, test coverage regressions, unvalidated string operands allowing pathological

input (e.g. `1e999999999`), and the exception handler still turning ordinary client

errors (wrong HTTP method, wrong content type, unknown routes) into 500s. Wrote an

amendment prompt covering: moving arithmetic into `CalculatorService` with shared

`parse`/`format` helpers, input `@Pattern`/`@Size` validation, a result-range guard

against oversized outputs, a full exception-handler rewrite (400/404/405/415), JaCoCo

coverage reporting, and an expanded test list.



\## 7. Post-Refactor Bug Fixes

After Antigravity implemented the refactoring plan, ran the actual service logic through

edge cases and found three regressions: `^` had become left-associative (`2^3^2` returning

64 instead of 512), an exponent range check that truncated to `int` before comparing

(allowing `2^4294967298` to silently return 4), and a test asserting the wrong message

for a range-overflow case. Wrote a targeted fix prompt for all three, plus a correction to

the "unknown path" test (POST vs GET on an unmapped route return different status codes

in Spring).



\## 8. Frontend Review \& Fix Prompt

Reviewed the React/TypeScript frontend against the backend contract. Found a real bug

(`√` inside a larger expression, e.g. `5 + √9`, was sent as an invalid basic-operator

payload instead of falling back to the expression endpoint), missing client-side input

validation, `any`-typed payloads, dead/duplicated code, and test/coverage gaps. Wrote a

fix prompt covering all of the above plus minor accessibility and responsive-layout fixes.



\## 9. README Correction Prompts (multiple rounds)

Reviewed drafts of the README against the assessment brief's explicit requirements

(setup instructions, how to run both parts, API examples, design decisions/assumptions).

Corrected an inaccurate "originally written in Go" claim, an overstated "arbitrary

precision" claim (actual behavior is BigDecimal at 34 significant digits, not unbounded),

added real Quick Start steps (both terminal and IntelliJ options for the backend, since

Maven isn't on the user's PATH), and added real curl request/response pairs.



\## 10. Docker Setup \& Debugging

Added a `docker-compose.yml` and per-service Dockerfiles. Debugged, in order: a resource

memory limit too tight for the JVM to boot reliably (256M -> 512M), a frontend build

failure caused by a Node 18 base image incompatible with Vite's toolchain (bumped to

Node 24, matching the locally tested version), and a Windows PowerShell `curl` alias

issue when manually verifying the running containers (resolved by using `curl.exe` or

`Invoke-RestMethod` instead of PowerShell's `Invoke-WebRequest` alias).



\## 11. Final README Polish

Filled in the real JaCoCo/Vitest coverage percentages (replacing placeholder text),

removed "Docker" from the "What I'd Do Next" section since it had since been implemented,

fixed the repo clone URL to use HTTPS instead of SSH (so the repo can be cloned without

the reviewer needing a registered SSH key), and added this file.

