import { useState } from 'react';
import { verifyRecord } from '../data/api.js';
export function VerifyAction({ name, row, onSaved }) {
  const [busy,setBusy]=useState(false),[error,setError]=useState('');
  if(row.is_verified)return <span className="tick">Verified</span>;
  async function verify(){setBusy(true);setError('');const result=await verifyRecord(name,row.id);if(result.error)setError(result.error);else onSaved(result.data);setBusy(false);}
  return <div><button className="btn btn-ghost" disabled={busy} onClick={verify}>{busy?'Saving…':'Verify'}</button>{error&&<small role="alert" className="action-error">{error}</small>}</div>;
}
