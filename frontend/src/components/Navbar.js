import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Navbar() {
  const { isAuthenticated, user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => { logout(); navigate('/login'); };

  return (
    <nav style={s.nav}>
      <div style={s.inner}>
        <Link to="/" style={s.brand}>
          <span style={s.logo}>GC</span>
          <span style={s.brandText}>GoComet Tracker</span>
        </Link>
        <div style={s.links}>
          {isAuthenticated ? (
            <>
              <Link to="/"       style={s.link}>Dashboard</Link>
              <Link to="/create" style={s.link}>New Shipment</Link>
              <Link to="/track"  style={s.link}>Track</Link>
              <span style={s.userBadge}>{user?.firstName}</span>
              <button onClick={handleLogout} style={s.logoutBtn}>Logout</button>
            </>
          ) : (
            <>
              <Link to="/login"    style={s.link}>Login</Link>
              <Link to="/register" style={s.cta}>Register</Link>
            </>
          )}
        </div>
      </div>
    </nav>
  );
}

const s = {
  nav:       { background:'#0f172a', padding:'0 24px', position:'sticky', top:0, zIndex:100 },
  inner:     { maxWidth:1100, margin:'0 auto', display:'flex', alignItems:'center', justifyContent:'space-between', height:56 },
  brand:     { display:'flex', alignItems:'center', gap:10, textDecoration:'none' },
  logo:      { background:'#3b82f6', color:'#fff', fontWeight:700, fontSize:14, padding:'4px 8px', borderRadius:6 },
  brandText: { color:'#f1f5f9', fontWeight:600, fontSize:16 },
  links:     { display:'flex', alignItems:'center', gap:20 },
  link:      { color:'#94a3b8', textDecoration:'none', fontSize:14 },
  cta:       { background:'#3b82f6', color:'#fff', padding:'6px 16px', borderRadius:6, textDecoration:'none', fontSize:14 },
  userBadge: { background:'#1e293b', color:'#60a5fa', padding:'4px 10px', borderRadius:20, fontSize:12 },
  logoutBtn: { background:'none', border:'1px solid #334155', color:'#94a3b8', padding:'5px 12px', borderRadius:6, cursor:'pointer', fontSize:13 },
};
