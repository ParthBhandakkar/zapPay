import { useState } from 'react';
import { customerAPI } from '../../utils/api';
import './Customer.css';

function Auth({ onLogin }) {
  const [mode, setMode] = useState('login'); // 'login' or 'signup'
  const [loginMethod, setLoginMethod] = useState('password'); // 'password' or 'otp'
  
  // Signup state
  const [signupData, setSignupData] = useState({
    full_name: '',
    phone_number: '',
    password: '',
    otp_code: ''
  });
  const [otpSent, setOtpSent] = useState(false);
  
  // Login state
  const [loginData, setLoginData] = useState({
    phone_number: '',
    password: '',
    otp_code: ''
  });
  const [loginOtpSent, setLoginOtpSent] = useState(false);
  
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');

  const handleSignupSendOtp = async () => {
    try {
      setLoading(true);
      setMessage('');
      const { data } = await customerAPI.post('/auth/register/otp/start', {
        full_name: signupData.full_name,
        phone_number: signupData.phone_number,
        role: 'customer'
      });
      setOtpSent(true);
      setMessage(data.otp_debug ? `OTP: ${data.otp_debug}` : 'OTP sent! Check server logs.');
    } catch (error) {
      setMessage(error.response?.data?.detail || 'Failed to send OTP');
    } finally {
      setLoading(false);
    }
  };

  const handleSignupComplete = async () => {
    try {
      setLoading(true);
      setMessage('');
      const { data } = await customerAPI.post('/auth/register/otp/complete', {
        ...signupData,
        role: 'customer'
      });
      onLogin({ access: data.access_token, refresh: data.refresh_token });
    } catch (error) {
      setMessage(error.response?.data?.detail || 'Signup failed');
    } finally {
      setLoading(false);
    }
  };

  const handlePasswordLogin = async () => {
    try {
      setLoading(true);
      setMessage('');
      const formData = new FormData();
      formData.append('username', loginData.phone_number);
      formData.append('password', loginData.password);
      
      const { data } = await customerAPI.post('/auth/login', formData);
      onLogin({ access: data.access_token, refresh: data.refresh_token });
    } catch (error) {
      setMessage(error.response?.data?.detail || 'Login failed');
    } finally {
      setLoading(false);
    }
  };

  const handleOtpLoginSend = async () => {
    try {
      setLoading(true);
      setMessage('');
      const { data } = await customerAPI.post('/auth/send-otp', {
        phone_number: loginData.phone_number,
        otp_type: 'login'
      });
      setLoginOtpSent(true);
      setMessage(data.otp_debug ? `OTP: ${data.otp_debug}` : 'OTP sent!');
    } catch (error) {
      setMessage(error.response?.data?.detail || 'Failed to send OTP');
    } finally {
      setLoading(false);
    }
  };

  const handleOtpLogin = async () => {
    try {
      setLoading(true);
      setMessage('');
      const { data } = await customerAPI.post('/auth/login/otp', {
        phone_number: loginData.phone_number,
        otp_code: loginData.otp_code
      });
      onLogin({ access: data.access_token, refresh: data.refresh_token });
    } catch (error) {
      setMessage(error.response?.data?.detail || 'Login failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-container">
      <div className="auth-card">
        <h1>Customer Portal</h1>
        
        <div className="mode-toggle">
          <button 
            className={mode === 'login' ? 'active' : ''}
            onClick={() => setMode('login')}
          >
            Login
          </button>
          <button 
            className={mode === 'signup' ? 'active' : ''}
            onClick={() => setMode('signup')}
          >
            Sign Up
          </button>
        </div>

        {mode === 'signup' ? (
          <div className="form">
            <input
              type="text"
              placeholder="Full Name"
              value={signupData.full_name}
              onChange={(e) => setSignupData({...signupData, full_name: e.target.value})}
            />
            <input
              type="tel"
              placeholder="Phone Number"
              value={signupData.phone_number}
              onChange={(e) => setSignupData({...signupData, phone_number: e.target.value})}
            />
            <input
              type="password"
              placeholder="Password"
              value={signupData.password}
              onChange={(e) => setSignupData({...signupData, password: e.target.value})}
            />
            
            {!otpSent ? (
              <button onClick={handleSignupSendOtp} disabled={loading}>
                {loading ? 'Sending...' : 'Send OTP'}
              </button>
            ) : (
              <>
                <input
                  type="text"
                  placeholder="Enter OTP"
                  value={signupData.otp_code}
                  onChange={(e) => setSignupData({...signupData, otp_code: e.target.value})}
                />
                <button onClick={handleSignupComplete} disabled={loading}>
                  {loading ? 'Creating Account...' : 'Complete Signup'}
                </button>
              </>
            )}
          </div>
        ) : (
          <div className="form">
            <div className="login-method-toggle">
              <button 
                className={loginMethod === 'password' ? 'active' : ''}
                onClick={() => setLoginMethod('password')}
              >
                Password
              </button>
              <button 
                className={loginMethod === 'otp' ? 'active' : ''}
                onClick={() => setLoginMethod('otp')}
              >
                OTP
              </button>
            </div>

            <input
              type="tel"
              placeholder="Phone Number"
              value={loginData.phone_number}
              onChange={(e) => setLoginData({...loginData, phone_number: e.target.value})}
            />

            {loginMethod === 'password' ? (
              <>
                <input
                  type="password"
                  placeholder="Password"
                  value={loginData.password}
                  onChange={(e) => setLoginData({...loginData, password: e.target.value})}
                />
                <button onClick={handlePasswordLogin} disabled={loading}>
                  {loading ? 'Logging in...' : 'Login'}
                </button>
              </>
            ) : (
              <>
                {!loginOtpSent ? (
                  <button onClick={handleOtpLoginSend} disabled={loading}>
                    {loading ? 'Sending...' : 'Send OTP'}
                  </button>
                ) : (
                  <>
                    <input
                      type="text"
                      placeholder="Enter OTP"
                      value={loginData.otp_code}
                      onChange={(e) => setLoginData({...loginData, otp_code: e.target.value})}
                    />
                    <button onClick={handleOtpLogin} disabled={loading}>
                      {loading ? 'Logging in...' : 'Login with OTP'}
                    </button>
                  </>
                )}
              </>
            )}
          </div>
        )}

        {message && (
          <div className={`message ${message.includes('failed') || message.includes('Failed') ? 'error' : 'success'}`}>
            {message}
          </div>
        )}
      </div>
    </div>
  );
}

export default Auth;

