import React, { useState } from 'react';
import './Calculator.css';
import { calculateApi } from '../api/calculate';
import { buildPayload } from '../utils/buildPayload';

export default function Calculator() {
  const [expression, setExpression] = useState('');
  const [errorMsg, setErrorMsg] = useState('');
  const [shouldResetNext, setShouldResetNext] = useState(false);
  const [isCalculating, setIsCalculating] = useState(false);

  const evaluateExpression = async (expr: string): Promise<string | null> => {
    if (!expr.trim()) return null;
    setErrorMsg('');
    setIsCalculating(true);

    let timeoutId: ReturnType<typeof setTimeout> | undefined;
    try {
      const controller = new AbortController();
      timeoutId = setTimeout(() => controller.abort(), 5000);

      const payload = buildPayload(expr);
      const data = await calculateApi(payload, controller.signal);
      
      if (data.error) {
        setErrorMsg(data.error);
        return null;
      }
      return data.result ? String(data.result) : null;
    } catch {
      setErrorMsg('Network or Timeout Error');
      return null;
    } finally {
      clearTimeout(timeoutId);
      setIsCalculating(false);
    }
  };

  const handleAppend = async (val: string) => {
    if (isCalculating) return;
    setErrorMsg('');
    const isOperator = ['+', '-', '*', '/', '^'].includes(val);

    let currentExpr = expression;
    if (shouldResetNext && !isOperator) {
      currentExpr = val === '.' ? '0.' : val;
      setExpression(currentExpr);
      setShouldResetNext(false);
      return;
    }
    setShouldResetNext(false);

    // Operator replacing operator
    if (isOperator) {
      const trimmed = currentExpr.trim();
      const lastChar = trimmed.slice(-1);
      if (['+', '-', '*', '/', '^'].includes(lastChar)) {
        currentExpr = trimmed.slice(0, -1).trim();
      }
    }

    // Ignore second .
    if (val === '.') {
      const tokens = currentExpr.split(/[\s()√]+/);
      const currentToken = tokens[tokens.length - 1] || '';
      if (currentToken.includes('.')) return;
    }

    // Ignore unmatched )
    if (val === ')') {
      const openCount = (currentExpr.match(/\(/g) || []).length;
      const closeCount = (currentExpr.match(/\)/g) || []).length;
      if (closeCount >= openCount) return;
    }

    // Auto-calculate if we are appending a new operator and we already have 3 parts without brackets
    if (isOperator && !currentExpr.includes('(') && !currentExpr.includes(')')) {
      const parts = currentExpr.trim().split(/\s+/);
      if (parts.length === 3 && parts[2] !== '') {
        const opMap: Record<string, boolean> = { '+': true, '-': true, '*': true, '/': true, '^': true };
        if (opMap[parts[1]]) {
          const result = await evaluateExpression(currentExpr);
          if (result !== null) {
            setExpression(result + ' ' + val + ' ');
            return;
          } else {
            return;
          }
        }
      }
    }

    if (isOperator) {
      setExpression(currentExpr + (currentExpr.endsWith(' ') ? '' : ' ') + val + ' ');
    } else if (val === '√') {
      setExpression(currentExpr + '√');
    } else {
      setExpression(currentExpr + val);
    }
  };

  const handlePercentage = async () => {
    if (isCalculating) return;
    
    const tokens = expression.split(/[\s()√]+/);
    const currentToken = tokens[tokens.length - 1];
    if (!currentToken || !/^-?(\d+(\.\d*)?|\.\d+)$/.test(currentToken)) return;

    setErrorMsg('');
    setIsCalculating(true);

    let timeoutId: ReturnType<typeof setTimeout> | undefined;
    try {
      const controller = new AbortController();
      timeoutId = setTimeout(() => controller.abort(), 5000);
      
      const response = await calculateApi({ operator: 'percentage', operandA: currentToken }, controller.signal);
      
      if (response.error) {
        setErrorMsg(response.error);
      } else if (response.result) {
        const lastIndex = expression.lastIndexOf(currentToken);
        setExpression(expression.slice(0, lastIndex) + response.result);
      }
    } catch {
      setErrorMsg('Network or Timeout Error');
    } finally {
      clearTimeout(timeoutId);
      setIsCalculating(false);
    }
  };

  const handleEquals = async () => {
    if (isCalculating) return;
    const trimmed = expression.trim();
    if (!trimmed) return;
    const lastChar = trimmed.slice(-1);
    if (['+', '-', '*', '/', '^'].includes(lastChar)) return;

    const result = await evaluateExpression(expression);
    if (result !== null) {
      setExpression(result);
      setShouldResetNext(true);
    }
  };

  const clearCurrent = () => {
    if (isCalculating) return;
    setErrorMsg('');
    setExpression(prev => prev.trim().slice(0, -1).trim());
  };

  const clearAll = () => {
    if (isCalculating) return;
    setExpression('');
    setErrorMsg('');
    setShouldResetNext(false);
  };

  return (
    <div className="calculator">
      <div className="display">
        {errorMsg && <div className="error" role="alert">{errorMsg}</div>}
        {isCalculating && <div className="calculating">Calculating...</div>}
        <div className="current">{expression || '0'}</div>
      </div>
      <div className="buttons">
        <button onClick={clearCurrent} className="btn-clear" disabled={isCalculating} aria-label="Clear current">C</button>
        <button onClick={clearAll} className="btn-clear" disabled={isCalculating} aria-label="Clear all">AC</button>
        <button onClick={() => handleAppend('(')} className="btn-op" disabled={isCalculating} aria-label="Open parenthesis">(</button>
        <button onClick={() => handleAppend(')')} className="btn-op" disabled={isCalculating} aria-label="Close parenthesis">)</button>

        <button onClick={() => handleAppend('7')} disabled={isCalculating}>7</button>
        <button onClick={() => handleAppend('8')} disabled={isCalculating}>8</button>
        <button onClick={() => handleAppend('9')} disabled={isCalculating}>9</button>
        <button onClick={() => handleAppend('/')} className="btn-op" disabled={isCalculating} aria-label="Divide">÷</button>

        <button onClick={() => handleAppend('4')} disabled={isCalculating}>4</button>
        <button onClick={() => handleAppend('5')} disabled={isCalculating}>5</button>
        <button onClick={() => handleAppend('6')} disabled={isCalculating}>6</button>
        <button onClick={() => handleAppend('*')} className="btn-op" disabled={isCalculating} aria-label="Multiply">×</button>

        <button onClick={() => handleAppend('1')} disabled={isCalculating}>1</button>
        <button onClick={() => handleAppend('2')} disabled={isCalculating}>2</button>
        <button onClick={() => handleAppend('3')} disabled={isCalculating}>3</button>
        <button onClick={() => handleAppend('-')} className="btn-op" disabled={isCalculating} aria-label="Subtract">−</button>

        <button onClick={() => handleAppend('.')} disabled={isCalculating} aria-label="Decimal">.</button>
        <button onClick={() => handleAppend('0')} disabled={isCalculating}>0</button>
        <button onClick={() => handleAppend('^')} className="btn-op" disabled={isCalculating} aria-label="Power">x^y</button>
        <button onClick={() => handleAppend('+')} className="btn-op" disabled={isCalculating} aria-label="Add">+</button>

        <button onClick={() => handleAppend('√')} className="btn-op" disabled={isCalculating} aria-label="Square Root">√</button>
        <button onClick={handlePercentage} className="btn-op" disabled={isCalculating} aria-label="Percentage">%</button>
        <button onClick={handleEquals} className="btn-eq span-2" disabled={isCalculating} aria-label="Equals">=</button>
      </div>
    </div>
  );
}
