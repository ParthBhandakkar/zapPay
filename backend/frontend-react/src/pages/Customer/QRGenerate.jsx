import { useState, useEffect } from 'react';
import { customerAPI, CUSTOMER_API_BASE } from '../../utils/api';

function QRGenerate() {
  const [qrType, setQrType] = useState('mobile');
  const [validityHours, setValidityHours] = useState('');
  const [qrData, setQrData] = useState(null);
  const [qrImageData, setQrImageData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');

  // Fetch QR image when qrData changes
  useEffect(() => {
    if (qrData?.id) {
      fetchQRImage(qrData.id);
    }
  }, [qrData]);

  const fetchQRImage = async (qrCodeId) => {
    try {
      const { data } = await customerAPI.get(`/qr/${qrCodeId}/image`);
      if (data && data.image_data) {
        setQrImageData(`data:image/png;base64,${data.image_data}`);
      }
    } catch (error) {
      console.error('Failed to load QR image:', error);
      setQrImageData(null);
    }
  };

  const handleGenerate = async () => {
    try {
      setLoading(true);
      setMessage('');
      setQrData(null); // Clear previous QR
      setQrImageData(null); // Clear previous image
      const payload = { qr_type: qrType };
      if (validityHours) payload.validity_hours = parseInt(validityHours);

      const { data } = await customerAPI.post('/qr/generate', payload);
      console.log('QR Generate Response:', data);
      // Backend returns QRCodeResponse directly, not wrapped in BaseResponse
      if (data && data.id) {
        setQrData(data);
        setMessage('QR Code generated successfully!');
      } else {
        setMessage('QR generation failed: Invalid response');
        console.error('Unexpected response structure:', data);
      }
    } catch (error) {
      console.error('QR Generate Error:', error);
      setMessage(error.response?.data?.detail || 'Failed to generate QR');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page-container">
      <h1>📱 Generate QR Code</h1>
      <p className="page-subtitle">Create a secure QR code for fuel payments</p>

      <div className="card">
        <div className="form">
          <div className="form-group">
            <label>QR Type</label>
            <select value={qrType} onChange={(e) => setQrType(e.target.value)}>
              <option value="mobile">📱 Mobile (Dynamic)</option>
              <option value="sticker">🏷️ Sticker (Static)</option>
            </select>
          </div>

          <div className="form-group">
            <label>Validity (hours) - Optional</label>
            <input
              type="number"
              placeholder="Leave empty for default"
              value={validityHours}
              onChange={(e) => setValidityHours(e.target.value)}
              min="1"
            />
          </div>

          <button onClick={handleGenerate} disabled={loading} className="btn-primary btn-large">
            {loading ? '⏳ Generating...' : '✨ Generate QR Code'}
          </button>
        </div>
      </div>

      {qrData && (
        <div className="card qr-display-card">
          <h3>✅ Your QR Code is Ready!</h3>
          <div className="qr-display">
            {qrImageData ? (
              <img 
                src={qrImageData}
                alt="QR Code"
                className="qr-image"
              />
            ) : (
              <div className="qr-loading">
                <div className="spinner">⏳</div>
                <p>Loading QR code image...</p>
              </div>
            )}
            <div className="qr-info">
              <div className="qr-info-item">
                <span className="qr-info-label">Type:</span>
                <span className="qr-info-value">{qrData.qr_type}</span>
              </div>
              <div className="qr-info-item">
                <span className="qr-info-label">Status:</span>
                <span className={`qr-status ${qrData.is_active ? 'active' : 'inactive'}`}>
                  {qrData.is_active ? '✅ Active' : '❌ Inactive'}
                </span>
              </div>
              {qrData.expires_at && (
                <div className="qr-info-item">
                  <span className="qr-info-label">Expires:</span>
                  <span className="qr-info-value">{new Date(qrData.expires_at).toLocaleString()}</span>
                </div>
              )}
            </div>
            <div className="qr-instructions">
              <p>📸 Show this QR code at the pump for payment</p>
              <p>💡 The QR code is linked to your wallet</p>
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

export default QRGenerate;

