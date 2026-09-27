import React, { useState } from 'react';
import './Calculator.css';

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';
const MAX_DIGITS = 15;

export default function Calculator() {
  const [currentVal, setCurrentVal] = useState('0');
  const [previousVal, setPreviousVal] = useState('');
  const [operation, setOperation] = useState('');
  const [errorMsg, setErrorMsg] = useState('');
  const [shouldResetNext, setShouldResetNext] = useState(false);
  const [lastEqVal, setLastEqVal] = useState('');
  const [lastEqOp, setLastEqOp] = useState('');

  const handleNumber = (num: string) => {
    setErrorMsg('');
    setLastEqVal('');
    setLastEqOp('');

    if (shouldResetNext) {
      setCurrentVal(num === '.' ? '0.' : num);
      setShouldResetNext(false);
      return;
    }

    if (num === '.' && currentVal.includes('.')) return;
    if (currentVal.replace(/[^0-9]/g, '').length >= MAX_DIGITS) return;

    if (currentVal === '0' && num !== '.') {
      setCurrentVal(num);
    } else {
      setCurrentVal(prev => prev + num);
    }
  };

  const calculate = async (a: string, b: string, op: string) => {
    let opStr = '';
    switch (op) {
      case '+': opStr = 'add'; break;
      case '-': opStr = 'subtract'; break;
      case '*': opStr = 'multiply'; break;
      case '/': opStr = 'divide'; break;
      case '^': opStr = 'power'; break;
      case '√': opStr = 'sqrt'; break;
      default: return null;
    }

    try {
      // Setup AbortController for network resilience
      const controller = new AbortController();
      const timeoutId = setTimeout(() => controller.abort(), 5000);

      const response = await fetch(`${API_URL}/api/calculate`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        signal: controller.signal,
        body: JSON.stringify({
          a: parseFloat(a),
          b: opStr === 'sqrt' ? 0 : parseFloat(b),
          operation: opStr
        })
      });
      clearTimeout(timeoutId);

      const data = await response.json();
      
      if (!response.ok || data.error) {
        setErrorMsg(data.error || 'API Error');
        setCurrentVal('0');
        setPreviousVal('');
        setOperation('');
        setShouldResetNext(true);
        return null;
      }
      return String(data.result);
    } catch (err) {
      setErrorMsg('Network or Timeout Error');
      return null;
    }
  };

  const handleOperation = async (op: string) => {
    setErrorMsg('');
    setLastEqVal('');
    setLastEqOp('');
    
    // Unary operations execute immediately on currentVal
    if (op === '√') {
      const res = await calculate(currentVal, '0', op);
      if (res !== null) {
        setCurrentVal(res);
        setShouldResetNext(true);
      }
      return;
    }

    // Binary operations
    if (operation && previousVal && !shouldResetNext) {
      // Evaluate successive operations like 5 + 5 * immediately
      const res = await calculate(previousVal, currentVal, operation);
      if (res !== null) {
        setPreviousVal(res);
        setOperation(op);
        setCurrentVal('0');
        setShouldResetNext(true);
      }
    } else {
      setPreviousVal(currentVal);
      setOperation(op);
      setCurrentVal('0');
      setShouldResetNext(false);
    }
  };

  const handleEquals = async () => {
    if (errorMsg) setErrorMsg('');

    let a = previousVal;
    let b = currentVal;
    let op = operation;

    // Repeated equals handling
    if (!operation && lastEqOp && lastEqVal) {
      a = currentVal;
      b = lastEqVal;
      op = lastEqOp;
    } else if (operation && previousVal) {
      setLastEqOp(operation);
      setLastEqVal(currentVal);
    } else {
      return;
    }

    const res = await calculate(a, b, op);
    if (res !== null) {
      setCurrentVal(res);
      setPreviousVal('');
      setOperation('');
      setShouldResetNext(true);
    }
  };

  const clearCurrent = () => {
    setCurrentVal('0');
    setErrorMsg('');
  };

  const clearAll = () => {
    setCurrentVal('0');
    setPreviousVal('');
    setOperation('');
    setErrorMsg('');
    setShouldResetNext(false);
    setLastEqOp('');
    setLastEqVal('');
  };

  return (
    <div className="calculator">
      <div className="display">
        <div className="error">{errorMsg}</div>
        <div className="previous">{previousVal} {operation}</div>
        <div className="current">{currentVal}</div>
      </div>
      <div className="buttons">
        <button onClick={clearCurrent} className="btn-clear">C</button>
        <button onClick={clearAll} className="btn-clear">AC</button>
        <button onClick={() => handleOperation('^')} className="btn-op">x^y</button>
        <button onClick={() => handleOperation('/')} className="btn-op">÷</button>

        <button onClick={() => handleNumber('7')}>7</button>
        <button onClick={() => handleNumber('8')}>8</button>
        <button onClick={() => handleNumber('9')}>9</button>
        <button onClick={() => handleOperation('*')} className="btn-op">×</button>

        <button onClick={() => handleNumber('4')}>4</button>
        <button onClick={() => handleNumber('5')}>5</button>
        <button onClick={() => handleNumber('6')}>6</button>
        <button onClick={() => handleOperation('-')} className="btn-op">−</button>

        <button onClick={() => handleNumber('1')}>1</button>
        <button onClick={() => handleNumber('2')}>2</button>
        <button onClick={() => handleNumber('3')}>3</button>
        <button onClick={() => handleOperation('+')} className="btn-op">+</button>

        <button onClick={() => handleOperation('√')} className="btn-op span-2">√</button>
        <button onClick={() => handleNumber('0')}>0</button>
        <button onClick={() => handleNumber('.')}>.</button>
        <button onClick={handleEquals} className="btn-eq span-4">=</button>
      </div>
    </div>
  );
}
