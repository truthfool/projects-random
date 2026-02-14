package main

import (
    "context"
    "log"
    "os"
    "os/signal"
    "syscall"
    "time"

    "github.com/ishanranasingh/go-interview-playground/internal/concurrency"
    "github.com/ishanranasingh/go-interview-playground/internal/httpserver"
    "github.com/ishanranasingh/go-interview-playground/internal/repo"
)

func main() {
    ctx, cancel := signal.NotifyContext(context.Background(), syscall.SIGINT, syscall.SIGTERM)
    defer cancel()

    storage := repo.NewInMemoryRepository()
    pool := concurrency.NewWorkerPool(4, 64)
    pool.Start()
    defer pool.Stop()

    srv := httpserver.NewServer(8080, storage, pool)

    go func() {
        if err := srv.Start(); err != nil {
            log.Printf("server stopped: %v", err)
        }
    }()

    <-ctx.Done()
    shutdownCtx, cancelShutdown := context.WithTimeout(context.Background(), 5*time.Second)
    defer cancelShutdown()
    if err := srv.Shutdown(shutdownCtx); err != nil {
        log.Printf("graceful shutdown error: %v", err)
        os.Exit(1)
    }
}


