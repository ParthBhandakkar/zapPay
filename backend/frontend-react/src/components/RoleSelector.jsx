import React from 'react';
import './RoleSelector.css';

function RoleSelector({ onSelectRole }) {
  // Check if returning to role selection
  const hasExistingData = localStorage.getItem('zappay.customer.tokens') || localStorage.getItem('zappay.pump.tokens');
  
  return (
    <div className="role-selector-container">
      <div className="role-selector-card">
        <h1 className="app-title">ZapPay</h1>
        <p className="subtitle">Fast, Secure Fuel Payments</p>
        
        {hasExistingData && (
          <div style={{
            padding: '0.75rem',
            backgroundColor: '#fff3cd',
            border: '1px solid #ffc107',
            borderRadius: '8px',
            marginBottom: '1rem',
            fontSize: '0.9rem',
            color: '#856404'
          }}>
            💡 You previously logged in. Select your role to continue.
          </div>
        )}
        
        <h2>Select Your Role</h2>
        <div className="role-buttons">
          <button
            className="role-btn customer"
            onClick={() => onSelectRole('customer')}
          >
            <span className="role-icon">👤</span>
            <span className="role-label">Customer</span>
            <span className="role-desc">Generate QR & Pay</span>
          </button>
          
          <button
            className="role-btn pump"
            onClick={() => onSelectRole('pump')}
          >
            <span className="role-icon">⛽</span>
            <span className="role-label">Pump Operator</span>
            <span className="role-desc">Scan & Process</span>
          </button>
        </div>
      </div>
    </div>
  );
}

export default RoleSelector;

