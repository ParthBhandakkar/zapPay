import { useState, useEffect } from 'react';
import { customerAPI } from '../../utils/api';

function Wallet() {
  const [wallet, setWallet] = useState(null);
  const [amount, setAmount] = useState('');
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');

  useEffect(() => {
    fetchWallet();
  }, []);

  const fetchWallet = async () => {
    try {
      const { data } = await customerAPI.get('/wallet/balance');
      setWallet(data);
    } catch (error) {
      setMessage('Failed to load wallet');
    }
  };

  const handleRecharge = async () => {
    try {
      setLoading(true);
      setMessage('');
      const { data } = await customerAPI.post(`/wallet/test-recharge?amount=${amount}`);
      setMessage(`Recharged ₹${amount}. New balance: ₹${data.data.new_balance.toFixed(2)}`);
      setAmount('');
      fetchWallet();
    } catch (error) {
      setMessage(error.response?.data?.detail || 'Recharge failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page-container">
      <h1>Wallet</h1>

      {wallet && (
        <div className="wallet-info">
          <div className="card wallet-card">
            <h2>Balance</h2>
            <div className="balance">₹{wallet.balance?.toFixed(2) || '0.00'}</div>
            <div className="wallet-stats">
              <div className="stat">
                <span className="label">Total Recharged:</span>
                <span className="value">₹{wallet.total_recharged?.toFixed(2) || '0.00'}</span>
              </div>
              <div className="stat">
                <span className="label">Total Spent:</span>
                <span className="value">₹{wallet.total_spent?.toFixed(2) || '0.00'}</span>
              </div>
            </div>
          </div>

          <div className="card recharge-card">
            <h3>Test Recharge (Dev Only)</h3>
            <div className="form-inline">
              <input
                type="number"
                placeholder="Amount"
                value={amount}
                onChange={(e) => setAmount(e.target.value)}
                min="1"
                step="0.01"
              />
              <button onClick={handleRecharge} disabled={loading || !amount}>
                {loading ? 'Processing...' : 'Recharge'}
              </button>
            </div>
          </div>
        </div>
      )}

      {message && (
        <div className={`message ${message.includes('Failed') || message.includes('failed') ? 'error' : 'success'}`}>
          {message}
        </div>
      )}
    </div>
  );
}

export default Wallet;

