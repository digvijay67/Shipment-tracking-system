import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

function AuthForm({ title, fields, submitLabel, onSubmit, footer }) {
  const [form,  setForm]  = useState({});
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(''); setLoading(true);
    try { await onSubmit(form); }
    catch (err) { setError(err.response?.data?.message || err.message || 'Something went wrong'); }
    finally { setLoading(false); }
  };

  return (
    <div style={s.wrap}>
      <div style={s.card}>
        <div style={s.logo}>GC</div>
        <h2 style={s.title}>{title}</h2>
        {error && <div style={s.error}>{error}</div>}
        <form onSubmit={handleSubmit}>
          {fields.map(f => (
            <div key={f.name} style={s.field}>
              <label style={s.label}>{f.label}</label>
              <input
                type={f.type || 'text'}
                placeholder={f.placeholder}
                required={f.required !== false}
                value={form[f.name] || ''}
                onChange={e => setForm(p => ({...p, [f.name]: e.target.value}))}
                style={s.input}
              />
            </div>
          ))}
          <button type="submit" disabled={loading} style={s.btn}>
            {loading ? 'Please wait...' : submitLabel}
          </button>
        </form>
        <div style={s.footer}>{footer}</div>
      </div>
    </div>
  );
}

export function LoginPage() {
  const { login } = useAuth();
  const navigate  = useNavigate();
  return (
    <AuthForm
      title="Sign in to Tracking"
      submitLabel="Sign In"
      fields={[
        { name:'email',    label:'Email',    type:'email',    placeholder:'you@example.com' },
        { name:'password', label:'Password', type:'password', placeholder:'••••••••' },
      ]}
      onSubmit={async (form) => { await login(form.email, form.password); navigate('/'); }}
      footer={<>No account? <Link to="/register" style={{color:'#3b82f6'}}>Register</Link>
        <br/><small style={{color:'#64748b'}}>Demo: demo@gocomet.com / demo123</small></>}
    />
  );
}
export default LoginPage;

const s = {
  wrap:  { display:'flex', justifyContent:'center', alignItems:'center', minHeight:'70vh' },
  card:  { background:'#fff', border:'1px solid #e2e8f0', borderRadius:12, padding:'40px 36px', width:'100%', maxWidth:420 },
  logo:  { background:'#3b82f6', color:'#fff', fontWeight:700, fontSize:18, padding:'8px 14px', borderRadius:8, display:'inline-block', marginBottom:16 },
  title: { fontSize:22, fontWeight:600, color:'#0f172a', marginBottom:24 },
  error: { background:'#fef2f2', border:'1px solid #fca5a5', color:'#b91c1c', padding:'10px 14px', borderRadius:8, marginBottom:16, fontSize:13 },
  field: { marginBottom:18 },
  label: { display:'block', fontSize:13, fontWeight:500, color:'#374151', marginBottom:6 },
  input: { width:'100%', padding:'10px 12px', border:'1px solid #d1d5db', borderRadius:8, fontSize:14, outline:'none', boxSizing:'border-box' },
  btn:   { width:'100%', background:'#3b82f6', color:'#fff', border:'none', padding:'12px', borderRadius:8, fontSize:15, fontWeight:500, cursor:'pointer', marginTop:8 },
  footer:{ textAlign:'center', marginTop:20, fontSize:13, color:'#6b7280', lineHeight:1.8 },
};
