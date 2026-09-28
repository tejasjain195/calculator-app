package com.calculator.app.controller;

import com.calculator.app.dto.CalcResponse;
import com.calculator.app.dto.request.CalcOperationRequest;
import com.calculator.app.service.CalculatorService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class CalculatorController {

    private final CalculatorService calculatorService;

    public CalculatorController(CalculatorService calculatorService) {
        this.calculatorService = calculatorService;
    }

    @PostMapping("/calculate")
    public ResponseEntity<CalcResponse> calculate(@Valid @RequestBody CalcOperationRequest request) {
        String result = calculatorService.evaluate(request);
        return ResponseEntity.ok(new CalcResponse(result));
    }
}
