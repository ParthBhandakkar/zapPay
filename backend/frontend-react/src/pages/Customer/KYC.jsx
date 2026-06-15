import { useState, useEffect } from 'react';
import { customerAPI } from '../../utils/api';

function KYC() {
  const [kycData, setKycData] = useState({
    aadhaar_number: '',
    pan_number: '',
    driving_license: ''
  });
  const [kycStatus, setKycStatus] = useState(null);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');

  useEffect(() => {
    fetchKYCStatus();
  }, []);

  const fetchKYCStatus = async () => {
    try {
      const { data } = await customerAPI.get('/users/kyc/status');
      setKycStatus(data);
    } catch (error) {
      console.error('Failed to load KYC status:', error);
    }
  };

  const handleSubmit = async () => {
    try {
      setLoading(true);
      setMessage('');
      await customerAPI.post('/users/kyc/submit', kycData);
      setMessage('KYC submitted successfully! Verification in progress.');
      fetchKYCStatus();
      setKycData({ aadhaar_number: '', pan_number: '', driving_license: '' });
    } catch (error) {
      setMessage(error.response?.data?.detail || 'KYC submission failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page-container">
      <h1>KYC Verification</h1>

      {kycStatus && (
        <div className={`card kyc-status ${kycStatus.kyc_status}`}>
          <h3>Current Status</h3>
          <div className="status-badge">
            <span className={`badge kyc-${kycStatus.kyc_status}`}>
              {kycStatus.kyc_status}
            </span>
          </div>
          {kycStatus.kyc_verified_at && (
            <p className="verified-date">
              Verified on: {new Date(kycStatus.kyc_verified_at).toLocaleDateString()}
            </p>
          )}
        </div>
      )}

      {(!kycStatus || kycStatus.kyc_status === 'not_submitted' || kycStatus.kyc_status === 'rejected') && (
        <div className="card">
          <h3>Submit KYC Documents</h3>
          <div className="form">
            <div className="form-group">
              <label>Aadhaar Number *</label>
              <input
                type="text"
                placeholder="12-digit Aadhaar"
                value={kycData.aadhaar_number}
                onChange={(e) => setKycData({...kycData, aadhaar_number: e.target.value})}
                maxLength="12"
              />
            </div>

            <div className="form-group">
              <label>PAN Number *</label>
              <input
                type="text"
                placeholder="ABCDE1234F"
                value={kycData.pan_number}
                onChange={(e) => setKycData({...kycData, pan_number: e.target.value.toUpperCase()})}
                maxLength="10"
              />
            </div>

            <div className="form-group">
              <label>Driving License (Optional)</label>
              <input
                type="text"
                placeholder="DL Number"
                value={kycData.driving_license}
                onChange={(e) => setKycData({...kycData, driving_license: e.target.value})}
              />
            </div>

            <button 
              onClick={handleSubmit} 
              disabled={loading || !kycData.aadhaar_number || !kycData.pan_number}
              className="btn-primary"
            >
              {loading ? 'Submitting...' : 'Submit KYC'}
            </button>
          </div>
        </div>
      )}

      {message && (
        <div className={`message ${message.includes('failed') || message.includes('Failed') ? 'error' : 'success'}`}>
          {message}
        </div>
      )}
    </div>
  );
}

export default KYC;

