import axios from 'axios';

// Derive sensible defaults from current host so phones on the same Wi‑Fi work without ngrok.
const currentHost =
	typeof window !== 'undefined' && window.location && window.location.hostname
		? window.location.hostname
		: 'localhost';

// Auto-detect protocol (use https if page is loaded over https, like ngrok)
const currentProtocol = 
	typeof window !== 'undefined' && window.location && window.location.protocol
		? window.location.protocol
		: 'http:';

const DEFAULT_CUSTOMER_API = `${currentProtocol}//${currentHost}:8000`;
const DEFAULT_PUMP_API = `${currentProtocol}//${currentHost}:8001`;

// Environment overrides if provided
const CUSTOMER_API_BASE = import.meta.env.VITE_CUSTOMER_API || DEFAULT_CUSTOMER_API;
const PUMP_API_BASE = import.meta.env.VITE_PUMP_API || DEFAULT_PUMP_API;

export const customerAPI = axios.create({
  baseURL: `${CUSTOMER_API_BASE}/api/v1`,
});

export const pumpAPI = axios.create({
  baseURL: PUMP_API_BASE,
});

// Request interceptor to add auth token
customerAPI.interceptors.request.use((config) => {
  const tokens = localStorage.getItem('zappay.customer.tokens');
  if (tokens) {
    const { access } = JSON.parse(tokens);
    config.headers.Authorization = `Bearer ${access}`;
  }
  return config;
});

pumpAPI.interceptors.request.use((config) => {
  const tokens = localStorage.getItem('zappay.pump.tokens');
  if (tokens) {
    const { access } = JSON.parse(tokens);
    config.headers.Authorization = `Bearer ${access}`;
  }
  return config;
});

// Response interceptor for error handling
const handleError = (error) => {
  if (error.response?.status === 401) {
    // Token expired, clear and redirect
    localStorage.removeItem('zappay.customer.tokens');
    localStorage.removeItem('zappay.pump.tokens');
  }
  return Promise.reject(error);
};

customerAPI.interceptors.response.use((response) => response, handleError);
pumpAPI.interceptors.response.use((response) => response, handleError);

export { CUSTOMER_API_BASE, PUMP_API_BASE };

