import { useState, useEffect } from 'react';
import { Routes, Route, NavLink, useNavigate, Navigate } from 'react-router-dom';
import './Pump.css';
import Auth from './Auth';
import Dashboard from './Dashboard';
import Settings from './Settings';
import Scanner from './Scanner';
import Transactions from './Transactions';
import Operators from './Operators';

function PumpApp({ onRoleReset }) {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    const tokens = localStorage.getItem('zappay.pump.tokens');
    console.log('Pump tokens check:', tokens);
    console.log('isAuthenticated:', !!tokens);
    setIsAuthenticated(!!tokens);
  }, []);

  const handleLogin = (tokens) => {
    localStorage.setItem('zappay.pump.tokens', JSON.stringify(tokens));
    setIsAuthenticated(true);
    navigate('dashboard');
  };

  const handleLogout = () => {
    localStorage.removeItem('zappay.pump.tokens');
    setIsAuthenticated(false);
    onRoleReset(); // Reset role and go to role selector
  };

  if (!isAuthenticated) {
    return (
      <div className="pump-app">
        <Auth onLogin={handleLogin} />
      </div>
    );
  }

  return (
    <div className="pump-app">
      <nav className="app-nav pump-nav">
        <div className="nav-brand">⛽ ZapPay Pump</div>
        <div className="nav-links">
          <NavLink to="dashboard" className={({ isActive }) => isActive ? 'active' : ''}>Dashboard</NavLink>
          <NavLink to="settings" className={({ isActive }) => isActive ? 'active' : ''}>Settings</NavLink>
          <NavLink to="scanner" className={({ isActive }) => isActive ? 'active' : ''}>Scanner</NavLink>
          <NavLink to="transactions" className={({ isActive }) => isActive ? 'active' : ''}>Transactions</NavLink>
          <NavLink to="operators" className={({ isActive }) => isActive ? 'active' : ''}>Operators</NavLink>
        </div>
        <div className="nav-actions">
          <button onClick={handleLogout} className="btn-secondary">Logout</button>
          <button onClick={onRoleReset} className="btn-link">Switch Role</button>
        </div>
      </nav>

      <div className="app-content">
        <Routes>
          <Route path="dashboard" element={<Dashboard />} />
          <Route path="settings" element={<Settings />} />
          <Route path="scanner" element={<Scanner />} />
          <Route path="transactions" element={<Transactions />} />
          <Route path="operators" element={<Operators />} />
          <Route path="*" element={<Navigate to="dashboard" replace />} />
        </Routes>
      </div>
    </div>
  );
}

export default PumpApp;

