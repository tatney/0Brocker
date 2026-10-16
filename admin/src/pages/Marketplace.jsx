import {useEffect,useState} from 'react';
import {supabase} from '../lib/supabase.js';
import {Banner,Button,PageHeader} from '../components/ui.jsx';

export function Marketplace({source}) {
 const [data,setData]=useState(null),[error,setError]=useState(''),[reason,setReason]=useState(''),[selected,setSelected]=useState(null),[busy,setBusy]=useState(false),[caseReady,setCaseReady]=useState(null);
 const load=async()=>{
  if(source!=='live'){setData({policy:{free_launch:true,provider_fee_ugx:2500},queue:[],events:[],enquiries:[],interest:[],ledger:[]});return;}
  const requests=await Promise.all(['marketplace_policy','marketplace_bookings','booking_events','rental_management_enquiries','marketplace_plan_interest','provider_fee_ledger'].map(table=>{let query=supabase.from(table).select('*');if(table==='marketplace_bookings') query=query.in('status',['DISPUTED','REVIEW_REQUIRED']);return query.limit(200);}));
  const failure=requests.find(r=>r.error);if(failure){setError(`${failure.error.message}. Marketplace migration may not be deployed.`);return;}
  setData({policy:requests[0].data[0],queue:requests[1].data.filter(b=>['DISPUTED','REVIEW_REQUIRED'].includes(b.status)),events:requests[2].data,enquiries:requests[3].data,interest:requests[4].data,ledger:requests[5].data});
 };
 useEffect(()=>{load();},[source]);
 useEffect(()=>{if(!selected||source!=='live')return;setCaseReady(null);let active=true;Promise.all(['booking_events','provider_fee_ledger'].map(table=>supabase.from(table).select('*').eq('booking_id',selected).order('id').limit(1000))).then(results=>{if(!active)return;const failure=results.find(r=>r.error);if(failure){setError(failure.error.message);return;}setData(previous=>({...previous,events:results[0].data,ledger:results[1].data}));setCaseReady(selected);});return()=>{active=false;};},[selected,source]);
 const decide=async(action)=>{if(!selected||caseReady!==selected||reason.trim().length<5||busy)return;setBusy(true);setError('');const {error:e}=await supabase.rpc('marketplace_action',{p_booking:selected,p_action:action,p_reason:reason});if(e)setError(e.message);else{setReason('');setSelected(null);await load();}setBusy(false);};
 return <><PageHeader title="Marketplace operations" subtitle="Evidence, attended-request fees and company rental-management enquiries"/>
 <Banner tone="info">Basic owner chat stays free. Subscriptions, search passes, AR navigation and payment collection are not activated. Workspace selection never grants qualification.</Banner>
 {source!=='live'&&<Banner tone="warn">Demo policy view. No real bookings, charges, enquiries or review decisions are created here.</Banner>}
 {error&&<Banner tone="danger">{error}</Banner>}
 {data&&<>
 <section className="panel"><h2>{data.policy?.free_launch?'Free launch - platform charges waived':'Attended-request fee policy'}</h2><p>Future fee illustration: UGX {data.policy?.provider_fee_ugx}. Reserve on technician acceptance; settle only after customer-confirmed arrival. Pre-arrival cancellation releases the reservation. Post-arrival cancellation preserves evidence and the fee for reasoned review.</p><p>Policy and balance changes require trusted server operations. A browser cannot activate paid plans or top up a provider.</p></section>
 <section className="panel"><h2>Dispute and cancellation review</h2>{data.queue.length===0?<p>No pending cases returned.</p>:data.queue.map(b=><div key={b.id}><Button onClick={()=>setSelected(b.id)}>Review booking #{b.id} - {b.status}</Button></div>)}
 {selected&&<><h3>Booking #{selected}</h3><p>Review the recorded customer and technician evidence before deciding. This preview shows up to 1,000 events for the selected booking; obtain any additional evidence before resolving a larger case.</p><ul>{data.events.filter(e=>e.booking_id===selected).map(e=><li key={e.id}>{e.created_at}: {e.action} - {e.reason||'No additional reason'} (actor {e.actor_id})</li>)}</ul><ul>{data.ledger.filter(e=>e.booking_id===selected).map(e=><li key={e.id}>{e.action}: UGX {e.amount_ugx}</li>)}</ul><label>Decision reason <textarea value={reason} onChange={e=>setReason(e.target.value)} maxLength={1000}/></label><Button disabled={busy||caseReady!==selected||reason.trim().length<5||source!=='live'} onClick={()=>decide('release_fee')}>Release / refund fee</Button><Button disabled={busy||reason.trim().length<5||source!=='live'} onClick={()=>decide('uphold_fee')}>Uphold fee</Button><p>A reviewer does not impersonate the customer or confirm service completion.</p></>}
 </section>
 <section className="panel"><h2>Company rental-management enquiries</h2><p>Separately contracted company service: rent collection, repairs, inspections and premises operations. Confirm ownership and representative authority before offering a management contract. These are enquiries, not active management agreements.</p>{data.enquiries.map(e=><p key={e.id}>{e.title_identifier} / {e.unit_number}: {e.scope} - {e.status}</p>)}</section>
 <section className="panel"><h2>Future plan interest</h2><p>{data.interest.length} recorded expressions of interest. No payment or entitlement. Provider Pro and Owner Portfolio offer business tools; Search Pass may add search tools and owner-consented calling. Prices and launch dates remain undecided.</p></section>
 </>}
 </>;
}
