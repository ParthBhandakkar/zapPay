import { useState } from 'react';
import { customerAPI } from '../../utils/api';

function Operators() {
  const [operatorData, setOperatorData] = useState({
    phone_number: '',
    employee_id: ''
  });
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');

  const handleAddOperator = async () => {
    try {
      setLoading(true);
      setMessage('');
      await customerAPI.post('/pumps/operators/add', operatorData);
      setMessage('Operator added successfully!');
      setOperatorData({ phone_number: '', employee_id: '' });
    } catch (error) {
      setMessage(error.response?.data?.detail || 'Failed to add operator');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page-container">
      <h1>Operator Management</h1>

      <div className="card">
        <h3>Add New Operator</h3>
        <div className="form">
          <div className="form-group">
            <label>Phone Number</label>
            <input
              type="tel"
              placeholder="Operator phone number"
              value={operatorData.phone_number}
              onChange={(e) => setOperatorData({...operatorData, phone_number: e.target.value})}
            />
          </div>

          <div className="form-group">
            <label>Employee ID</label>
            <input
              type="text"
              placeholder="Employee ID"
              value={operatorData.employee_id}
              onChange={(e) => setOperatorData({...operatorData, employee_id: e.target.value})}
            />
          </div>

          <button 
            onClick={handleAddOperator} 
            disabled={loading || !operatorData.phone_number || !operatorData.employee_id}
            className="btn-primary"
          >
            {loading ? 'Adding...' : 'Add Operator'}
          </button>
        </div>
      </div>

      {message && (
        <div className={`message ${message.includes('Failed') || message.includes('failed') ? 'error' : 'success'}`}>
          {message}
        </div>
      )}

      <div className="card info-card">
        <h3>ℹ️ About Operators</h3>
        <p>Operators are pump employees who can scan QR codes and process fuel purchases.</p>
        <p>The user must already be registered in the system. This feature links them to your pump.</p>
      </div>
    </div>
  );
}

export default Operators;

