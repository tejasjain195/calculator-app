import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import Calculator from './Calculator';
import '@testing-library/jest-dom';

// Mock fetch
global.fetch = vi.fn();

describe('Calculator UI & State', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    (global.fetch as any).mockReset();
  });

  const mockFetchSuccess = (result: number) => {
    (global.fetch as any).mockResolvedValueOnce({
      ok: true,
      json: async () => ({ result })
    });
  };

  const mockFetchError = (error: string) => {
    (global.fetch as any).mockResolvedValueOnce({
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

  it('handles double decimals gracefully (ignores second decimal)', () => {
    render(<Calculator />);
    clickButton('5');
    clickButton('.');
    clickButton('.');
    clickButton('5');
    expect(screen.getByText('5.5', { selector: '.current' })).toBeInTheDocument();
  });

  it('blocks inputs exceeding max characters', () => {
    render(<Calculator />);
    for (let i = 0; i < 20; i++) {
      clickButton('1');
    }
    expect(screen.getByText('111111111111111', { selector: '.current' })).toBeInTheDocument();
  });

  it('performs successive operations (5 + 5 * -> triggers API)', async () => {
    mockFetchSuccess(10); // for 5+5
    render(<Calculator />);
    clickButton('5');
    clickButton('+');
    clickButton('5');
    clickButton('×');

    await waitFor(() => {
      expect(global.fetch).toHaveBeenCalledWith(expect.any(String), expect.objectContaining({
        body: JSON.stringify({ a: 5, b: 5, operation: 'add' })
      }));
    });
    
    expect(screen.getByText('10 *', { selector: '.previous' })).toBeInTheDocument();
    expect(screen.getByText('0', { selector: '.current' })).toBeInTheDocument();
  });

  it('handles repeated equals correctly', async () => {
    mockFetchSuccess(7); // 5+2
    mockFetchSuccess(9); // 7+2
    
    render(<Calculator />);
    clickButton('5');
    clickButton('+');
    clickButton('2');
    clickButton('=');

    await waitFor(() => {
      expect(screen.getByText('7', { selector: '.current' })).toBeInTheDocument();
    });

    clickButton('=');

    await waitFor(() => {
      expect(global.fetch).toHaveBeenLastCalledWith(expect.any(String), expect.objectContaining({
        body: JSON.stringify({ a: 7, b: 2, operation: 'add' })
      }));
      expect(screen.getByText('9', { selector: '.current' })).toBeInTheDocument();
    });
  });

  it('C clears current, AC clears all', () => {
    render(<Calculator />);
    clickButton('5');
    clickButton('+');
    clickButton('2');
    
    // C only clears '2'
    clickButton('C');
    expect(screen.getByText('0', { selector: '.current' })).toBeInTheDocument();
    expect(screen.getByText('5 +', { selector: '.previous' })).toBeInTheDocument();

    // AC clears everything
    clickButton('AC');
    expect(screen.getByText('0', { selector: '.current' })).toBeInTheDocument();
    expect(screen.queryByText('5 +', { selector: '.previous' })).toBeNull();
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
      // Should reset display after error
      expect(screen.getByText('0', { selector: '.current' })).toBeInTheDocument();
    });
  });

  it('handles unary operation (sqrt)', async () => {
    mockFetchSuccess(3);
    render(<Calculator />);
    clickButton('9');
    clickButton('√');

    await waitFor(() => {
      expect(global.fetch).toHaveBeenCalledWith(expect.any(String), expect.objectContaining({
        body: JSON.stringify({ a: 9, b: 0, operation: 'sqrt' })
      }));
      expect(screen.getByText('3', { selector: '.current' })).toBeInTheDocument();
    });
  });
});
