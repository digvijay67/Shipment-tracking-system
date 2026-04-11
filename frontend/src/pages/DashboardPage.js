import React, { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import { shipmentApi } from '../services/api';
import { useAuth } from '../context/AuthContext';

const STATUS_COLORS = {
  PENDING:           { bg:'#fef9c3', text:'#854d0e', label:'Pending' },
  PICKED_UP:         { bg:'#dbeafe', text:'#1e40af', label:'Picked Up' },
  IN_TRANSIT:        { bg:'#e0e7ff', text:'#3730a3', label:'In Transit' },
  OUT_FOR_DELIVERY:  { bg:'#d1fae5', text:'#065f46', label:'Out for Delivery' },
  DELIVERED:         { bg:'#dcfce7', text:'#166534', label:'Delivered' },
  FAILED:            { bg:'#fee2e2', text:'#991b1b', label:'Failed' },
  CANCELLED:         { bg:'#f3f4f6', text:'#4b5563', label:'Cancelled' },
  RETURNED:          { bg:'#fce7f3', text:'#9d174d', label:'Returned' },
};

function StatusBadge({ status }) {
  const c = STATUS_COLORS[status] || { bg:'#f3f4f6', text:'#374151', label: status };
  return (
    <span style={{ background:c.bg, color:c.text, padding:'3px 10px', borderRadius:20, fontSize:12, fontWeight:500 }}>
      {c.label}
    </span>
  );
}

export default function DashboardPage() {
  const { user } = useAuth();
  const [shipments, setShipments] = useState([]);
  const [loading,   setLoading]   = useState(true);
  const [page,      setPage]      = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [filterStatus, setFilterStatus] = useState('');
  const [error, setError] = useState('');

  const fetchShipments = useCallback(async () => {
    setLoading(true); setError('');
    try {
      const res = await shipmentApi.list({ page, size: 10, status: filterStatus || undefined });
      const data = res.data.data;
      setShipments(data.content || []);
      setTotalPages(data.totalPages || 0);
    } catch (err) {
      setError('Failed to load shipments');
    } finally {
      setLoading(false);
    }
  }, [page, filterStatus]);

  useEffect(() => { fetchShipments(); }, [fetchShipments]);

  const counts = shipments.reduce((acc, s) => {
    acc[s.status] = (acc[s.status] || 0) + 1; return acc;
  }, {});

  return (
    <div>
      <div style={s.header}>
        <div>
          <h1 style={s.h1}>Welcome, {user?.firstName}</h1>
          <p style={s.sub}>Manage and track your shipments in real time</p>
        </div>
        <Link to="/create" style={s.createBtn}>+ New Shipment</Link>
      </div>

      {/* Metric cards */}
      <div style={s.metrics}>
        {[
          { label:'Total',       value: shipments.length,        color:'#3b82f6' },
          { label:'In Transit',  value: counts.IN_TRANSIT || 0,  color:'#8b5cf6' },
          { label:'Delivered',   value: counts.DELIVERED  || 0,  color:'#10b981' },
          { label:'Pending',     value: counts.PENDING    || 0,  color:'#f59e0b' },
        ].map(m => (
          <div key={m.label} style={s.metricCard}>
            <div style={{...s.metricValue, color: m.color}}>{m.value}</div>
            <div style={s.metricLabel}>{m.label}</div>
          </div>
        ))}
      </div>

      {/* Filter */}
      <div style={s.filterRow}>
        <select value={filterStatus} onChange={e => { setFilterStatus(e.target.value); setPage(0); }} style={s.select}>
          <option value="">All statuses</option>
          {Object.keys(STATUS_COLORS).map(st => <option key={st} value={st}>{STATUS_COLORS[st].label}</option>)}
        </select>
        <button onClick={fetchShipments} style={s.refreshBtn}>Refresh</button>
      </div>

      {/* Table */}
      {error && <div style={s.error}>{error}</div>}
      {loading ? (
        <div style={s.loading}>Loading shipments...</div>
      ) : shipments.length === 0 ? (
        <div style={s.empty}>
          <p>No shipments yet.</p>
          <Link to="/create" style={s.createBtn}>Create your first shipment</Link>
        </div>
      ) : (
        <>
          <div style={s.tableWrap}>
            <table style={s.table}>
              <thead>
                <tr style={s.thead}>
                  {['Tracking #','Origin','Destination','Weight','Status','Expected Delivery','Actions'].map(h => (
                    <th key={h} style={s.th}>{h}</th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {shipments.map(ship => (
                  <tr key={ship.id} style={s.tr}>
                    <td style={{...s.td, fontFamily:'monospace', fontWeight:600, color:'#3b82f6'}}>{ship.trackingNumber}</td>
                    <td style={s.td}>{ship.origin}</td>
                    <td style={s.td}>{ship.destination}</td>
                    <td style={s.td}>{ship.weightKg} kg</td>
                    <td style={s.td}><StatusBadge status={ship.status} /></td>
                    <td style={s.td}>
                      {ship.expectedDelivery
                        ? new Date(ship.expectedDelivery).toLocaleDateString()
                        : '—'}
                    </td>
                    <td style={s.td}>
                      <Link to={`/track/${ship.id}`} style={s.trackLink}>Track</Link>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {/* Pagination */}
          {totalPages > 1 && (
            <div style={s.pagination}>
              <button disabled={page === 0} onClick={() => setPage(p => p - 1)} style={s.pageBtn}>← Prev</button>
              <span style={s.pageInfo}>Page {page + 1} of {totalPages}</span>
              <button disabled={page >= totalPages - 1} onClick={() => setPage(p => p + 1)} style={s.pageBtn}>Next →</button>
            </div>
          )}
        </>
      )}
    </div>
  );
}

const s = {
  header:      { display:'flex', justifyContent:'space-between', alignItems:'flex-start', marginBottom:28 },
  h1:          { fontSize:26, fontWeight:700, color:'#0f172a', marginBottom:4 },
  sub:         { fontSize:14, color:'#64748b' },
  createBtn:   { background:'#3b82f6', color:'#fff', padding:'10px 20px', borderRadius:8, textDecoration:'none', fontSize:14, fontWeight:500 },
  metrics:     { display:'grid', gridTemplateColumns:'repeat(auto-fit,minmax(160px,1fr))', gap:16, marginBottom:28 },
  metricCard:  { background:'#fff', border:'1px solid #e2e8f0', borderRadius:12, padding:'20px 24px' },
  metricValue: { fontSize:32, fontWeight:700, lineHeight:1 },
  metricLabel: { fontSize:13, color:'#64748b', marginTop:6 },
  filterRow:   { display:'flex', gap:12, marginBottom:16, alignItems:'center' },
  select:      { padding:'8px 12px', border:'1px solid #d1d5db', borderRadius:8, fontSize:13, background:'#fff' },
  refreshBtn:  { padding:'8px 16px', border:'1px solid #d1d5db', borderRadius:8, background:'#fff', cursor:'pointer', fontSize:13 },
  error:       { background:'#fef2f2', color:'#b91c1c', padding:'12px', borderRadius:8, marginBottom:16, fontSize:13 },
  loading:     { textAlign:'center', padding:'60px', color:'#64748b' },
  empty:       { textAlign:'center', padding:'60px', color:'#64748b', display:'flex', flexDirection:'column', alignItems:'center', gap:16 },
  tableWrap:   { overflowX:'auto' },
  table:       { width:'100%', borderCollapse:'collapse', background:'#fff', border:'1px solid #e2e8f0', borderRadius:12, overflow:'hidden' },
  thead:       { background:'#f8fafc' },
  th:          { padding:'12px 16px', textAlign:'left', fontSize:12, fontWeight:600, color:'#64748b', borderBottom:'1px solid #e2e8f0', whiteSpace:'nowrap' },
  tr:          { borderBottom:'1px solid #f1f5f9' },
  td:          { padding:'14px 16px', fontSize:13, color:'#1e293b', verticalAlign:'middle' },
  trackLink:   { color:'#3b82f6', textDecoration:'none', fontSize:13, fontWeight:500 },
  pagination:  { display:'flex', justifyContent:'center', alignItems:'center', gap:16, marginTop:20 },
  pageBtn:     { padding:'8px 16px', border:'1px solid #d1d5db', borderRadius:8, background:'#fff', cursor:'pointer', fontSize:13 },
  pageInfo:    { fontSize:13, color:'#64748b' },
};
