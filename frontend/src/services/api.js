import axios from 'axios';

const API_BASE = process.env.REACT_APP_API_URL || '/api';

const api = axios.create({
  baseURL: API_BASE,
  timeout: 20000,
  headers: { 'Content-Type': 'application/json' },
});

// Attach JWT on every request
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// Redirect to login on 401
api.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err.response?.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(err);
  }
);

export const authApi = {
  register: (data) => api.post('/users/register', data),
  login:    (data) => api.post('/users/login',    data),
};

export const shipmentApi = {
  create:       (data)     => api.post('/shipments', data),
  getById:      (id)       => api.get(`/shipments/${id}`),
  updateStatus: (id, data) => api.put(`/shipments/${id}/status`, data),
  list:         (params)   => api.get('/shipments', { params }),
};

export const trackingApi = {
  getLive:    (id)            => api.get(`/tracking/${id}/live`),
  getHistory: (id)            => api.get(`/tracking/${id}/history`),
  getByNumber:(trackingNumber)=> api.get(`/tracking/number/${trackingNumber}`),
};

export const predictionApi = {
  predict: (data)  => api.post('/predictions/predict', data),
  getEta:  (id, p) => api.get(`/predictions/${id}/eta`,  { params: p }),
  getRisk: (id, p) => api.get(`/predictions/${id}/risk`, { params: p }),
};

export default api;
