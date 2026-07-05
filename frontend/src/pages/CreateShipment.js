import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { shipmentApi } from '../services/api';

const CARRIERS = ['GoComet Logistics','Express','Priority','Standard','Economy'];

export default function CreateShipment() {
  const navigate = useNavigate();
  const [form, setForm] = useState({ origin:'', destination:'', senderName:'', receiverName:'', receiverPhone:'', weightKg:'', distanceKm:'', carrier:'GoComet Logistics', expectedDelivery:'', notes:'' });
  const [loading, setLoading] = useState(false);
  const [error,   setError]   = useState('');
  const [success, setSuccess] = useState(null);

  const set = (k, v) => setForm(p => ({...p, [k]: v}));

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (loading) return;
    setError('');
    setLoading(true);
    try {
      const payload = { ...form, weightKg: parseFloat(form.weightKg), distanceKm: form.distanceKm ? parseFloat(form.distanceKm) : null, expectedDelivery: form.expectedDelivery || null };
      const res = await shipmentApi.create(payload);
      setSuccess(res.data.data);
    } catch (err) {
      const message = err?.response?.data?.message || 'Failed to create shipment';
      setError(message);
    } finally { setLoading(false); }
  };

  if (success) {
    return (
      <div style={{display:'flex',justifyContent:'center',paddingTop:40}}>
        <div style={{background:'#fff',border:'1px solid #e2e8f0',borderRadius:16,padding:40,maxWidth:520,width:'100%',textAlign:'center'}}>
          <div style={{width:56,height:56,background:'#dcfce7',color:'#16a34a',borderRadius:'50%',display:'flex',alignItems:'center',justifyContent:'center',fontSize:24,fontWeight:700,margin:'0 auto 16px'}}>✓</div>
          <h2 style={{fontSize:22,fontWeight:700,color:'#0f172a',marginBottom:8}}>Shipment Created!</h2>
          <p style={{fontSize:14,color:'#64748b',marginBottom:24}}>Your shipment is registered and being tracked.</p>
          <div style={{background:'#eff6ff',border:'1px solid #bfdbfe',borderRadius:10,padding:16,marginBottom:24}}>
            <div style={{fontSize:11,color:'#6b7280',fontWeight:500,textTransform:'uppercase',letterSpacing:1,marginBottom:4}}>Tracking Number</div>
            <div style={{fontSize:24,fontWeight:700,color:'#1d4ed8',fontFamily:'monospace'}}>{success.trackingNumber}</div>
          </div>
          <div style={{display:'grid',gridTemplateColumns:'1fr 1fr',gap:0,border:'1px solid #e2e8f0',borderRadius:10,overflow:'hidden',marginBottom:24,textAlign:'left'}}>
            {[['From', success.origin],['To', success.destination],['Weight', `${success.weightKg} kg`],['Carrier', success.carrier]].map(([l,v]) => (
              <div key={l} style={{padding:'12px 16px',borderBottom:'1px solid #f1f5f9'}}>
                <div style={{fontSize:11,color:'#94a3b8',marginBottom:2}}>{l}</div>
                <div style={{fontSize:13,fontWeight:500,color:'#0f172a'}}>{v}</div>
              </div>
            ))}
          </div>
          <div style={{display:'flex',gap:12,justifyContent:'center',flexWrap:'wrap'}}>
            <button onClick={() => navigate(`/track/${success.id}`)} style={{background:'#3b82f6',color:'#fff',border:'none',padding:'11px 24px',borderRadius:8,fontSize:14,cursor:'pointer',fontWeight:500}}>Track Shipment</button>
            <button onClick={() => { setSuccess(null); setForm({ origin:'',destination:'',senderName:'',receiverName:'',receiverPhone:'',weightKg:'',distanceKm:'',carrier:'GoComet Logistics',expectedDelivery:'',notes:'' }); }} style={{background:'#eff6ff',color:'#3b82f6',border:'1px solid #bfdbfe',padding:'11px 20px',borderRadius:8,fontSize:14,cursor:'pointer'}}>Create Another</button>
            <button onClick={() => navigate('/')} style={{background:'none',color:'#64748b',border:'1px solid #d1d5db',padding:'11px 20px',borderRadius:8,fontSize:14,cursor:'pointer'}}>Dashboard</button>
          </div>
        </div>
      </div>
    );
  }

  const inp = {padding:'10px 12px',border:'1px solid #d1d5db',borderRadius:8,fontSize:14,width:'100%',boxSizing:'border-box'};

  return (
    <div>
      <h1 style={{fontSize:26,fontWeight:700,color:'#0f172a',marginBottom:6}}>Create New Shipment</h1>
      <p style={{fontSize:14,color:'#64748b',marginBottom:28}}>Fill in the details below to register and start tracking your shipment.</p>
      {error && <div style={{background:'#fef2f2',border:'1px solid #fca5a5',color:'#b91c1c',padding:'12px 16px',borderRadius:8,marginBottom:20,fontSize:13}}>{error}</div>}
      <form onSubmit={handleSubmit} style={{display:'flex',flexDirection:'column',gap:20}}>

        {/* Route */}
        <div style={{background:'#fff',border:'1px solid #e2e8f0',borderRadius:12,padding:24}}>
          <h3 style={{fontSize:15,fontWeight:600,marginBottom:18,paddingBottom:12,borderBottom:'1px solid #f1f5f9',color:'#0f172a'}}>Route Details</h3>
          <div style={{display:'grid',gridTemplateColumns:'repeat(auto-fit,minmax(200px,1fr))',gap:16,marginBottom:16}}>
            <div><label style={{fontSize:13,fontWeight:500,color:'#374151',display:'block',marginBottom:5}}>Origin *</label><input required value={form.origin} onChange={e=>set('origin',e.target.value)} placeholder="Mumbai, India" style={inp}/></div>
            <div><label style={{fontSize:13,fontWeight:500,color:'#374151',display:'block',marginBottom:5}}>Destination *</label><input required value={form.destination} onChange={e=>set('destination',e.target.value)} placeholder="Delhi, India" style={inp}/></div>
          </div>
          <div style={{display:'grid',gridTemplateColumns:'repeat(auto-fit,minmax(200px,1fr))',gap:16}}>
            <div><label style={{fontSize:13,fontWeight:500,color:'#374151',display:'block',marginBottom:5}}>Distance (km)</label><input type="number" value={form.distanceKm} onChange={e=>set('distanceKm',e.target.value)} placeholder="1400" style={inp}/></div>
            <div><label style={{fontSize:13,fontWeight:500,color:'#374151',display:'block',marginBottom:5}}>Carrier</label>
              <select value={form.carrier} onChange={e=>set('carrier',e.target.value)} style={{...inp,background:'#fff'}}>{CARRIERS.map(c=><option key={c}>{c}</option>)}</select>
            </div>
          </div>
        </div>

        {/* People */}
        <div style={{background:'#fff',border:'1px solid #e2e8f0',borderRadius:12,padding:24}}>
          <h3 style={{fontSize:15,fontWeight:600,marginBottom:18,paddingBottom:12,borderBottom:'1px solid #f1f5f9',color:'#0f172a'}}>Sender & Receiver</h3>
          <div style={{display:'grid',gridTemplateColumns:'repeat(auto-fit,minmax(200px,1fr))',gap:16,marginBottom:16}}>
            <div><label style={{fontSize:13,fontWeight:500,color:'#374151',display:'block',marginBottom:5}}>Sender Name *</label><input required value={form.senderName} onChange={e=>set('senderName',e.target.value)} placeholder="John Doe" style={inp}/></div>
            <div><label style={{fontSize:13,fontWeight:500,color:'#374151',display:'block',marginBottom:5}}>Receiver Name *</label><input required value={form.receiverName} onChange={e=>set('receiverName',e.target.value)} placeholder="Jane Smith" style={inp}/></div>
          </div>
          <div style={{maxWidth:280}}>
            <label style={{fontSize:13,fontWeight:500,color:'#374151',display:'block',marginBottom:5}}>Receiver Phone</label>
            <input value={form.receiverPhone} onChange={e=>set('receiverPhone',e.target.value)} placeholder="+91 9876543210" style={inp}/>
          </div>
        </div>

        {/* Package */}
        <div style={{background:'#fff',border:'1px solid #e2e8f0',borderRadius:12,padding:24}}>
          <h3 style={{fontSize:15,fontWeight:600,marginBottom:18,paddingBottom:12,borderBottom:'1px solid #f1f5f9',color:'#0f172a'}}>Package Details</h3>
          <div style={{display:'grid',gridTemplateColumns:'repeat(auto-fit,minmax(200px,1fr))',gap:16,marginBottom:16}}>
            <div><label style={{fontSize:13,fontWeight:500,color:'#374151',display:'block',marginBottom:5}}>Weight (kg) *</label><input required type="number" step="0.1" value={form.weightKg} onChange={e=>set('weightKg',e.target.value)} placeholder="12.5" style={inp}/></div>
            <div><label style={{fontSize:13,fontWeight:500,color:'#374151',display:'block',marginBottom:5}}>Expected Delivery</label><input type="datetime-local" value={form.expectedDelivery} onChange={e=>set('expectedDelivery',e.target.value)} style={inp}/></div>
          </div>
          <div>
            <label style={{fontSize:13,fontWeight:500,color:'#374151',display:'block',marginBottom:5}}>Notes</label>
            <textarea value={form.notes} onChange={e=>set('notes',e.target.value)} placeholder="Fragile — handle with care..." rows={3} style={{...inp,resize:'vertical',fontFamily:'inherit'}}/>
          </div>
        </div>

        <div style={{display:'flex',gap:12}}>
          <button type="submit" disabled={loading} style={{background:'#3b82f6',color:'#fff',border:'none',padding:'12px 32px',borderRadius:8,fontSize:15,fontWeight:500,cursor:'pointer'}}>
            {loading ? 'Creating...' : 'Create Shipment'}
          </button>
          <button type="button" onClick={() => navigate('/')} style={{background:'none',color:'#64748b',border:'1px solid #d1d5db',padding:'12px 24px',borderRadius:8,fontSize:14,cursor:'pointer'}}>Cancel</button>
        </div>
      </form>
    </div>
  );
}
