import { supabase, isSupabaseConfigured } from '../lib/supabase.js';
import { isTestWorkspace } from '../lib/workspace.js';
import { demoData } from './demo.js';
import { BOOKING_STATUSES, NEXT_ACTION } from '../lib/metrics.js';
const RESOURCES={
  properties:{table:'properties',order:'rating',ascending:false},
  providers:{table:'service_providers',order:'rating',ascending:false},
  bookings:{table:'marketplace_bookings',order:'created_at',ascending:false},
  users:{table:'users',order:'created_at',ascending:false,fields:'id,full_name,phone,email,avatar_emoji,is_professional,created_at'},
  transactions:{table:'wallet_transactions',order:'created_at',ascending:false},
  reviews:{table:'reviews',order:'created_at',ascending:false},
};
export const resourceNames=Object.keys(RESOURCES);
const DEMO_KEY='0brocker-demo-patches-v1';
function demoPatches(){try{return JSON.parse(localStorage.getItem(DEMO_KEY)||'{}')??{};}catch{return {};}}
function demoRows(name){const patches=demoPatches()[name]??{};return (demoData[name]??[]).map(row=>{
  const patch=patches[row.id]??{};
  return {...row,...(typeof patch.is_verified==='boolean'?{is_verified:patch.is_verified}:{}),...(name==='bookings'&&BOOKING_STATUSES.includes(patch.status)?{status:patch.status}:{}),...(typeof patch.started_at==='string'?{started_at:patch.started_at}:{}),...(typeof patch.completed_at==='string'?{completed_at:patch.completed_at}:{})};
});}
async function requireAdmin(){const {data,error}=await supabase.auth.getUser();if(error||data.user?.app_metadata?.role!=='admin')throw new Error('Administrator sign-in is required.');}
export async function fetchResource(name,{limit=500}={}){
  const config=RESOURCES[name];if(!config)return {data:[],source:'live',error:'Unknown resource.'};
  if((!isSupabaseConfigured || isTestWorkspace()))return {data:demoRows(name),source:'demo',error:null};
  try{
    await requireAdmin();
    const {data,error,count}=await supabase.from(config.table).select(config.fields??'*',{count:'exact'}).order(config.order,{ascending:config.ascending}).limit(limit);
    if(error)throw new Error(error.message);
    return {data:data??[],source:'live',error:count>limit?`Showing ${limit} of ${count} records. Summaries and exports cover loaded records only.`:null};
  }catch(error){return {data:[],source:'live',error:`Unable to load ${name}: ${error.message}`};}
}
async function patchRecord(name,id,patch,expectedStatus){
  try{
    if((!isSupabaseConfigured || isTestWorkspace())){
      const row=demoRows(name).find(r=>r.id===id);if(!row)throw new Error('Record not found.');
      if(patch.status==='PAYMENT'&&row.is_paid)throw new Error('This booking has already been paid.');
      if(expectedStatus&&row.status!==expectedStatus)throw new Error('This booking changed. Refresh and try again.');
      const patches=demoPatches();patches[name]={...patches[name],[id]:{...patches[name]?.[id],...patch}};
      localStorage.setItem(DEMO_KEY,JSON.stringify(patches));return {data:{...row,...patch},error:null};
    }
    await requireAdmin();
    let query=supabase.from(RESOURCES[name].table).update(patch).eq('id',id);
    if(expectedStatus)query=query.eq('status',expectedStatus);
    if(patch.status==='PAYMENT')query=query.eq('is_paid',false);
    const {data,error}=await query.select().single();
    if(error)throw new Error('Update failed or the record changed. Refresh and try again.');
    return {data,error:null};
  }catch(error){return {data:null,error:error.message};}
}
export async function updateBookingStatus(id,status,previousStatus){
  if(!BOOKING_STATUSES.includes(status)||NEXT_ACTION[previousStatus]?.value!==status)return {data:null,error:'Invalid booking transition.'};
  const patch={status};
  if(status==='COMPLETED')patch.completed_at=new Date().toISOString();
  if(status==='IN_PROGRESS')patch.started_at=new Date().toISOString();
  return patchRecord('bookings',id,patch,previousStatus);
}
export async function verifyRecord(name,id){
  if(!['properties','providers'].includes(name))return {data:null,error:'This record cannot be verified.'};
  return patchRecord(name,id,{is_verified:true});
}
export function subscribeToBookings(onChange){
  if((!isSupabaseConfigured || isTestWorkspace()))return ()=>{};
  const channel=supabase.channel('admin-bookings').on('postgres_changes',{event:'*',schema:'public',table:'marketplace_bookings'},onChange).subscribe();
  return ()=>supabase.removeChannel(channel);
}
