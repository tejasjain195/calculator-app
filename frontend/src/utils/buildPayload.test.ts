import { describe, it, expect } from 'vitest';
import { buildPayload } from './buildPayload';

describe('buildPayload', () => {
  it('handles basic numbers', () => {
    expect(buildPayload('5 + 3')).toEqual({ operator: 'add', operandA: '5', operandB: '3' });
    expect(buildPayload('10 * 2')).toEqual({ operator: 'multiply', operandA: '10', operandB: '2' });
  });

  it('handles negative numbers', () => {
    expect(buildPayload('-5 + -3')).toEqual({ operator: 'add', operandA: '-5', operandB: '-3' });
    expect(buildPayload('10 * -2')).toEqual({ operator: 'multiply', operandA: '10', operandB: '-2' });
  });

  it('handles decimals', () => {
    expect(buildPayload('5.5 + .3')).toEqual({ operator: 'add', operandA: '5.5', operandB: '.3' });
    expect(buildPayload('-0.5 * 2.0')).toEqual({ operator: 'multiply', operandA: '-0.5', operandB: '2.0' });
  });

  it('handles pure sqrt', () => {
    expect(buildPayload('√9')).toEqual({ operator: 'sqrt', operandA: '9' });
    expect(buildPayload('√-9')).toEqual({ operator: 'sqrt', operandA: '-9' });
    expect(buildPayload('√ 16')).toEqual({ operator: 'sqrt', operandA: '16' });
  });

  it('falls back to expression if sqrt is mid-expression', () => {
    expect(buildPayload('5 + √9')).toEqual({ operator: 'expression', expression: '5 + √9' });
    expect(buildPayload('√9 + 1')).toEqual({ operator: 'expression', expression: '√9 + 1' });
  });

  it('falls back to expression if brackets exist', () => {
    expect(buildPayload('(5 + 3) * 2')).toEqual({ operator: 'expression', expression: '(5 + 3) * 2' });
    expect(buildPayload('5 + (3 * 2)')).toEqual({ operator: 'expression', expression: '5 + (3 * 2)' });
  });

  it('falls back to expression if operands are not simple numbers', () => {
    expect(buildPayload('5 + 3+2')).toEqual({ operator: 'expression', expression: '5 + 3+2' });
    expect(buildPayload('5.5.5 + 3')).toEqual({ operator: 'expression', expression: '5.5.5 + 3' });
    expect(buildPayload('a + b')).toEqual({ operator: 'expression', expression: 'a + b' });
  });
});
