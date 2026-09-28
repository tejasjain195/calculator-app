// @ts-nocheck
/// <reference types="@testing-library/jest-dom" />
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import Calculator from './Calculator';
import '@testing-library/jest-dom';

// Mock fetch
globalThis.fetch = vi.fn();

describe('Calculator UI & State', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    (globalThis.fetch as any).mockReset();
  });

  const mockFetchSuccess = (result: string) => {
    (globalThis.fetch as any).mockResolvedValueOnce({
      ok: true,
      json: async () => ({ result })
    });
  };

  const mockFetchError = (error: string) => {
    (globalThis.fetch as any).mockResolvedValueOnce({
      ok: false,
      json: async () => ({ error })
    });
  };

  const clickButton = (text: string) => {
    fireEvent.click(screen.getByText(text, { selector: 'button' }));
  };

  it('renders initial state', () => {
    render(<Calculator />);
    expect(screen.getByText('0', { selector: '.current' })).toBeInTheDocument();
  });

  it('builds expression string correctly', () => {
    render(<Calculator />);
    clickButton('5');
    clickButton('+');
    clickButton('5');
    expect(screen.getByText('5 + 5', { selector: '.current' })).toBeInTheDocument();
  });

  it('auto-calculates on consecutive operators and replaces consecutive operators', async () => {
    mockFetchSuccess('10');
    render(<Calculator />);
    clickButton('5');
    clickButton('+');
    clickButton('5');
    clickButton('×');

    await waitFor(() => {
      expect(globalThis.fetch).toHaveBeenCalledWith(expect.any(String), expect.objectContaining({
        body: JSON.stringify({ operator: 'add', operandA: '5', operandB: '5' })
      }));
      expect(screen.getByText('10 *', { selector: '.current' })).toBeInTheDocument();
    });

    // Replace operator
    clickButton('−');
    expect(screen.getByText('10 -', { selector: '.current' })).toBeInTheDocument();
  });

  it('blocks double dots', () => {
    render(<Calculator />);
    clickButton('5');
    clickButton('.');
    clickButton('5');
    clickButton('.'); // Should be ignored
    expect(screen.getByText('5.5', { selector: '.current' })).toBeInTheDocument();
  });

  it('handles percentage button instantly', async () => {
    mockFetchSuccess('0.5');
    render(<Calculator />);
    clickButton('5');
    clickButton('0');
    clickButton('%');

    await waitFor(() => {
      expect(globalThis.fetch).toHaveBeenCalledWith(expect.any(String), expect.objectContaining({
        body: JSON.stringify({ operator: 'percentage', operandA: '50' })
      }));
      expect(screen.getByText('0.5', { selector: '.current' })).toBeInTheDocument();
    });
  });

  it('sends simple binary payload', async () => {
    mockFetchSuccess('8');
    render(<Calculator />);
    clickButton('5');
    clickButton('+');
    clickButton('3');
    clickButton('=');

    await waitFor(() => {
      expect(globalThis.fetch).toHaveBeenCalledWith(expect.any(String), expect.objectContaining({
        body: JSON.stringify({ operator: 'add', operandA: '5', operandB: '3' })
      }));
      expect(screen.getByText('8', { selector: '.current' })).toBeInTheDocument();
    });
  });

  it('handles parenthesis', async () => {
    mockFetchSuccess('20');
    render(<Calculator />);
    clickButton('(');
    clickButton('5');
    clickButton('+');
    clickButton('5');
    clickButton(')');
    clickButton('×');
    clickButton('2');
    clickButton('=');

    await waitFor(() => {
      expect(globalThis.fetch).toHaveBeenCalledWith(expect.any(String), expect.objectContaining({
        body: JSON.stringify({ operator: 'expression', expression: '(5 + 5) * 2' })
      }));
      expect(screen.getByText('20', { selector: '.current' })).toBeInTheDocument();
    });
  });

  it('renders backend error cleanly', async () => {
    mockFetchError('cannot divide by zero');
    render(<Calculator />);
    clickButton('5');
    clickButton('÷');
    clickButton('0');
    clickButton('=');

    await waitFor(() => {
      expect(screen.getByText('cannot divide by zero', { selector: '.error' })).toBeInTheDocument();
    });
  });

  it('disables buttons during calculating state', async () => {
    // Return a never-resolving promise to simulate loading
    (globalThis.fetch as any).mockImplementationOnce(() => new Promise(() => {}));
    
    render(<Calculator />);
    clickButton('5');
    clickButton('+');
    clickButton('3');
    clickButton('=');

    await waitFor(() => {
      expect(screen.getByText('Calculating...')).toBeInTheDocument();
    });

    const buttons = screen.getAllByRole('button');
    buttons.forEach(button => {
      expect(button).toBeDisabled();
    });
  });
});
