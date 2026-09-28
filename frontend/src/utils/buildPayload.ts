import { CalcRequest } from '../api/calculate';

const numberPattern = /^-?(\d+(\.\d*)?|\.\d+)$/;

export const buildPayload = (expr: string): CalcRequest => {
  const trimmed = expr.trim();
  
  if (trimmed.startsWith('√')) {
    const inner = trimmed.substring(1).trim();
    if (numberPattern.test(inner)) {
      return { operator: 'sqrt', operandA: inner };
    }
  }

  if (!trimmed.includes('(') && !trimmed.includes(')')) {
    const parts = trimmed.split(/\s+/);
    if (parts.length === 3) {
      const [a, op, b] = parts;
      if (numberPattern.test(a) && numberPattern.test(b)) {
        const opMap: Record<string, string> = {
          '+': 'add',
          '−': 'subtract',
          '-': 'subtract',
          '×': 'multiply',
          '*': 'multiply',
          '÷': 'divide',
          '/': 'divide',
          '^': 'power'
        };
        const mappedOp = opMap[op];
        if (mappedOp) {
          return {
            operator: mappedOp as 'add' | 'subtract' | 'multiply' | 'divide' | 'power',
            operandA: a,
            operandB: b
          };
        }
      }
    }
  }

  return { operator: 'expression', expression: trimmed };
};
