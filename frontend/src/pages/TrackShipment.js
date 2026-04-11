import React, { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { shipmentApi, trackingApi, predictionApi } from '../services/api';

const STATUS_META = {
  PENDING:          { color:'#f59e0b', label:'Pending',           step:0 },
  PICKED_UP:        { color:'#3b82f6', label:'Picked Up',         step:1 },
  IN_TRANSIT:       { color:'#8b5cf6', label:'In Transit',        step:2 },
  OUT_FOR_DELIVERY: { color:'#06b6d4', label:'Out for Delivery',  step:3 },
  DELIVERED:        { color:'#10b981', label:'Delivered',         step:4 },
  FAILED:           { color:'#ef4444', label:'Failed',            step:-1 },
  CANCELLED:        { color:'#6b7280', label:'Cancelled',         step:-1 },
  RETURNED:         { color:'#ec4899', label:'Returned',          step:-1 },
};
const STEPS = ['PENDING','PICKED_UP','IN_TRANSIT','OUT_FOR_DELIVERY','DELIVERED'];
const RISK_CONFIG = {
  LOW:    { bg:'#dcfce7', text:'#166534', border:'#86efac', label:'Low Risk',    icon:'OK' },
  MEDIUM: { bg:'#fef9c3', text:'#854d0e', border:'#fde047', label:'Medium Risk', icon:'!' },
  HIGH:   { bg:'#fee2e2', text:'#991b1b', border:'#fca5a5', label:'High Risk',   icon:'!!' },
};

function StatusPill({ status, small }) {
  const meta = STATUS_META[status] || { color:'#64748b', label: status };
  const size = small ? { fontSize:11, padding:'2px 8px' } : { fontSize:13, padding:'5px 14px' };
  return (
    <span style={{ background: meta.color + '22', color: meta.color, border: `1px solid ${meta.color}44`, borderRadius:20, fontWeight:500, ...size }}>
      {meta.label}
    </span>
  );
}

export default function TrackShipment() {
  const { id }    = useParams();
  const navigate  = useNavigate();
  const [searchId, setSearchId]    = useState(id || '');
  const [shipment, setShipment]    = useState(null);
  const [history,  setHistory]     = useState([]);
  const [prediction, setPrediction]= useState(null);
  const [loading,  setLoading]     = useState(false);
  const [error,    setError]       = useState('');

  const loadShipment = useCallback(async (targetId) => {
    if (!targetId) return;
    setLoading(true); setError('');
    try {
      const [sRes, hRes] = await Promise.allSettled([
        shipmentApi.getById(targetId),
        trackingApi.getHistory(targetId),
      ]);
      const ship = sRes.status === 'fulfilled' ? sRes.value.data.data : null;
      if (!ship) { setError('Shipment not found.'); setLoading(false); return; }
      setShipment(ship);
      const hist = hRes.status === 'fulfilled' ? (hRes.value.data.events || []) : [];
      setHistory(hist);
      try {
        const pRes = await predictionApi.predict({
          shipmentId: ship.id, origin: ship.origin, destination: ship.destination,
          distanceKm: ship.distanceKm, weightKg: ship.weightKg, carrier: ship.carrier,
          status: ship.status, shipmentDate: ship.createdAt, expectedDelivery: ship.expectedDelivery,
        });
        setPrediction(pRes.data.data);
      } catch { setPrediction(null); }
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load shipment.');
    } finally { setLoading(false); }
  }, []);

  useEffect(() => { if (id) loadShipment(id); }, [id, loadShipment]);

  const handleSearch = (e) => {
    e.preventDefault();
    if (searchId.trim()) { navigate('/track/' + searchId.trim()); loadShipment(searchId.trim()); }
  };

  const currentStep = shipment ? (STATUS_META[shipment.status]?.step ?? 0) : -1;
  const risk = prediction ? RISK_CONFIG[prediction.riskLevel] : null;

  return (
    <div>
      <h1 style={{fontSize:26,fontWeight:700,color:'#0f172a',marginBottom:6}}>Track Shipment</h1>
      <p style={{fontSize:14,color:'#64748b',marginBottom:24}}>Real-time tracking with AI-powered ETA and risk prediction.</p>

      <form onSubmit={handleSearch} style={{display:'flex',gap:12,marginBottom:28,maxWidth:560}}>
        <input value={searchId} onChange={e=>setSearchId(e.target.value)}
          placeholder="Enter shipment ID..."
          style={{flex:1,padding:'11px 14px',border:'1px solid #d1d5db',borderRadius:8,fontSize:14,outline:'none'}}/>
        <button type="submit" style={{background:'#3b82f6',color:'#fff',border:'none',padding:'11px 24px',borderRadius:8,fontSize:14,fontWeight:500,cursor:'pointer'}}>Track</button>
      </form>

      {loading && <div style={{textAlign:'center',padding:60,color:'#64748b'}}>Loading shipment data...</div>}
      {error   && <div style={{background:'#fef2f2',border:'1px solid #fca5a5',color:'#b91c1c',padding:14,borderRadius:8,fontSize:13}}>{error}</div>}

      {shipment && !loading && (
        <div style={{display:'flex',flexDirection:'column',gap:20}}>

          {/* Header */}
          <div style={{background:'#fff',border:'1px solid #e2e8f0',borderRadius:12,padding:24}}>
            <div style={{display:'flex',justifyContent:'space-between',alignItems:'flex-start',flexWrap:'wrap',gap:12,marginBottom:20}}>
              <div>
                <div style={{fontSize:11,color:'#94a3b8',marginBottom:4,letterSpacing:1}}>TRACKING NUMBER</div>
                <div style={{fontSize:22,fontWeight:700,color:'#1d4ed8',fontFamily:'monospace'}}>{shipment.trackingNumber}</div>
              </div>
              <StatusPill status={shipment.status}/>
            </div>
            <div style={{display:'grid',gridTemplateColumns:'repeat(auto-fit,minmax(140px,1fr))',gap:16}}>
              {[['From',shipment.origin],['To',shipment.destination],['Sender',shipment.senderName],['Receiver',shipment.receiverName],['Weight',shipment.weightKg+' kg'],['Carrier',shipment.carrier]].map(([l,v])=>(
                <div key={l}>
                  <div style={{fontSize:11,color:'#94a3b8',marginBottom:3}}>{l}</div>
                  <div style={{fontSize:13,fontWeight:500,color:'#0f172a'}}>{v||'—'}</div>
                </div>
              ))}
            </div>
          </div>

          {/* Progress stepper */}
          <div style={{background:'#fff',border:'1px solid #e2e8f0',borderRadius:12,padding:24}}>
            <h3 style={{fontSize:15,fontWeight:600,color:'#0f172a',marginBottom:20}}>Shipment Progress</h3>
            <div style={{display:'flex',alignItems:'center'}}>
              {STEPS.map((step, i) => {
                const meta   = STATUS_META[step];
                const done   = currentStep > i;
                const active = currentStep === i;
                const isLast = i === STEPS.length - 1;
                return (
                  <React.Fragment key={step}>
                    <div style={{display:'flex',flexDirection:'column',alignItems:'center',flex:isLast?0:1,minWidth:0}}>
                      <div style={{width:32,height:32,borderRadius:'50%',background:done?'#10b981':active?meta.color:'#e2e8f0',display:'flex',alignItems:'center',justifyContent:'center',fontSize:13,fontWeight:700,color:(done||active)?'#fff':'#94a3b8',flexShrink:0}}>
                        {done ? '✓' : i+1}
                      </div>
                      <div style={{fontSize:10,color:active?meta.color:done?'#10b981':'#94a3b8',marginTop:6,textAlign:'center',fontWeight:active?600:400,whiteSpace:'nowrap'}}>{meta.label}</div>
                    </div>
                    {!isLast && <div style={{flex:1,height:2,background:done?'#10b981':'#e2e8f0',marginBottom:20,minWidth:8}}/>}
                  </React.Fragment>
                );
              })}
            </div>
          </div>

          {/* AI cards */}
          {prediction && (
            <div style={{display:'grid',gridTemplateColumns:'repeat(auto-fit,minmax(260px,1fr))',gap:16}}>
              <div style={{background:'#fff',border:'1px solid #e2e8f0',borderRadius:12,padding:24}}>
                <div style={{display:'flex',justifyContent:'space-between',alignItems:'center',marginBottom:16}}>
                  <h3 style={{fontSize:15,fontWeight:600,color:'#0f172a'}}>AI ETA Prediction</h3>
                  <span style={{fontSize:11,background:'#eff6ff',color:'#3b82f6',padding:'2px 8px',borderRadius:20}}>AI</span>
                </div>
                <div style={{fontSize:38,fontWeight:700,color:'#1d4ed8',lineHeight:1,marginBottom:4}}>{prediction.predictedEtaHours}h</div>
                <div style={{fontSize:13,color:'#64748b',marginBottom:14}}>Estimated transit time</div>
                <div style={{fontSize:13,color:'#374151',marginBottom:6}}>Arrival by: <strong>{new Date(prediction.predictedEtaDate).toLocaleString()}</strong></div>
                <div style={{fontSize:12,color:'#64748b'}}>Confidence: <strong style={{color:'#0f172a'}}>{Math.round(prediction.confidenceScore*100)}%</strong></div>
                {prediction.breakdown && (
                  <div style={{marginTop:16,padding:12,background:'#f8fafc',borderRadius:8}}>
                    <div style={{fontSize:11,color:'#64748b',fontWeight:600,marginBottom:8}}>BREAKDOWN</div>
                    {[['Distance',`${Math.round(prediction.breakdown.distanceKm)} km`],['Avg Speed',`${prediction.breakdown.avgSpeedKmh} km/h`],['Traffic ×',prediction.breakdown.trafficFactor?.toFixed(2)],['Weather ×',prediction.breakdown.weatherFactor?.toFixed(2)],['Carrier ×',prediction.breakdown.carrierFactor?.toFixed(2)]].map(([l,v])=>(
                      <div key={l} style={{display:'flex',justifyContent:'space-between',fontSize:12,marginBottom:3}}>
                        <span style={{color:'#64748b'}}>{l}</span><span style={{fontWeight:500,color:'#374151'}}>{v}</span>
                      </div>
                    ))}
                  </div>
                )}
              </div>

              {risk && (
                <div style={{background:'#fff',border:'1px solid #e2e8f0',borderRadius:12,padding:24}}>
                  <div style={{display:'flex',justifyContent:'space-between',alignItems:'center',marginBottom:16}}>
                    <h3 style={{fontSize:15,fontWeight:600,color:'#0f172a'}}>Delay Risk Assessment</h3>
                    <span style={{fontSize:11,background:'#eff6ff',color:'#3b82f6',padding:'2px 8px',borderRadius:20}}>AI</span>
                  </div>
                  <div style={{background:risk.bg,border:`1px solid ${risk.border}`,borderRadius:10,padding:16,marginBottom:16,textAlign:'center'}}>
                    <div style={{fontSize:26,fontWeight:700,color:risk.text,marginBottom:4}}>{risk.icon} {risk.label}</div>
                    <div style={{fontSize:13,color:risk.text}}>Score: {prediction.riskScore}/100</div>
                  </div>
                  <div style={{height:8,background:'#e2e8f0',borderRadius:4,overflow:'hidden',marginBottom:14}}>
                    <div style={{height:'100%',width:`${prediction.riskScore}%`,background:prediction.riskLevel==='HIGH'?'#ef4444':prediction.riskLevel==='MEDIUM'?'#f59e0b':'#10b981',borderRadius:4}}/>
                  </div>
                  <div style={{fontSize:11,color:'#94a3b8',fontWeight:600,marginBottom:6}}>RISK FACTORS</div>
                  <p style={{fontSize:13,color:'#374151',lineHeight:1.7}}>{prediction.riskFactors}</p>
                </div>
              )}
            </div>
          )}

          {/* Timeline */}
          {history.length > 0 && (
            <div style={{background:'#fff',border:'1px solid #e2e8f0',borderRadius:12,padding:24}}>
              <h3 style={{fontSize:15,fontWeight:600,color:'#0f172a',marginBottom:20}}>Event History</h3>
              <div style={{display:'flex',flexDirection:'column',gap:0}}>
                {[...history].reverse().map((evt, i) => (
                  <div key={evt.id||i} style={{display:'flex',gap:16,paddingBottom:i<history.length-1?20:0}}>
                    <div style={{display:'flex',flexDirection:'column',alignItems:'center'}}>
                      <div style={{width:12,height:12,borderRadius:'50%',background:i===0?'#3b82f6':'#d1d5db',flexShrink:0,marginTop:3}}/>
                      {i<history.length-1 && <div style={{width:2,flex:1,background:'#e2e8f0',marginTop:4}}/>}
                    </div>
                    <div style={{flex:1}}>
                      <div style={{display:'flex',justifyContent:'space-between',flexWrap:'wrap',gap:8,marginBottom:4}}>
                        <span style={{fontSize:13,fontWeight:600,color:'#0f172a'}}>{evt.description||evt.eventType}</span>
                        <span style={{fontSize:11,color:'#94a3b8'}}>{new Date(evt.createdAt).toLocaleString()}</span>
                      </div>
                      <StatusPill status={evt.status} small/>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}

        </div>
      )}
    </div>
  );
}
