package service

import (
	"context"
	"testing"

	"github.com/shopspring/decimal"
)

func TestCalculateResult(t *testing.T) {
	svc := NewCalculatorService()
	ctx := context.Background()

	tests := []struct {
		name      string
		a, b      string
		operation string
		want      string
		expectErr string
	}{
		// Basic Arithmetic
		{"Addition", "5", "3", "add", "8", ""},
		{"Addition precision", "0.1", "0.2", "add", "0.3", ""}, // This is the killer feature!
		{"Subtraction", "10", "4", "subtract", "6", ""},
		{"Multiplication", "7", "6", "multiply", "42", ""},
		{"Division", "20", "5", "divide", "4", ""},
		{"Division by zero", "10", "0", "divide", "", "cannot divide by zero"},
		
		// Negative operations
		{"Negative multiply", "-5", "-5", "multiply", "25", ""},
		{"Negative divide", "-10", "2", "divide", "-5", ""},
		{"Negative add", "-5", "-10", "add", "-15", ""},

		// Exponentiation (float cast fallback)
		{"Power basic", "2", "3", "power", "8", ""},
		{"Power zero", "5", "0", "power", "1", ""},
		{"Power negative base frac exp", "-2", "0.5", "power", "", "Not a Real Number"},

		// Square Root
		{"Sqrt basic", "9", "0", "sqrt", "3", ""},
		{"Sqrt negative", "-4", "0", "sqrt", "", "Not a Real Number"},

		// Unknown
		{"Invalid operation", "1", "1", "modulo", "", "unsupported operation"},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			a, _ := decimal.NewFromString(tt.a)
			b, _ := decimal.NewFromString(tt.b)

			got, err := svc.Calculate(ctx, a, b, tt.operation)
			
			if tt.expectErr != "" {
				if err == nil || err.Error() != tt.expectErr {
					t.Fatalf("Calculate() error = %v, expectErr %v", err, tt.expectErr)
				}
				return
			}
			
			if err != nil {
				t.Fatalf("Calculate() unexpected error: %v", err)
			}

			if got != tt.want {
				t.Errorf("Calculate() = %v, want %v", got, tt.want)
			}
		})
	}
}
