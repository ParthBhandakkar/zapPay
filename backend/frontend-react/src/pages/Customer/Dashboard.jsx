import { useState, useEffect } from 'react';
import { customerAPI } from '../../utils/api';

function Dashboard() {
  const [dashboard, setDashboard] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchDashboard();
  }, []);

  const fetchDashboard = async () => {
    try {
      const { data } = await customerAPI.get('/users/dashboard');
      setDashboard(data);
    } catch (error) {
      console.error('Failed to load dashboard:', error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <div className="loading">Loading...</div>;

  return (
    <div className="page-container">
      <h1>Dashboard</h1>
      
      {dashboard && (
        <div className="dashboard-grid">
          <div className="card">
            <h3>Wallet Balance</h3>
            <div className="amount">₹{dashboard.wallet_balance?.toFixed(2) || '0.00'}</div>
          </div>
          
          <div className="card">
            <h3>Total Spent</h3>
            <div className="amount">₹{dashboard.total_spent?.toFixed(2) || '0.00'}</div>
          </div>
          
          <div className="card">
            <h3>Total Transactions</h3>
            <div className="count">{dashboard.transaction_count || 0}</div>
          </div>
          
          <div className="card">
            <h3>Active QR Codes</h3>
            <div className="count">{dashboard.active_qr_count || 0}</div>
          </div>
        </div>
      )}

      {dashboard?.recent_transactions && dashboard.recent_transactions.length > 0 && (
        <div className="recent-transactions">
          <h2>Recent Transactions</h2>
          <div className="transaction-list">
            {dashboard.recent_transactions.map((tx) => (
              <div key={tx.id} className="transaction-item">
                <div className="tx-info">
                  <div className="tx-type">{tx.transaction_type}</div>
                  <div className="tx-date">{new Date(tx.created_at).toLocaleDateString()}</div>
                </div>
                <div className={`tx-amount ${tx.transaction_type === 'debit' ? 'debit' : 'credit'}`}>
                  {tx.transaction_type === 'debit' ? '-' : '+'}₹{tx.amount.toFixed(2)}
                </div>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}

export default Dashboard;

