package com.calculator.app.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PercentageRequest(
    @NotNull(message = "operandA is required") 
    @Pattern(regexp="^-?(\\d+(\\.\\d*)?|\\.\\d+)$", message="operandA must be a plain decimal number")
    @Size(max=150)
    String operandA
) implements CalcOperationRequest {}
