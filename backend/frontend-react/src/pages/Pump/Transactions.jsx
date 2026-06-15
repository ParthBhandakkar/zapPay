import { useState, useEffect } from 'react';
import { pumpAPI } from '../../utils/api';
import axios from 'axios';

function Transactions() {
  const [pumpId, setPumpId] = useState('1');
  const [transactions, setTransactions] = useState([]);
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [loading, setLoading] = useState(true);
  const [pumpInfo, setPumpInfo] = useState(null);

  const fetchPumpInfo = async () => {
    try {
      // Try to get pump settings to determine the pump ID
      const { data } = await pumpAPI.get('/settings/1');
      if (data && data.pump_id) {
        setPumpId(data.pump_id.toString());
      }
    } catch (error) {
      console.log('Could not fetch pump settings, using default pump ID 1');
    }
  };

  const fetchTransactions = async () => {
    try {
      setLoading(true);
      // Call main API with pump authentication token
      const tokens = localStorage.getItem('zappay.pump.tokens');
      if (!tokens) {
        console.log('No pump tokens found');
        setTransactions([]);
        setLoading(false);
        return;
      }
      
      const { access } = JSON.parse(tokens);
      const CUSTOMER_API_BASE = window.location.hostname === 'localhost' 
        ? 'http://localhost:8000' 
        : `${window.location.protocol}//${window.location.hostname}:8000`;
      
      const { data } = await axios.get(
        `${CUSTOMER_API_BASE}/api/v1/transactions/pump/${pumpId}/history?page=${page}&page_size=10`,
        {
          headers: {
            Authorization: `Bearer ${access}`
          }
        }
      );
      setTransactions(data.transactions || data.data || []);
      setTotalPages(data.total_pages || 1);
    } catch (error) {
      console.error('Failed to load transactions:', error);
      setTransactions([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchPumpInfo();
  }, []);

  useEffect(() => {
    if (pumpId) {
      fetchTransactions();
    }
  }, [page, pumpId]);

  return (
    <div className="page-container">
      <h1>💰 Pump Transactions</h1>
      <p className="page-subtitle">View all fuel purchases processed at this pump</p>

      {loading ? (
        <div className="loading">Loading transactions...</div>
      ) : transactions.length === 0 ? (
        <div className="card empty-state">
          <div style={{textAlign: 'center', padding: '40px'}}>
            <div style={{fontSize: '48px', marginBottom: '20px'}}>⛽</div>
            <h3>No transactions yet</h3>
            <p>Transactions will appear here once customers start purchasing fuel</p>
          </div>
        </div>
      ) : (
        <>
          <div className="transactions-list">
            {transactions.map((tx) => (
              <div key={tx.id} className="card transaction-card">
                <div className="tx-header">
                  <span className="tx-type">{tx.fuel_quantity}L {tx.fuel_type}</span>
                  <span className={`tx-status ${tx.status}`}>{tx.status}</span>
                </div>
                
                <div className="tx-details">
                  <div className="detail-row">
                    <span className="label">Amount:</span>
                    <span className="amount">₹{tx.amount.toFixed(2)}</span>
                  </div>
                  
                  <div className="detail-row">
                    <span className="label">Rate:</span>
                    <span>₹{tx.fuel_rate}/L</span>
                  </div>
                  
                  <div className="detail-row">
                    <span className="label">Customer:</span>
                    <span>{tx.customer_name || tx.user_id}</span>
                  </div>
                  
                  <div className="detail-row">
                    <span className="label">Date:</span>
                    <span>{new Date(tx.created_at).toLocaleString()}</span>
                  </div>
                  
                  {tx.transaction_id && (
                    <div className="detail-row">
                      <span className="label">ID:</span>
                      <span className="tx-id">{tx.transaction_id}</span>
                    </div>
                  )}
                </div>
              </div>
            ))}
          </div>

          {totalPages > 1 && (
            <div className="pagination">
              <button 
                onClick={() => setPage(p => Math.max(1, p - 1))}
                disabled={page === 1}
              >
                Previous
              </button>
              <span>Page {page} of {totalPages}</span>
              <button 
                onClick={() => setPage(p => Math.min(totalPages, p + 1))}
                disabled={page === totalPages}
              >
                Next
              </button>
            </div>
          )}
        </>
      )}
    </div>
  );
}

export default Transactions;

