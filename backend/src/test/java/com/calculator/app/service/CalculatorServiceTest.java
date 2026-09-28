package com.calculator.app.service;

import com.calculator.app.dto.request.AddRequest;
import com.calculator.app.dto.request.DivideRequest;
import com.calculator.app.dto.request.ExpressionRequest;
import com.calculator.app.dto.request.MultiplyRequest;
import com.calculator.app.dto.request.PercentageRequest;
import com.calculator.app.dto.request.PowerRequest;
import com.calculator.app.dto.request.SqrtRequest;
import com.calculator.app.dto.request.SubtractRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CalculatorServiceTest {

    private final CalculatorService service = new CalculatorService();

    @Test
    void testBasicArithmetic() {
        assertEquals("8", service.evaluate(new AddRequest("5", "3")));
        assertEquals("0.3", service.evaluate(new AddRequest("0.1", "0.2")));
        assertEquals("6", service.evaluate(new SubtractRequest("10", "4")));
        assertEquals("42", service.evaluate(new MultiplyRequest("7", "6")));
        assertEquals("4", service.evaluate(new DivideRequest("20", "5")));
        assertEquals("0.5", service.evaluate(new PercentageRequest("50")));
    }

    @Test
    void testExpressionParsing() {
        assertEquals("-15", service.evaluate(new ExpressionRequest("-5 + -10")));
        assertEquals("15", service.evaluate(new ExpressionRequest("5 + 5 * 2")));
        assertEquals("20", service.evaluate(new ExpressionRequest("(5 + 5) * 2")));
        assertEquals("20", service.evaluate(new ExpressionRequest(" ( 5 + 5 ) * 2 ")));
    }

    @Test
    void testUnaryAndNegative() {
        assertEquals("-5", service.evaluate(new ExpressionRequest("-5")));
        assertEquals("-10", service.evaluate(new ExpressionRequest("5 * -2")));
        assertEquals("-5", service.evaluate(new ExpressionRequest("-10 / 2")));
        assertEquals("-15", service.evaluate(new ExpressionRequest("-5 + -10")));
        assertEquals("-5", service.evaluate(new ExpressionRequest("---5")));
        assertEquals("-4", service.evaluate(new ExpressionRequest("-2^2")));
        assertEquals("2", service.evaluate(new ExpressionRequest("--2")));
        assertEquals("0.5", service.evaluate(new ExpressionRequest("2^-1")));
        assertEquals("-6", service.evaluate(new ExpressionRequest("-2*3")));
    }

    @Test
    void testPowerAndSqrt() {
        assertEquals("8", service.evaluate(new PowerRequest("2", "3")));
        assertEquals("3", service.evaluate(new SqrtRequest("9")));
        assertEquals("0", service.evaluate(new SqrtRequest("0")));
        assertEquals("6", service.evaluate(new ExpressionRequest("√9*2")));
        assertEquals("1125899906842624", service.evaluate(new PowerRequest("2", "50")));
        assertEquals("12157665459056928801", service.evaluate(new PowerRequest("3", "40")));
        assertEquals("0.01", service.evaluate(new PowerRequest("0.1", "2")));
        assertEquals("8", service.evaluate(new ExpressionRequest("2 ^ 3.0")));
        assertEquals("512", service.evaluate(new ExpressionRequest("2^3^2")));
    }

    @Test
    void testErrors() {
        Exception e1 = assertThrows(IllegalArgumentException.class, () -> service.evaluate(new DivideRequest("10", "0")));
        assertEquals("cannot divide by zero", e1.getMessage());

        Exception e2 = assertThrows(IllegalArgumentException.class, () -> service.evaluate(new ExpressionRequest("10 / 0")));
        assertEquals("cannot divide by zero", e2.getMessage());

        Exception e3 = assertThrows(IllegalArgumentException.class, () -> service.evaluate(new ExpressionRequest("(5 + 5")));
        assertEquals("Mismatched parentheses", e3.getMessage());

        Exception e4 = assertThrows(IllegalArgumentException.class, () -> service.evaluate(new ExpressionRequest("5 + 5)")));
        assertEquals("Mismatched parentheses", e4.getMessage());

        Exception e5 = assertThrows(IllegalArgumentException.class, () -> service.evaluate(new SqrtRequest("-4")));
        assertEquals("Not a Real Number", e5.getMessage());

        Exception e6 = assertThrows(IllegalArgumentException.class, () -> service.evaluate(new PowerRequest("0", "-1")));
        assertEquals("cannot divide by zero", e6.getMessage());

        Exception e7 = assertThrows(IllegalArgumentException.class, () -> service.evaluate(new PowerRequest("2", "1001")));
        assertEquals("Exponent must be between -1000 and 1000", e7.getMessage());

        Exception e8 = assertThrows(IllegalArgumentException.class, () -> service.evaluate(new ExpressionRequest("2 3")));
        assertEquals("Invalid expression", e8.getMessage());

        Exception e9 = assertThrows(IllegalArgumentException.class, () -> service.evaluate(new ExpressionRequest("5 $ 3")));
        assertEquals("Invalid character: $", e9.getMessage());
        
        Exception e10 = assertThrows(IllegalArgumentException.class, () -> service.evaluate(new ExpressionRequest("   ")));
        assertEquals("Missing expression", e10.getMessage());

        Exception e11 = assertThrows(IllegalArgumentException.class, () -> service.evaluate(new PowerRequest("2", "4294967298")));
        assertEquals("Exponent must be between -1000 and 1000", e11.getMessage());
    }
}
