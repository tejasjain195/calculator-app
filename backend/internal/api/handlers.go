package api

import (
	"encoding/json"
	"net/http"

	"github.com/shopspring/decimal"
	"github.com/user/calculator-app/backend/internal/service"
)

type CalcRequest struct {
	A         decimal.Decimal `json:"a"`
	B         decimal.Decimal `json:"b"`
	Operation string          `json:"operation"`
}

type CalcResponse struct {
	Result string `json:"result"`
}

type Handler struct {
	calcService service.CalculatorService
}

func NewHandler(svc service.CalculatorService) *Handler {
	return &Handler{calcService: svc}
}

func (h *Handler) CalculateHandler(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodPost {
		respondError(w, http.StatusMethodNotAllowed, "Method not allowed")
		return
	}

	var req CalcRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		respondError(w, http.StatusBadRequest, "Invalid JSON payload")
		return
	}

	if req.Operation == "" {
		respondError(w, http.StatusBadRequest, "Missing operation")
		return
	}

	result, err := h.calcService.Calculate(r.Context(), req.A, req.B, req.Operation)
	if err != nil {
		respondError(w, http.StatusBadRequest, err.Error())
		return
	}

	respondJSON(w, http.StatusOK, CalcResponse{Result: result})
}
