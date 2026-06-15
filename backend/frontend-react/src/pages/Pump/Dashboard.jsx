import { useState, useEffect } from 'react';
import { pumpAPI } from '../../utils/api';

function Dashboard() {
  const [dashboard, setDashboard] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadDashboard();
  }, []);

  const loadDashboard = async () => {
    try {
      setLoading(true);
      // For now, show welcome message. You can add pump stats API later
      setDashboard({ message: 'Welcome to Pump Dashboard!' });
    } catch (error) {
      console.error('Failed to load dashboard:', error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="page-container">
        <div className="loading">Loading dashboard...</div>
      </div>
    );
  }

  return (
    <div className="page-container">
      <h1>⛽ Pump Dashboard</h1>
      <p className="page-subtitle">Overview of your pump operations</p>

      <div className="dashboard-grid">
        <div className="card stat-card">
          <div className="stat-icon">💰</div>
          <h3>Today's Sales</h3>
          <div className="amount">₹0.00</div>
          <div className="stat-subtitle">No sales yet</div>
        </div>
        
        <div className="card stat-card">
          <div className="stat-icon">🔢</div>
          <h3>Transactions</h3>
          <div className="count">0</div>
          <div className="stat-subtitle">Total transactions</div>
        </div>
        
        <div className="card stat-card">
          <div className="stat-icon">⛽</div>
          <h3>Fuel Dispensed</h3>
          <div className="count">0.00 L</div>
          <div className="stat-subtitle">Total fuel</div>
        </div>
        
        <div className="card stat-card">
          <div className="stat-icon">👥</div>
          <h3>Customers</h3>
          <div className="count">0</div>
          <div className="stat-subtitle">Served today</div>
        </div>
      </div>

      <div className="card welcome-card">
        <h2>🚀 Quick Actions</h2>
        <div className="quick-actions">
          <a href="#/pump/scanner" className="action-btn">
            <span className="action-icon">📸</span>
            <span className="action-label">Scan QR Code</span>
          </a>
          <a href="#/pump/settings" className="action-btn">
            <span className="action-icon">⚙️</span>
            <span className="action-label">Pump Settings</span>
          </a>
          <a href="#/pump/transactions" className="action-btn">
            <span className="action-icon">📊</span>
            <span className="action-label">View Transactions</span>
          </a>
          <a href="#/pump/operators" className="action-btn">
            <span className="action-icon">👨‍💼</span>
            <span className="action-label">Manage Operators</span>
          </a>
        </div>
      </div>

      {dashboard && (
        <div className="dashboard-grid">
          <div className="card">
            <h3>Total Sales</h3>
            <div className="amount">₹{dashboard.total_sales?.toFixed(2) || '0.00'}</div>
          </div>
          
          <div className="card">
            <h3>Total Transactions</h3>
            <div className="count">{dashboard.total_transactions || 0}</div>
          </div>
          
          <div className="card">
            <h3>Total Fuel (L)</h3>
            <div className="count">{dashboard.total_fuel_quantity?.toFixed(2) || '0.00'}</div>
          </div>
          
          <div className="card">
            <h3>Active Today</h3>
            <div className="count">{dashboard.transactions_today || 0}</div>
          </div>
        </div>
      )}

      {dashboard?.recent_transactions && dashboard.recent_transactions.length > 0 && (
        <div className="card">
          <h2>Recent Transactions</h2>
          <div className="transaction-list">
            {dashboard.recent_transactions.map((tx) => (
              <div key={tx.id} className="transaction-item">
                <div className="tx-info">
                  <div className="tx-type">{tx.fuel_quantity}L {tx.fuel_type}</div>
                  <div className="tx-date">{new Date(tx.created_at).toLocaleDateString()}</div>
                </div>
                <div className="tx-amount">₹{tx.amount.toFixed(2)}</div>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}

export default Dashboard;

