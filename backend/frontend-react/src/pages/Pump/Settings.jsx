import { useState, useEffect } from 'react';
import { pumpAPI } from '../../utils/api';

function Settings() {
  const [settings, setSettings] = useState({
    pump_id: '1', // Default to pump ID 1
    pump_name: '',
    petrol_price: ''
  });
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');

  // Auto-load settings on component mount
  useEffect(() => {
    if (settings.pump_id) {
      handleLoad();
    }
  }, []); // Only run once on mount

  const handleSave = async () => {
    try {
      setLoading(true);
      setMessage('');
      await pumpAPI.post('/settings/save', settings);
      setMessage('Settings saved successfully!');
    } catch (error) {
      setMessage(error.response?.data?.detail || 'Failed to save settings');
    } finally {
      setLoading(false);
    }
  };

  const handleLoad = async () => {
    try {
      setLoading(true);
      setMessage('');
      const { data } = await pumpAPI.get(`/settings/${settings.pump_id}`);
      setSettings(prev => ({
        ...prev,
        pump_id: data.pump_id.toString(),
        pump_name: data.pump_name || '',
        petrol_price: data.petrol_price || ''
      }));
      setMessage('Settings loaded successfully!');
    } catch (error) {
      console.error('Settings load error:', error);
      setMessage(error.response?.data?.detail || 'Failed to load settings. Please check your permissions.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page-container">
      <h1>⚙️ Pump Settings</h1>
      <p className="page-subtitle">Configure your pump details and pricing</p>

      <div className="card">
        <div className="form">
          <div className="form-group">
            <label>Pump ID</label>
            <input
              type="number"
              placeholder="1"
              value={settings.pump_id}
              onChange={(e) => setSettings({...settings, pump_id: e.target.value})}
            />
            <small style={{color: '#666', fontSize: '0.9em'}}>Default pump ID is 1. Change only if you have multiple pumps.</small>
          </div>

          <div className="form-group">
            <label>Pump Name</label>
            <input
              type="text"
              placeholder="ZapPay Pump Station"
              value={settings.pump_name}
              onChange={(e) => setSettings({...settings, pump_name: e.target.value})}
            />
          </div>

          <div className="form-group">
            <label>Petrol Price (₹/L)</label>
            <input
              type="number"
              step="0.01"
              placeholder="100.00"
              value={settings.petrol_price}
              onChange={(e) => setSettings({...settings, petrol_price: e.target.value})}
            />
          </div>

          <div className="form-actions">
            <button onClick={handleSave} disabled={loading || !settings.pump_id}>
              {loading ? 'Saving...' : 'Save Settings'}
            </button>
            <button onClick={handleLoad} disabled={loading || !settings.pump_id} className="btn-secondary">
              {loading ? 'Loading...' : 'Load Settings'}
            </button>
          </div>
        </div>
      </div>

      {message && (
        <div className={`message ${message.includes('Failed') || message.includes('failed') ? 'error' : 'success'}`}>
          {message}
        </div>
      )}
    </div>
  );
}

export default Settings;

