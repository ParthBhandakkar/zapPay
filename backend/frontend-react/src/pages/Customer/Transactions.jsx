import { useState, useEffect } from 'react';
import { customerAPI } from '../../utils/api';

function Transactions() {
  const [transactions, setTransactions] = useState([]);
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchTransactions();
  }, [page]);

  const fetchTransactions = async () => {
    try {
      setLoading(true);
      const { data } = await customerAPI.get(`/transactions/history?page=${page}&page_size=10`);
      // Handle different response formats
      setTransactions(data.transactions || data.data || []);
      setTotalPages(data.total_pages || 1);
    } catch (error) {
      console.error('Failed to load transactions:', error);
      setTransactions([]);
      setTotalPages(1);
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <div className="loading">Loading...</div>;

  return (
    <div className="page-container">
      <h1>Transaction History</h1>

      {transactions.length === 0 ? (
        <div className="card empty-state">
          <p>No transactions yet</p>
        </div>
      ) : (
        <>
          <div className="transactions-list">
            {transactions.map((tx) => (
              <div key={tx.id} className="card transaction-card">
                <div className="tx-header">
                  <span className={`tx-type ${tx.transaction_type}`}>
                    {tx.transaction_type.toUpperCase()}
                  </span>
                  <span className={`tx-status ${tx.status}`}>{tx.status}</span>
                </div>
                
                <div className="tx-details">
                  <div className="detail-row">
                    <span className="label">Amount:</span>
                    <span className={`amount ${tx.transaction_type === 'fuel_purchase' ? 'fuel_purchase' : tx.transaction_type}`}>
                      {tx.transaction_type === 'debit' || tx.transaction_type === 'fuel_purchase' ? '-' : '+'}₹{tx.amount.toFixed(2)}
                    </span>
                  </div>
                  
                  {tx.fuel_type && (
                    <div className="detail-row">
                      <span className="label">Fuel:</span>
                      <span>{tx.fuel_quantity}L {tx.fuel_type} @ ₹{tx.fuel_rate}/L</span>
                    </div>
                  )}
                  
                  {tx.pump_name && (
                    <div className="detail-row">
                      <span className="label">Pump:</span>
                      <span>{tx.pump_name}</span>
                    </div>
                  )}
                  
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

