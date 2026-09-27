package api

import (
	"bytes"
	"context"
	"net/http"
	"net/http/httptest"
	"testing"

	"github.com/shopspring/decimal"
)

type MockService struct{}

func (m *MockService) Calculate(ctx context.Context, a, b decimal.Decimal, operation string) (string, error) {
	if operation == "error" {
		return "", context.DeadlineExceeded
	}
	return "42", nil
}

func TestCalculateHandler(t *testing.T) {
	handler := NewHandler(&MockService{})

	tests := []struct {
		name           string
		method         string
		body           string
		expectedStatus int
		expectedJSON   string
	}{
		{"Invalid Method", http.MethodGet, "", http.StatusMethodNotAllowed, `{"error":"Method not allowed","code":405}`},
		{"Malformed JSON", http.MethodPost, `{"a": 1`, http.StatusBadRequest, `{"error":"Invalid JSON payload","code":400}`},
		{"Missing Operation", http.MethodPost, `{"a":"5","b":"5"}`, http.StatusBadRequest, `{"error":"Missing operation","code":400}`},
		{"Valid Request", http.MethodPost, `{"a":"20","b":"22","operation":"add"}`, http.StatusOK, `{"result":"42"}`},
		{"Service Error", http.MethodPost, `{"a":"1","b":"1","operation":"error"}`, http.StatusBadRequest, `{"error":"context deadline exceeded","code":400}`},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			req := httptest.NewRequest(tt.method, "/api/calculate", bytes.NewBufferString(tt.body))
			w := httptest.NewRecorder()

			handler.CalculateHandler(w, req)

			if w.Code != tt.expectedStatus {
				t.Errorf("expected status %d, got %d", tt.expectedStatus, w.Code)
			}

			// Trim newline from w.Body.String()
			gotBody := w.Body.String()
			if len(gotBody) > 0 && gotBody[len(gotBody)-1] == '\n' {
				gotBody = gotBody[:len(gotBody)-1]
			}
			if gotBody != tt.expectedJSON {
				t.Errorf("expected body %s, got %s", tt.expectedJSON, gotBody)
			}
		})
	}
}
