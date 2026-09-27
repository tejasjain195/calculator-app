package service

import (
	"context"
	"errors"
	"math"

	"github.com/shopspring/decimal"
)

type CalculatorService interface {
	Calculate(ctx context.Context, a, b decimal.Decimal, operation string) (string, error)
}

type calculatorService struct{}

func NewCalculatorService() CalculatorService {
	return &calculatorService{}
}

func (s *calculatorService) Calculate(ctx context.Context, a, b decimal.Decimal, operation string) (string, error) {
	// Check context cancellation
	if err := ctx.Err(); err != nil {
		return "", err
	}

	switch operation {
	case "add":
		return a.Add(b).String(), nil
	case "subtract":
		return a.Sub(b).String(), nil
	case "multiply":
		return a.Mul(b).String(), nil
	case "divide":
		if b.IsZero() {
			return "", errors.New("cannot divide by zero")
		}
		return a.Div(b).String(), nil
	case "power":
		// shopspring/decimal doesn't support fractional exponents natively with arbitrary precision.
		// Casting to float64 is the accepted trade-off.
		af, _ := a.Float64()
		bf, _ := b.Float64()
		res := math.Pow(af, bf)
		if math.IsNaN(res) {
			return "", errors.New("Not a Real Number")
		}
		if math.IsInf(res, 0) {
			return "", errors.New("Result is Infinity")
		}
		return decimal.NewFromFloat(res).String(), nil
	case "sqrt":
		if a.IsNegative() {
			return "", errors.New("Not a Real Number")
		}
		af, _ := a.Float64()
		res := math.Sqrt(af)
		if math.IsNaN(res) {
			return "", errors.New("Not a Real Number")
		}
		return decimal.NewFromFloat(res).String(), nil
	default:
		return "", errors.New("unsupported operation")
	}
}
