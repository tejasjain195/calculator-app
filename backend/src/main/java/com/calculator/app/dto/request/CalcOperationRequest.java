package com.calculator.app.dto.request;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "operator")
@JsonSubTypes({
  @JsonSubTypes.Type(value = AddRequest.class, name = "add"),
  @JsonSubTypes.Type(value = SubtractRequest.class, name = "subtract"),
  @JsonSubTypes.Type(value = MultiplyRequest.class, name = "multiply"),
  @JsonSubTypes.Type(value = DivideRequest.class, name = "divide"),
  @JsonSubTypes.Type(value = PowerRequest.class, name = "power"),
  @JsonSubTypes.Type(value = SqrtRequest.class, name = "sqrt"),
  @JsonSubTypes.Type(value = PercentageRequest.class, name = "percentage"),
  @JsonSubTypes.Type(value = ExpressionRequest.class, name = "expression")
})
public sealed interface CalcOperationRequest 
    permits AddRequest, SubtractRequest, MultiplyRequest, DivideRequest, 
            PowerRequest, SqrtRequest, PercentageRequest, ExpressionRequest {
}
