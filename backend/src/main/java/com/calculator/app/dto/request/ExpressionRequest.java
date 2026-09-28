package com.calculator.app.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ExpressionRequest(
    @NotBlank(message = "expression is required") 
    @Size(max=500)
    String expression
) implements CalcOperationRequest {}
