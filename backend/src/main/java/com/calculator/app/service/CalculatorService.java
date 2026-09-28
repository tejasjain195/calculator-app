package com.calculator.app.service;

import com.calculator.app.dto.request.AddRequest;
import com.calculator.app.dto.request.CalcOperationRequest;
import com.calculator.app.dto.request.DivideRequest;
import com.calculator.app.dto.request.ExpressionRequest;
import com.calculator.app.dto.request.MultiplyRequest;
import com.calculator.app.dto.request.PercentageRequest;
import com.calculator.app.dto.request.PowerRequest;
import com.calculator.app.dto.request.SqrtRequest;
import com.calculator.app.dto.request.SubtractRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

@Service
public class CalculatorService {

    private static final MathContext MC = MathContext.DECIMAL128;

    private enum TokenType { NUMBER, OPERATOR, LEFT_PAREN, RIGHT_PAREN }

    private record Token(TokenType type, String value) {}

    public String evaluate(CalcOperationRequest request) {
        BigDecimal result;
        // A sealed exhaustive switch is the preferred Java 21 upgrade path.
        if (request instanceof AddRequest r) {
            result = parse(r.operandA()).add(parse(r.operandB()), MC);
        } else if (request instanceof SubtractRequest r) {
            result = parse(r.operandA()).subtract(parse(r.operandB()), MC);
        } else if (request instanceof MultiplyRequest r) {
            result = parse(r.operandA()).multiply(parse(r.operandB()), MC);
        } else if (request instanceof DivideRequest r) {
            BigDecimal b = parse(r.operandB());
            if (b.compareTo(BigDecimal.ZERO) == 0) {
                throw new IllegalArgumentException("cannot divide by zero");
            }
            result = parse(r.operandA()).divide(b, MC);
        } else if (request instanceof PowerRequest r) {
            result = calculatePower(parse(r.operandA()), parse(r.operandB()));
        } else if (request instanceof SqrtRequest r) {
            BigDecimal a = parse(r.operandA());
            if (a.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Not a Real Number");
            }
            result = a.sqrt(MC);
        } else if (request instanceof PercentageRequest r) {
            result = parse(r.operandA()).divide(BigDecimal.valueOf(100), MC);
        } else if (request instanceof ExpressionRequest r) {
            return evaluateShuntingYard(r.expression());
        } else {
            throw new IllegalStateException("Unsupported request type: " + request.getClass());
        }
        
        checkRange(result);
        return format(result);
    }

    private BigDecimal parse(String val) {
        return new BigDecimal(val);
    }

    private String format(BigDecimal val) {
        return val.stripTrailingZeros().toPlainString();
    }

    private void checkRange(BigDecimal value) {
        if (value.compareTo(BigDecimal.ZERO) != 0) {
            if (Math.abs(value.precision() - value.scale() - 1) > 100) {
                throw new IllegalArgumentException("Result out of supported range");
            }
        }
    }

    private BigDecimal calculatePower(BigDecimal base, BigDecimal exponent) {
        if (exponent.remainder(BigDecimal.ONE).signum() == 0) {
            if (exponent.abs().compareTo(BigDecimal.valueOf(1000)) > 0) {
                throw new IllegalArgumentException("Exponent must be between -1000 and 1000");
            }
            int exp = exponent.intValueExact();
            if (exp < 0) {
                BigDecimal pow = base.pow(-exp, MC);
                if (pow.compareTo(BigDecimal.ZERO) == 0) {
                    throw new IllegalArgumentException("cannot divide by zero");
                }
                return BigDecimal.ONE.divide(pow, MC);
            }
            return base.pow(exp, MC);
        } else {
            double fres = Math.pow(base.doubleValue(), exponent.doubleValue());
            if (Double.isNaN(fres)) {
                throw new IllegalArgumentException("Not a Real Number");
            }
            if (Double.isInfinite(fres)) {
                throw new IllegalArgumentException("Result is Infinity");
            }
            return BigDecimal.valueOf(fres);
        }
    }

    private String evaluateShuntingYard(String expr) {
        if (expr == null || expr.trim().isEmpty()) {
            throw new IllegalArgumentException("Missing expression");
        }
        
        List<Token> tokens = tokenize(expr);
        List<Token> rpn = toRPN(tokens);
        return evaluateRPN(rpn);
    }

