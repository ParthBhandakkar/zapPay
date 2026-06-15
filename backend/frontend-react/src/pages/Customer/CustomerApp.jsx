import { useState, useEffect } from 'react';
import { Routes, Route, NavLink, useNavigate, Navigate } from 'react-router-dom';
import './Customer.css';
import Auth from './Auth';
import Dashboard from './Dashboard';
import Wallet from './Wallet';
import QRGenerate from './QRGenerate';
import Profile from './Profile';
import KYC from './KYC';
import Transactions from './Transactions';

function CustomerApp({ onRoleReset }) {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    const tokens = localStorage.getItem('zappay.customer.tokens');
    setIsAuthenticated(!!tokens);
  }, []);

  const handleLogin = (tokens) => {
    localStorage.setItem('zappay.customer.tokens', JSON.stringify(tokens));
    setIsAuthenticated(true);
    navigate('dashboard');
  };

  const handleLogout = () => {
    localStorage.removeItem('zappay.customer.tokens');
    setIsAuthenticated(false);
    onRoleReset(); // Reset role and go to role selector
  };

  if (!isAuthenticated) {
    return (
      <div className="customer-app">
        <Auth onLogin={handleLogin} />
      </div>
    );
  }

  return (
    <div className="customer-app">
      <nav className="app-nav">
        <div className="nav-brand">ZapPay Customer</div>
        <div className="nav-links">
          <NavLink to="dashboard" className={({ isActive }) => isActive ? 'active' : ''}>Dashboard</NavLink>
          <NavLink to="wallet" className={({ isActive }) => isActive ? 'active' : ''}>Wallet</NavLink>
          <NavLink to="qr" className={({ isActive }) => isActive ? 'active' : ''}>QR Code</NavLink>
          <NavLink to="profile" className={({ isActive }) => isActive ? 'active' : ''}>Profile</NavLink>
          <NavLink to="kyc" className={({ isActive }) => isActive ? 'active' : ''}>KYC</NavLink>
          <NavLink to="transactions" className={({ isActive }) => isActive ? 'active' : ''}>Transactions</NavLink>
        </div>
        <div className="nav-actions">
          <button onClick={handleLogout} className="btn-secondary">Logout</button>
          <button onClick={onRoleReset} className="btn-link">Switch Role</button>
        </div>
      </nav>

      <div className="app-content">
        <Routes>
          <Route path="dashboard" element={<Dashboard />} />
          <Route path="wallet" element={<Wallet />} />
          <Route path="qr" element={<QRGenerate />} />
          <Route path="profile" element={<Profile />} />
          <Route path="kyc" element={<KYC />} />
          <Route path="transactions" element={<Transactions />} />
          <Route path="*" element={<Navigate to="dashboard" replace />} />
        </Routes>
      </div>
    </div>
  );
}

export default CustomerApp;

