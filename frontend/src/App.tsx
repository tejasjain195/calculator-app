import React from 'react';
import Calculator from './components/Calculator';
import { ErrorBoundary } from './components/ErrorBoundary';

function App() {
  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
      <ErrorBoundary>
        <Calculator />
      </ErrorBoundary>
    </div>
  );
}

export default App;
