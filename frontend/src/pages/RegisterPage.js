import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function RegisterPage() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({});
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault(); setError(''); setLoading(true);
    if (form.password !== form.confirmPassword) {
      setError('Passwords do not match'); setLoading(false); return;
    }
    try {
      await register({ email: form.email, password: form.password, firstName: form.firstName, lastName: form.lastName, phone: form.phone });
      navigate('/');
    } catch (err) {
      setError(err.response?.data?.message || 'Registration failed');
    } finally { setLoading(false); }
  };

  const field = (name, label, type = 'text', placeholder = '') => (
    <div style={s.field}>
      <label style={s.label}>{label}</label>
      <input type={type} placeholder={placeholder} required
        value={form[name] || ''}
        onChange={e => setForm(p => ({...p, [name]: e.target.value}))}
        style={s.input} />
    </div>
  );

  return (
    <div style={s.wrap}>
      <div style={s.card}>
        <div style={s.logo}>GC</div>
        <h2 style={s.title}>Create your account</h2>
        {error && <div style={s.error}>{error}</div>}
        <form onSubmit={handleSubmit}>
          <div style={{display:'grid', gridTemplateColumns:'1fr 1fr', gap:12}}>
            {field('firstName','First Name','text','John')}
            {field('lastName', 'Last Name', 'text','Doe')}
          </div>
          {field('email',    'Email',    'email',    'you@example.com')}
          {field('phone',    'Phone',    'tel',      '+91 9876543210')}
          {field('password', 'Password', 'password', '••••••••')}
          {field('confirmPassword','Confirm Password','password','••••••••')}
          <button type="submit" disabled={loading} style={s.btn}>
            {loading ? 'Creating account...' : 'Create Account'}
          </button>
        </form>
        <div style={s.footer}>Already have an account? <Link to="/login" style={{color:'#3b82f6'}}>Sign in</Link></div>
      </div>
    </div>
  );
}

const s = {
  wrap: {display:'flex',justifyContent:'center',alignItems:'center',minHeight:'70vh'},
  card: {background:'#fff',border:'1px solid #e2e8f0',borderRadius:12,padding:'40px 36px',width:'100%',maxWidth:480},
  logo: {background:'#3b82f6',color:'#fff',fontWeight:700,fontSize:18,padding:'8px 14px',borderRadius:8,display:'inline-block',marginBottom:16},
  title:{fontSize:22,fontWeight:600,color:'#0f172a',marginBottom:24},
  error:{background:'#fef2f2',border:'1px solid #fca5a5',color:'#b91c1c',padding:'10px 14px',borderRadius:8,marginBottom:16,fontSize:13},
  field:{marginBottom:14},
  label:{display:'block',fontSize:13,fontWeight:500,color:'#374151',marginBottom:5},
  input:{width:'100%',padding:'10px 12px',border:'1px solid #d1d5db',borderRadius:8,fontSize:14,boxSizing:'border-box'},
  btn:  {width:'100%',background:'#3b82f6',color:'#fff',border:'none',padding:'12px',borderRadius:8,fontSize:15,fontWeight:500,cursor:'pointer',marginTop:8},
  footer:{textAlign:'center',marginTop:16,fontSize:13,color:'#6b7280'},
};
