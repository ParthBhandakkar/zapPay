import { useState, useEffect, useRef } from 'react';
import { Html5Qrcode } from 'html5-qrcode';
import { pumpAPI, PUMP_API_BASE } from '../../utils/api';
import axios from 'axios';

function Scanner() {
  const [scanning, setScanning] = useState(false);
  const [customerData, setCustomerData] = useState(null);
  const [qrCodeData, setQrCodeData] = useState(''); // Store the original QR code data
  const [manualQrData, setManualQrData] = useState('');
  const [purchaseData, setPurchaseData] = useState({
    pump_id: '1',
    fuel_type: 'petrol',
    fuel_quantity: '',
    fuel_rate: ''
  });
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');
  const html5QrRef = useRef(null);

  useEffect(() => {
    return () => {
      if (html5QrRef.current) {
        html5QrRef.current.stop().catch(() => {});
      }
    };
  }, []);

  const startScanner = async () => {
    try {
      setMessage('');
      setCustomerData(null); // Clear previous customer data
      
      // Stop existing scanner if any
      if (html5QrRef.current) {
        try {
          await html5QrRef.current.stop();
          html5QrRef.current = null;
        } catch (e) {
          console.log('Previous scanner already stopped');
        }
      }
      
      // Small delay to ensure camera is released
      await new Promise(resolve => setTimeout(resolve, 100));
      
      // Create new scanner instance
      const html5QrCode = new Html5Qrcode("qr-reader");
      html5QrRef.current = html5QrCode;
      
      // Get available cameras
      const devices = await Html5Qrcode.getCameras();
      console.log('Available cameras:', devices);
      
      if (!devices || devices.length === 0) {
        throw new Error('No cameras found on this device');
      }
      
      // Try to start with back camera, fallback to any camera
      let cameraId = devices[0].id;
      
      // Look for back/rear camera
      const backCamera = devices.find(device => 
        device.label.toLowerCase().includes('back') || 
        device.label.toLowerCase().includes('rear') ||
        device.label.toLowerCase().includes('environment')
      );
      
      if (backCamera) {
        cameraId = backCamera.id;
        console.log('Using back camera:', backCamera.label);
      } else {
        console.log('Using first available camera:', devices[0].label);
      }
      
      await html5QrCode.start(
        cameraId,
        { fps: 10, qrbox: { width: 250, height: 250 } },
        onScanSuccess,
        onScanFailure
      );
      setScanning(true);
      setMessage('📸 Scanner active. Point camera at QR code.');
    } catch (error) {
      console.error('Scanner start error:', error);
      const errorStr = error?.message || error?.toString() || String(error);
      let errorMsg = 'Failed to start scanner';
      
      if (errorStr.includes('secure context') || errorStr.includes('https')) {
        errorMsg = '🔒 Camera requires HTTPS or localhost. Access this page from your computer using: http://localhost:5173';
      } else if (errorStr.includes('NotAllowedError') || errorStr.includes('Permission denied')) {
        errorMsg = '❌ Camera permission denied. Please allow camera access and try again.';
      } else if (errorStr.includes('NotFoundError') || errorStr.includes('No cameras')) {
        errorMsg = '❌ No camera found on this device.';
      } else if (errorStr.includes('NotReadableError')) {
        errorMsg = '❌ Camera is being used by another app. Please close other apps and try again.';
      } else {
        errorMsg = `❌ ${errorStr}`;
      }
      
      setMessage(errorMsg);
      html5QrRef.current = null;
      setScanning(false);
    }
  };

  const stopScanner = async () => {
    if (html5QrRef.current) {
      try {
        await html5QrRef.current.stop();
        html5QrRef.current = null;
        setScanning(false);
        setMessage('Scanner stopped.');
      } catch (error) {
        console.error('Error stopping scanner:', error);
        html5QrRef.current = null;
        setScanning(false);
      }
    }
  };

  const onScanFailure = (error) => {
    // Silent fail for scanning errors (expected when no QR in view)
    // console.log('Scan failure:', error);
  };

  const onScanSuccess = async (qrData) => {
    stopScanner();
    validateQR(qrData);
  };

  const validateQR = async (qrData) => {
    try {
      setLoading(true);
      setMessage('Validating QR...');
      console.log('🔍 Scanning QR Data:', qrData);

      // Use unauthenticated request for QR validation (pump operators don't need to be logged in to validate customer QR)
      const { data } = await axios.post(`${PUMP_API_BASE}/qr/validate`, { qr_data: qrData });
      console.log('✅ Validation Response:', data);

      if (data.success || data.valid) {
        console.log('📋 Customer Data:', data.data || data);
        const customerInfo = data.data || data; // Handle both response formats
        setCustomerData({
          user_id: customerInfo.user_id,
          user_name: customerInfo.user_name,
          user_phone: customerInfo.user_phone,
          qr_code_id: customerInfo.qr_code_id,
          qr_type: customerInfo.qr_type,
          wallet_balance: customerInfo.wallet_balance,
          vehicle_number: customerInfo.vehicle_number
        });
        setQrCodeData(qrData); // Store the original QR code data for purchase
        setMessage('✅ QR validated! Customer details loaded.');
        setManualQrData(''); // Clear manual input
      } else {
        console.warn('⚠️ Validation not successful:', data);
        setMessage(data.message || 'QR validation failed - response not successful');
        setCustomerData(null);
      }
    } catch (error) {
      console.error('❌ Validation Error:', error);
      console.error('Error response:', error.response?.data);
      setMessage(error.response?.data?.detail || 'QR validation failed');
      setCustomerData(null);
    } finally {
      setLoading(false);
    }
  };

  const handleManualValidate = () => {
    if (manualQrData.trim()) {
      validateQR(manualQrData.trim());
    } else {
      setMessage('Please enter QR data');
    }
  };

  const handlePurchase = async () => {
    try {
      setLoading(true);
      setMessage('');
      const payload = {
        qr_code: qrCodeData, // Use the original QR code data, not the ID
        pump_id: parseInt(purchaseData.pump_id), // Convert to integer
        fuel_type: purchaseData.fuel_type,
        fuel_quantity: parseFloat(purchaseData.fuel_quantity),
        fuel_rate: parseFloat(purchaseData.fuel_rate)
      };

      const { data } = await pumpAPI.post('/transactions/fuel-purchase', payload);
      setMessage(`Purchase successful! Transaction ID: ${data.data.transaction_id}`);
      setCustomerData(null);
      setQrCodeData(''); // Clear QR code data
      setPurchaseData({ pump_id: '1', fuel_type: 'petrol', fuel_quantity: '', fuel_rate: '' });
    } catch (error) {
      console.error('Purchase error:', error.response?.data);
      setMessage(error.response?.data?.detail || 'Purchase failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page-container">
      <h1>📷 QR Scanner</h1>
      <p className="page-subtitle">Scan customer QR code to view details and process fuel purchase</p>

      {window.location.hostname !== 'localhost' && window.location.hostname !== '127.0.0.1' && window.location.protocol === 'http:' && (
        <div className="alert alert-warning">
          <h3>⚠️ Camera Access Limitation</h3>
          <p>Camera access on mobile requires HTTPS or localhost. To use the scanner:</p>
          <ul>
            <li><strong>On Computer:</strong> Open <code>http://localhost:5173</code></li>
            <li><strong>On Mobile:</strong> For demo purposes, manually enter customer details below</li>
          </ul>
        </div>
      )}

      <div className="card scanner-card">
        <div className="scanner-wrapper">
          <div id="qr-reader" style={{ width: '100%', maxWidth: '500px', margin: '0 auto', borderRadius: '12px', overflow: 'hidden' }}></div>
        </div>
        
        <div className="scanner-controls">
          {!scanning ? (
            <div className="button-group">
              <button onClick={startScanner} disabled={loading} className="btn-primary btn-large">
                <span style={{ fontSize: '20px', marginRight: '8px' }}>📸</span>
                Start Scanner
              </button>
              {customerData && (
                <button onClick={() => {
                  setCustomerData(null);
                  setQrCodeData('');
                }} className="btn-secondary">
                  Clear Customer
                </button>
              )}
            </div>
          ) : (
            <button onClick={stopScanner} className="btn-secondary btn-large">
              <span style={{ fontSize: '20px', marginRight: '8px' }}>⏹</span>
              Stop Scanner
            </button>
          )}
        </div>
        
        {!customerData && !scanning && (
          <div className="scanner-instructions">
            <p>👆 Click "Start Scanner" to begin</p>
            <p>📱 Point your camera at the customer's QR code</p>
            <p>✨ Customer details will appear automatically</p>
          </div>
        )}
      </div>

      {/* Manual QR Input - Fallback for testing */}
      {!customerData && (
        <div className="card manual-input-card">
          <h3>🔧 Manual QR Validation (Testing)</h3>
          <p>For testing without camera, paste QR data here:</p>
          <div className="manual-input-group">
            <input
              type="text"
              placeholder="Paste encrypted QR data here..."
              value={manualQrData}
              onChange={(e) => setManualQrData(e.target.value)}
              className="manual-qr-input"
              disabled={loading || scanning}
            />
            <button 
              onClick={handleManualValidate}
              disabled={loading || scanning || !manualQrData.trim()}
              className="btn-primary"
            >
              {loading ? '⏳ Validating...' : '✓ Validate'}
            </button>
          </div>
          <p className="help-text">
            💡 Tip: Generate a QR code in Customer interface, copy the encrypted data from browser console
          </p>
        </div>
      )}

      {customerData && (
        <div className="card customer-details-card">
          <div className="customer-header">
            <h2>✅ Customer Verified</h2>
            <button onClick={() => {
              setCustomerData(null);
              setQrCodeData('');
            }} className="btn-link">Clear</button>
          </div>
          
          <div className="customer-info-grid">
            <div className="info-item">
              <div className="info-label">👤 Name</div>
              <div className="info-value">{customerData.user_name}</div>
            </div>
            <div className="info-item">
              <div className="info-label">📱 Phone</div>
              <div className="info-value">{customerData.user_phone}</div>
            </div>
            <div className="info-item highlight">
              <div className="info-label">💰 Wallet Balance</div>
              <div className="info-value balance">₹{customerData.wallet_balance?.toFixed(2)}</div>
            </div>
            {customerData.vehicle_number && (
              <div className="info-item">
                <div className="info-label">🚗 Vehicle</div>
                <div className="info-value">{customerData.vehicle_number}</div>
              </div>
            )}
          </div>

          <div className="purchase-section">
            <h3>⛽ Fuel Purchase</h3>
            <div className="form">
            <div className="form-group">
              <label>Pump ID</label>
              <input
                type="number"
                value={purchaseData.pump_id}
                onChange={(e) => setPurchaseData({...purchaseData, pump_id: e.target.value})}
              />
            </div>

            <div className="form-group">
              <label>Fuel Type</label>
              <select
                value={purchaseData.fuel_type}
                onChange={(e) => setPurchaseData({...purchaseData, fuel_type: e.target.value})}
              >
                <option value="petrol">Petrol</option>
                <option value="diesel">Diesel</option>
              </select>
            </div>

            <div className="form-group">
              <label>Quantity (Liters)</label>
              <input
                type="number"
                step="0.1"
                value={purchaseData.fuel_quantity}
                onChange={(e) => setPurchaseData({...purchaseData, fuel_quantity: e.target.value})}
              />
            </div>

            <div className="form-group">
              <label>Rate (₹/L)</label>
              <input
                type="number"
                step="0.01"
                value={purchaseData.fuel_rate}
                onChange={(e) => setPurchaseData({...purchaseData, fuel_rate: e.target.value})}
              />
            </div>

            <div className="purchase-summary">
              <div className="summary-row">
                <span>Total Amount:</span>
                <span className="total-amount">
                  ₹{(parseFloat(purchaseData.fuel_quantity || 0) * parseFloat(purchaseData.fuel_rate || 0)).toFixed(2)}
                </span>
              </div>
            </div>

            <button 
              onClick={handlePurchase} 
              disabled={loading || !purchaseData.fuel_quantity || !purchaseData.fuel_rate}
              className="btn-primary btn-large"
            >
              {loading ? '⏳ Processing...' : '✅ Complete Purchase'}
            </button>
          </div>
          </div>
        </div>
      )}

      {message && (
        <div className={`message ${message.includes('failed') || message.includes('Failed') || message.includes('❌') ? 'error' : 'success'}`}>
          {message}
        </div>
      )}
    </div>
  );
}

export default Scanner;

