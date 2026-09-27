package main

import (
	"context"
	"log/slog"
	"net/http"
	"os"
	"os/signal"
	"syscall"
	"time"

	"github.com/user/calculator-app/backend/internal/api"
	"github.com/user/calculator-app/backend/internal/service"
)

func main() {
	// Initialize structured logger
	logger := slog.New(slog.NewJSONHandler(os.Stdout, nil))
	slog.SetDefault(logger)

	// Dependency Injection
	calcService := service.NewCalculatorService()
	handlers := api.NewHandler(calcService)

	// Router setup
	mux := http.NewServeMux()
	mux.HandleFunc("/api/calculate", handlers.CalculateHandler)

	// Chain middlewares
	handler := api.LoggingMiddleware(
		api.RecoverMiddleware(
			api.TimeoutMiddleware(5*time.Second)(
				api.CORSMiddleware(mux),
			),
		),
	)

	// Server config
	srv := &http.Server{
		Addr:    ":8080",
		Handler: handler,
	}

	// Graceful shutdown channel
	stop := make(chan os.Signal, 1)
	signal.Notify(stop, os.Interrupt, syscall.SIGTERM)

	go func() {
		slog.Info("Server starting on port 8080")
		if err := srv.ListenAndServe(); err != nil && err != http.ErrServerClosed {
			slog.Error("Server error", "error", err)
			os.Exit(1)
		}
	}()

	<-stop
	slog.Info("Shutting down gracefully...")

	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()

	if err := srv.Shutdown(ctx); err != nil {
		slog.Error("Server forced to shutdown", "error", err)
	}
	slog.Info("Server exiting")
}
