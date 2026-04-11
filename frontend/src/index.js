import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App';

// Global reset
const style = document.createElement('style');
style.textContent = `
  *, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }
  body {
    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, sans-serif;
    background: #f8fafc;
    color: #1e293b;
    line-height: 1.6;
  }
  h1,h2,h3,h4,h5 { line-height: 1.3; }
  a { color: inherit; }
  button { font-family: inherit; }
  input, select, textarea { font-family: inherit; }
`;
document.head.appendChild(style);

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(<React.StrictMode><App /></React.StrictMode>);
