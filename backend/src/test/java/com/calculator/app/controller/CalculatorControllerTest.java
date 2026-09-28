package com.calculator.app.controller;

import com.calculator.app.service.CalculatorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CalculatorController.class)
@Import(CalculatorService.class)
class CalculatorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testAdd200() throws Exception {
        mockMvc.perform(post("/api/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"operator\":\"add\", \"operandA\":\"5\", \"operandB\":\"3\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("8"));
    }

    @Test
    void testSubtract200() throws Exception {
        mockMvc.perform(post("/api/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"operator\":\"subtract\", \"operandA\":\"10\", \"operandB\":\"4\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("6"));
    }

    @Test
    void testMultiply200() throws Exception {
        mockMvc.perform(post("/api/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"operator\":\"multiply\", \"operandA\":\"7\", \"operandB\":\"6\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("42"));
    }

    @Test
    void testDivide200() throws Exception {
        mockMvc.perform(post("/api/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"operator\":\"divide\", \"operandA\":\"20\", \"operandB\":\"5\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("4"));
    }

    @Test
    void testPower200() throws Exception {
        mockMvc.perform(post("/api/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"operator\":\"power\", \"operandA\":\"2\", \"operandB\":\"3\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("8"));
    }

    @Test
    void testSqrt200() throws Exception {
        mockMvc.perform(post("/api/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"operator\":\"sqrt\", \"operandA\":\"9\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("3"));
    }

    @Test
    void testPercentage200() throws Exception {
        mockMvc.perform(post("/api/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"operator\":\"percentage\", \"operandA\":\"50\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("0.5"));
    }

    @Test
    void testExpression200() throws Exception {
        mockMvc.perform(post("/api/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"operator\":\"expression\", \"expression\":\"(5+5)*2\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("20"));
    }

    @Test
    void testUnknownOperator400() throws Exception {
        mockMvc.perform(post("/api/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"operator\":\"modulo\", \"operandA\":\"5\", \"operandB\":\"3\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Malformed request or unknown/missing operator. Supported: add, subtract, multiply, divide, power, sqrt, percentage, expression"))
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void testMissingOperator400() throws Exception {
        mockMvc.perform(post("/api/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"operandA\":\"5\", \"operandB\":\"3\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Malformed request or unknown/missing operator. Supported: add, subtract, multiply, divide, power, sqrt, percentage, expression"))
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void testMissingOperand400() throws Exception {
        mockMvc.perform(post("/api/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"operator\":\"add\", \"operandA\":\"5\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("operandB is required"))
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void testNonNumericOperandAbc400() throws Exception {
        mockMvc.perform(post("/api/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"operator\":\"add\", \"operandA\":\"abc\", \"operandB\":\"3\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("operandA must be a plain decimal number"))
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void testNonNumericOperandSciNotation400() throws Exception {
        mockMvc.perform(post("/api/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"operator\":\"add\", \"operandA\":\"1e999999999\", \"operandB\":\"3\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("operandA must be a plain decimal number"))
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void testMalformedJson400() throws Exception {
        mockMvc.perform(post("/api/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{operator:add}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Malformed request or unknown/missing operator. Supported: add, subtract, multiply, divide, power, sqrt, percentage, expression"));
    }

    @Test
    void testEmptyBody400() throws Exception {
        mockMvc.perform(post("/api/calculate")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Malformed request or unknown/missing operator. Supported: add, subtract, multiply, divide, power, sqrt, percentage, expression"));
    }

    @Test
    void testDivideByZero400() throws Exception {
        mockMvc.perform(post("/api/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"operator\":\"divide\", \"operandA\":\"10\", \"operandB\":\"0\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("cannot divide by zero"))
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void testRangeOverflow400() throws Exception {
        mockMvc.perform(post("/api/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"operator\":\"expression\", \"expression\":\"(10^1000)^1000\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Result out of supported range"));
    }

    @Test
    void testPowerExponentLimit400() throws Exception {
        mockMvc.perform(post("/api/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"operator\":\"power\", \"operandA\":\"2\", \"operandB\":\"1001\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Exponent must be between -1000 and 1000"));
    }

    @Test
    void testMethodNotAllowed405() throws Exception {
        mockMvc.perform(get("/api/calculate"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void testUnsupportedMediaType415() throws Exception {
        mockMvc.perform(post("/api/calculate")
                .contentType(MediaType.TEXT_PLAIN)
                .content("add 5 3"))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void testUnknownPath404() throws Exception {
        mockMvc.perform(get("/api/unknown"))
                .andExpect(status().isNotFound());
    }
}
