package com.calculator.app.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SubtractRequest(
    @NotNull(message = "operandA is required") 
    @Pattern(regexp="^-?(\\d+(\\.\\d*)?|\\.\\d+)$", message="operandA must be a plain decimal number")
    @Size(max=150)
    String operandA, 
    
    @NotNull(message = "operandB is required") 
    @Pattern(regexp="^-?(\\d+(\\.\\d*)?|\\.\\d+)$", message="operandB must be a plain decimal number")
    @Size(max=150)
    String operandB
) implements CalcOperationRequest {}
