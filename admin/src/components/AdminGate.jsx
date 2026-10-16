import { useEffect, useState } from 'react';
import { ShieldCheck, Home, LogOut } from 'lucide-react';
import { isSupabaseConfigured, supabase } from '../lib/supabase.js';
import {setTestWorkspace} from '../lib/workspace.js';
export function AdminGate({children}) {
  const [user,setUser]=useState(null),[testing,setTesting]=useState(false),[loading,setLoading]=useState(isSupabaseConfigured);
  const [mode,setMode]=useState('live'),[email,setEmail]=useState(''),[password,setPassword]=useState(''),[error,setError]=useState(''),[busy,setBusy]=useState(false);
  useEffect(()=>{
    if(!supabase)return; let mounted=true;
    Promise.all([supabase.auth.getUser(),fetch('/api/test-session').then(r=>r.ok?r.json():{}).catch(()=>({}))]).then(([auth,test])=>{
      if(mounted){setUser(auth.data.user);setTestWorkspace(test.authenticated);setTesting(!!test.authenticated);setLoading(false);}
    }).catch(()=>{if(mounted)setLoading(false);});
    const {data:{subscription}}=supabase.auth.onAuthStateChange((_event,session)=>{if(mounted)setUser(session?.user??null);});
    return ()=>{mounted=false;subscription.unsubscribe();};
  },[]);
  if(!isSupabaseConfigured)return children;
  if(loading)return <div className="auth-screen"><div className="spinner" role="status">Checking your session-</div></div>;
  async function logout(){if(testing){await fetch('/api/test-session',{method:'DELETE'});setTestWorkspace(false);setTesting(false);}else await supabase.auth.signOut();}
  if(testing||user?.app_metadata?.role==='admin')return <>{testing&&<div className="test-workspace-banner">V1 TEST WORKSPACE - Sample data - Changes stay in this browser - No connection to the APK</div>}{children}<button className="signout" onClick={logout}><LogOut size={14}/> Sign out</button></>;
  async function login(event){event.preventDefault();setBusy(true);setError('');try{
    if(mode==='test'){
      const res=await fetch('/api/test-session',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({email,password})});
      const result=await res.json();if(!res.ok||!result.authenticated)throw Error('Sign-in failed. Check your test email and password.');
      setTestWorkspace(true);setTesting(true);
    } else {const {error}=await supabase.auth.signInWithPassword({email,password});if(error)throw Error('Sign-in failed. Check your email and password.');}
  }catch(e){setError(e.message||'Unable to reach sign-in service.');}finally{setPassword('');setBusy(false);}}
  return <main className="auth-screen"><section className="auth-card">
    <div className="brand-mark"><Home size={22}/></div><div className="auth-eyebrow">0BROCKER WORKSPACE</div>
    <h1>{user?'Administrator access required':'Welcome back.'}</h1>
    <p>{user?'This account does not have an administrator role.':'Sign in to manage your marketplace.'}</p>
    {user?<button className="btn btn-primary" onClick={logout}>Use another account</button>:<>
      <div className="auth-modes"><button type="button" className={'btn '+(mode==='live'?'btn-primary':'')} onClick={()=>{setMode('live');setError('');}}>Live admin</button><button type="button" className={'btn '+(mode==='test'?'btn-primary':'')} onClick={()=>{setMode('test');setError('');}}>Test workspace</button></div>
      {mode==='test'&&<p>Isolated sample data for v1 testing. No live users, payments or APK records are modified.</p>}
      <form onSubmit={login}>
        <label>Email address<input type="email" autoComplete="username" required value={email} onChange={e=>setEmail(e.target.value)} placeholder="you@example.com"/></label>
        <label>Password<input type="password" autoComplete="current-password" required value={password} onChange={e=>setPassword(e.target.value)}/></label>
        {error&&<div role="alert" className="banner banner-danger">{error}</div>}
        <button className="btn btn-primary" disabled={busy}>{busy?'Signing in-':'Sign in'} <ShieldCheck size={16}/></button>
      </form></>}
    <div className="auth-foot"><ShieldCheck size={14}/> {mode==='test'?'V1 testing access':'Administrator accounts only'}</div>
  </section></main>;
}
