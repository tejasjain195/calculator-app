export type CalcRequest = 
  | { operator: 'add' | 'subtract' | 'multiply' | 'divide' | 'power'; operandA: string; operandB: string }
  | { operator: 'sqrt' | 'percentage'; operandA: string }
  | { operator: 'expression'; expression: string };

export type CalcResponse = {
  result?: string;
  error?: string;
};

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

export const calculateApi = async (payload: CalcRequest, signal: AbortSignal): Promise<CalcResponse> => {
  const response = await fetch(`${API_URL}/api/calculate`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    signal,
    body: JSON.stringify(payload)
  });
  return response.json();
};