    private List<Token> tokenize(String expr) {
        List<Token> tokens = new ArrayList<>();
        int i = 0;
        while (i < expr.length()) {
            char ch = expr.charAt(i);

            if (Character.isWhitespace(ch)) {
                i++;
                continue;
            }

            if (Character.isDigit(ch) || ch == '.') {
                int start = i;
                boolean hasDot = false;
                while (i < expr.length()) {
                    char c = expr.charAt(i);
                    if (Character.isDigit(c)) {
                        i++;
                    } else if (c == '.' && !hasDot) {
                        hasDot = true;
                        i++;
                    } else {
                        break;
                    }
                }
                tokens.add(new Token(TokenType.NUMBER, expr.substring(start, i)));
                continue;
            }

            if (ch == '(') {
                tokens.add(new Token(TokenType.LEFT_PAREN, "("));
                i++;
                continue;
            }
            if (ch == ')') {
                tokens.add(new Token(TokenType.RIGHT_PAREN, ")"));
                i++;
                continue;
            }

            if (ch == '+' || ch == '-' || ch == '*' || ch == '/' || ch == '^' || ch == '√') {
                if (ch == '-') {
                    boolean isUnary = false;
                    if (tokens.isEmpty()) {
                        isUnary = true;
                    } else {
                        Token prev = tokens.get(tokens.size() - 1);
                        if (prev.type() == TokenType.OPERATOR || prev.type() == TokenType.LEFT_PAREN) {
                            isUnary = true;
                        }
                    }
                    if (isUnary) {
                        tokens.add(new Token(TokenType.OPERATOR, "u-"));
                        i++;
                        continue;
                    }
                }
                tokens.add(new Token(TokenType.OPERATOR, String.valueOf(ch)));
                i++;
                continue;
            }

            throw new IllegalArgumentException("Invalid character: " + ch);
        }

        return tokens;
    }

    private int getPrecedence(String op) {
        switch (op) {
            case "+": case "-": return 1;
            case "*": case "/": return 2;
            case "u-": return 3;
            case "^": return 4;
            case "√": return 5;
            default: return 0;
        }
    }

    private boolean isLeftAssociative(String op) {
        return op.equals("+") || op.equals("-") || op.equals("*") || op.equals("/");
    }

    private boolean isPrefix(String op) {
        return op.equals("u-") || op.equals("√");
    }

    private List<Token> toRPN(List<Token> tokens) {
        List<Token> output = new ArrayList<>();
        Deque<Token> ops = new ArrayDeque<>();

        for (Token t : tokens) {
            switch (t.type()) {
                case NUMBER:
                    output.add(t);
                    break;
                case LEFT_PAREN:
                    ops.push(t);
                    break;
                case RIGHT_PAREN:
                    boolean found = false;
                    while (!ops.isEmpty()) {
                        Token top = ops.pop();
                        if (top.type() == TokenType.LEFT_PAREN) {
                            found = true;
                            break;
                        }
                        output.add(top);
                    }
                    if (!found) throw new IllegalArgumentException("Mismatched parentheses");
                    break;
                case OPERATOR:
                    if (isPrefix(t.value())) {
                        ops.push(t);
                    } else {
                        while (!ops.isEmpty()) {
                            Token top = ops.peek();
                            if (top.type() == TokenType.LEFT_PAREN) break;
                            
                            int precedenceTop = getPrecedence(top.value());
                            int precedenceT = getPrecedence(t.value());
                            
                            if (precedenceTop > precedenceT || (precedenceTop == precedenceT && isLeftAssociative(t.value()))) {
                                output.add(ops.pop());
                            } else {
                                break;
                            }
                        }
                        ops.push(t);
                    }
                    break;
            }
        }

        while (!ops.isEmpty()) {
            Token top = ops.pop();
            if (top.type() == TokenType.LEFT_PAREN) throw new IllegalArgumentException("Mismatched parentheses");
            output.add(top);
        }

        return output;
    }

    private String evaluateRPN(List<Token> rpn) {
        Deque<BigDecimal> stack = new ArrayDeque<>();

        for (Token t : rpn) {
            if (t.type() == TokenType.NUMBER) {
                try {
                    stack.push(parse(t.value()));
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid number format");
                }
            } else if (t.type() == TokenType.OPERATOR) {
                BigDecimal result;
                if (t.value().equals("u-")) {
                    if (stack.isEmpty()) throw new IllegalArgumentException("Invalid expression");
                    result = stack.pop().negate();
                } else if (t.value().equals("√")) {
                    if (stack.isEmpty()) throw new IllegalArgumentException("Invalid expression");
                    BigDecimal a = stack.pop();
                    if (a.compareTo(BigDecimal.ZERO) < 0) {
                        throw new IllegalArgumentException("Not a Real Number");
                    }
                    result = a.sqrt(MC);
                } else {
                    if (stack.size() < 2) throw new IllegalArgumentException("Invalid expression");
                    BigDecimal b = stack.pop();
                    BigDecimal a = stack.pop();
                    
                    switch (t.value()) {
                        case "+":
                            result = a.add(b, MC);
                            break;
                        case "-":
                            result = a.subtract(b, MC);
                            break;
                        case "*":
                            result = a.multiply(b, MC);
                            break;
                        case "/":
                            if (b.compareTo(BigDecimal.ZERO) == 0) {
                                throw new IllegalArgumentException("cannot divide by zero");
                            }
                            result = a.divide(b, MC);
                            break;
                        case "^":
                            result = calculatePower(a, b);
                            break;
                        default:
                            throw new IllegalArgumentException("Unsupported operator");
                    }
                }
                checkRange(result);
                stack.push(result);
            }
        }

        if (stack.size() != 1) {
            throw new IllegalArgumentException("Invalid expression");
        }

        return format(stack.pop());
    }
}
