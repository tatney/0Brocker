import { useMemo } from 'react';
import { ArrowUpRight, Banknote, Home, ShieldCheck, Star, Users, Wrench, CircleCheck, Building2 } from 'lucide-react';
import { useResource } from '../data/useResource.js';
import { summarizeBookings, summarizeMoney } from '../lib/metrics.js';
import { formatInteger, formatMinor, formatRelative, formatUgx, titleCase } from '../lib/format.js';
import { Banner, Button, DataTable, PageHeader, Rating, StatCard, StatusPill, Spinner } from '../components/ui.jsx';

function Bars({items,label,color='var(--brand)'}){
 const maximum=Math.max(1,...items.map(i=>i.value));
 return <div className="bars" role="img" aria-label={`${label}: ${items.map(i=>`${i.label}: ${i.value}`).join(', ')}`}>
 {items.map(item=><div className="bar-row" key={item.label}><span>{item.label}</span><div className="bar-track"><div style={{width:`${item.value/maximum*100}%`,background:color}}/></div><strong>{item.value}</strong></div>)}
 </div>;
}
export function Overview({source,onNavigate}){
 const resources={properties:useResource('properties'),providers:useResource('providers'),bookings:useResource('bookings'),users:useResource('users'),transactions:useResource('transactions'),reviews:useResource('reviews')};
 const {properties,providers,bookings,users,transactions,reviews}=Object.fromEntries(Object.entries(resources).map(([k,v])=>[k,v.rows]));
 const errors=Object.values(resources).map(r=>r.error).filter(Boolean),loading=Object.values(resources).some(r=>r.loading);
 const stats=useMemo(()=>{
  const booking=summarizeBookings(bookings),wallet=summarizeMoney(transactions);
  const verified=properties.filter(p=>p.is_verified).length,verifiedProviders=providers.filter(p=>p.is_verified).length;
  const avgRating=reviews.length?reviews.reduce((s,r)=>s+r.overall,0)/reviews.length:0;
  return {...booking,...wallet,verified,verifiedProviders,avgRating};
 },[bookings,transactions,properties,providers,reviews]);
 const recent=[...bookings].sort((a,b)=>new Date(b.created_at)-new Date(a.created_at)).slice(0,6);
 const topProviders=[...providers].sort((a,b)=>b.jobs_completed-a.jobs_completed).slice(0,5);
 const workload=[{label:'Needs a provider',value:bookings.filter(b=>b.status==='SEARCHING').length},{label:'Active service',value:stats.open.filter(b=>b.status!=='SEARCHING').length},{label:'Completed',value:stats.completed.length},{label:'Awaiting payment',value:bookings.filter(b=>b.status==='PAYMENT').length},{label:'Cancelled',value:bookings.filter(b=>b.status==='CANCELLED').length}];
 const listingMix=['HOUSE','LAND','CAR'].map(asset=>({label:titleCase(asset),value:properties.filter(p=>p.asset_type===asset).length}));
 return <>
  <PageHeader title="Overview" subtitle="Your marketplace, at a glance." actions={<Button variant="primary" onClick={()=>onNavigate('bookings')}>Manage bookings <ArrowUpRight size={16}/></Button>}/>
  {source==='demo'&&<Banner tone="warn"><strong>Demo workspace.</strong> Explore sample records and try management actions. Changes stay in this browser; no live marketplace is affected.</Banner>}
  {errors.length>0&&<Banner tone="danger"><strong>Some data could not be loaded.</strong> {errors.join(' ')}</Banner>}
  {loading?<Spinner label="Loading marketplace…"/>:<>
  <section className="stat-grid">
   <StatCard label="Open bookings" value={formatInteger(stats.open.length)} hint={`${stats.completed.length} completed · ${bookings.length} total`} icon={Wrench} tone="accent" onClick={()=>onNavigate('bookings')}/>
   <StatCard label="Property listings" value={formatInteger(properties.length)} hint={`${stats.verified} verified listings`} icon={Home} onClick={()=>onNavigate('properties')}/>
   <StatCard label="Service providers" value={formatInteger(providers.length)} hint={`${stats.verifiedProviders} verified professionals`} icon={ShieldCheck} tone="info" onClick={()=>onNavigate('providers')}/>
   <StatCard label="Registered users" value={formatInteger(users.length)} hint={`${users.filter(u=>u.is_professional).length} professional accounts`} icon={Users} onClick={()=>onNavigate('people')}/>
   <StatCard label="Wallet credits" value={formatMinor(stats.credits)} hint="Recorded inflows · not platform revenue" icon={Banknote} tone="success" onClick={()=>onNavigate('money')}/>
   <StatCard label="Customer rating" value={reviews.length?stats.avgRating.toFixed(1):'—'} hint={`${reviews.length} submitted reviews`} icon={Star} tone="warn" onClick={()=>onNavigate('reviews')}/>
  </section>
  <section className="attention-strip" aria-label="Needs attention">
   <div className="attention-title"><span className="attention-icon"><CircleCheck size={20}/></span><div><strong>Keep things moving</strong><span>Your marketplace review queue</span></div></div>
   <button onClick={()=>onNavigate('properties')}><strong>{properties.length-stats.verified}</strong> unverified listings <ArrowUpRight size={15}/></button>
   <button onClick={()=>onNavigate('providers')}><strong>{providers.length-stats.verifiedProviders}</strong> unverified providers <ArrowUpRight size={15}/></button>
   <button onClick={()=>onNavigate('bookings')}><strong>{bookings.filter(b=>b.status==='SEARCHING').length}</strong> searching bookings <ArrowUpRight size={15}/></button>
  </section>
  <div className="chart-grid">
   <section className="panel"><div className="panel-head"><div><h2>Booking workload</h2><p>Where your service requests stand</p></div><span className="chart-count">{bookings.length} requests</span></div><Bars items={workload} label="Booking workload"/></section>
   <section className="panel"><div className="panel-head"><div><h2>Listing mix</h2><p>Assets available in the marketplace</p></div><Building2 size={18} className="muted"/></div><Bars items={listingMix} label="Listing mix" color="var(--accent)"/><div className="mix-footer"><span>{properties.filter(p=>p.listing_type==='RENT').length} for rent</span><span>{properties.filter(p=>p.listing_type==='BUY').length} for sale</span></div></section>
  </div>
  <div className="split">
   <section className="panel"><div className="panel-head"><h2>Latest bookings</h2><Button onClick={()=>onNavigate('bookings')}>View all <ArrowUpRight size={14}/></Button></div>
    <DataTable rows={recent} columns={[
     {key:'service',header:'Service',render:r=><span className="cell-strong">{titleCase(r.service_type)}</span>},
     {key:'provider',header:'Provider',render:r=>r.provider_name||<span className="muted">Unassigned</span>},
     {key:'status',header:'Status',render:r=><StatusPill status={r.status}/>},
     {key:'total',header:'Total',align:'right',render:r=>formatUgx(r.total_cost??0)},
     {key:'age',header:'Created',align:'right',render:r=>formatRelative(r.created_at)},
    ]} empty="No bookings yet."/>
   </section>
   <section className="panel"><div className="panel-head"><h2>Busiest providers</h2><Button onClick={()=>onNavigate('providers')}>View all <ArrowUpRight size={14}/></Button></div>
    <DataTable rows={topProviders} columns={[
     {key:'name',header:'Provider',render:r=><span className="cell-strong">{r.name}</span>},
     {key:'jobs',header:'Jobs',align:'right',render:r=>formatInteger(r.jobs_completed)},
     {key:'rating',header:'Rating',align:'right',render:r=><Rating value={r.rating}/>},
    ]} empty="No providers yet."/>
   </section>
  </div>
  <p className="overview-foot">All figures cover loaded records. Currency: UGX · Dates: East Africa Time.</p>
  </>}
 </>;
}
