import React, { useState, useEffect } from 'react';
import { HashRouter as Router, Routes, Route, Navigate, useNavigate, useLocation } from 'react-router-dom';
import './App.css';

// Lazy load components to avoid hook issues
const RoleSelector = React.lazy(() => import('./components/RoleSelector'));
const CustomerApp = React.lazy(() => import('./pages/Customer/CustomerApp'));
const PumpApp = React.lazy(() => import('./pages/Pump/PumpApp'));

// Route guards based on role
function CustomerRouteGuard({ role, children }) {
  if (role !== 'customer') {
    return <Navigate to={role === 'pump' ? '/pump' : '/'} replace />;
  }
  return children;
}

function PumpRouteGuard({ role, children }) {
  if (role !== 'pump') {
    return <Navigate to={role === 'customer' ? '/customer' : '/'} replace />;
  }
  return children;
}

function AppContent() {
  const navigate = useNavigate();
  const location = useLocation();
  
  const [role, setRole] = useState(() => {
    try {
      // Only auto-load role if user is navigating to a specific path
      const savedRole = localStorage.getItem('zappay.role');
      const currentPath = window.location.hash.replace('#', '');
      
      // If opening root URL, always show role selector (even if role was saved)
      if (!currentPath || currentPath === '/') {
        return null;
      }
      
      // If navigating to /customer or /pump, use saved role
      return savedRole || null;
    } catch (e) {
      console.error('LocalStorage error:', e);
      return null;
    }
  });

  useEffect(() => {
    console.log('Current role:', role);
    console.log('Current path:', location.pathname);
    
    // Don't auto-redirect on initial load at root
    if (role && location.pathname === '/' && location.pathname !== window.location.hash.replace('#', '')) {
      navigate(role === 'customer' ? '/customer' : '/pump', { replace: true });
    }
  }, [role, location.pathname, navigate]);

  const handleRoleSelect = (selectedRole) => {
    console.log('Role selected:', selectedRole);
    setRole(selectedRole);
    try {
      localStorage.setItem('zappay.role', selectedRole);
      navigate(selectedRole === 'customer' ? '/customer' : '/pump', { replace: true });
    } catch (e) {
      console.error('Failed to save role:', e);
    }
  };

  const handleRoleReset = () => {
    console.log('Resetting role');
    setRole(null);
    try {
      localStorage.removeItem('zappay.role');
      localStorage.removeItem('zappay.customer.tokens');
      localStorage.removeItem('zappay.pump.tokens');
      navigate('/', { replace: true });
    } catch (e) {
      console.error('Failed to clear storage:', e);
    }
  };

  if (!role) {
    return <RoleSelector onSelectRole={handleRoleSelect} />;
  }

  return (
    <Routes>
      <Route
        path="/customer/*"
        element={
          <CustomerRouteGuard role={role}>
            <CustomerApp onRoleReset={handleRoleReset} />
          </CustomerRouteGuard>
        }
      />
      <Route
        path="/pump/*"
        element={
          <PumpRouteGuard role={role}>
            <PumpApp onRoleReset={handleRoleReset} />
          </PumpRouteGuard>
        }
      />
      <Route path="*" element={<Navigate to={role === 'customer' ? '/customer' : '/pump'} replace />} />
    </Routes>
  );
}

function App() {
  return (
    <Router>
      <div className="app">
        <React.Suspense fallback={<div style={{display:'flex',justifyContent:'center',alignItems:'center',minHeight:'100vh',fontSize:'24px'}}>Loading...</div>}>
          <AppContent />
        </React.Suspense>
      </div>
    </Router>
  );
}

export default App;
