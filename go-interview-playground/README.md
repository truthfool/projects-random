Go Interview Playground

Quick start

- Install Go 1.21+.
- Run: go run ./cmd/server

What you'll learn

- Goroutines, channels, select, context, worker pool
- Interfaces and composition
- Generics basics with utilities
- Error handling and wrapping
- net/http server, middleware, graceful shutdown
- Rate limiting (token bucket)

API

- GET /health -> ok
- GET /items -> list items
- POST /items {"key":"k","value":"v"}
- POST /work {"payload":"p"}

Exercises

- Add per-user rate limiting via header X-API-Key
- Add timeouts for /work when queue is full
- Write tests for rate limiter and worker pool
