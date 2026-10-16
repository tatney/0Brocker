import {supabase,isSupabaseConfigured} from '../lib/supabase.js';
import {isTestWorkspace} from '../lib/workspace.js';
import {verificationFailures} from '../lib/verification.js';
const key='0brocker-verification-demo-v1';
const local=()=>!isSupabaseConfigured||isTestWorkspace();
function samples(){
  const apps=[{id:'demo-provider',applicant_id:'demo-technician',kind:'provider',subject:'PLUMBING',jurisdiction:'DEMO',credential_number:'DEMO-1001',issuer:'DEMO issuer',expires_on:'2028-01-01',status:'pending',created_at:'2026-10-12T10:00:00Z'},
    {id:'demo-owner',applicant_id:'demo-owner',kind:'property',subject:'DEMO-TITLE-1204',unit_number:'1204',jurisdiction:'DEMO',relationship:'individual_owner',status:'pending',created_at:'2026-10-12T10:00:00Z'},
    {id:'demo-broker',applicant_id:'demo-broker',kind:'property',subject:'DEMO-TITLE-1204',unit_number:'1204',jurisdiction:'DEMO',relationship:'broker',status:'pending',created_at:'2026-10-12T10:00:00Z'}];
  const evidence=apps.flatMap(a=>['identity','liveness',a.kind==='provider'?'credential':'ownership'].map(kind=>({application_id:a.id,subject_id:a.applicant_id,kind,verified:true,source_reference:'SIMULATED fixture — no real registry check',expires_at:'2028-01-01T00:00:00Z',details:a.kind==='provider'?{service:a.subject,jurisdiction:a.jurisdiction,credential_number:a.credential_number,issuer:a.issuer,expires_on:a.expires_on,technician_id:a.applicant_id}:{property_id:a.subject,unit:a.unit_number,jurisdiction:a.jurisdiction,owner_id:'demo-owner'}})));
  return {apps,evidence,audit:[]};
}
function demo(){try{return JSON.parse(localStorage.getItem(key))??samples();}catch{return samples();}}
export async function fetchVerification(){
  if(local())return {...demo(),source:'demo'};
  const [{data:apps,error},{data:evidence,error:evidenceError},{data:audit,error:auditError}]=await Promise.all([supabase.from('verification_applications').select('*').order('created_at',{ascending:false}).limit(100),supabase.from('verification_evidence').select('*').limit(1000),supabase.from('verification_audit').select('*').order('created_at',{ascending:false}).limit(1000)]);
  if(error||evidenceError||auditError)throw new Error(error?.message||evidenceError?.message||auditError.message);
  const checked=await Promise.all(apps.map(async a=>{const {data,error}=await supabase.rpc('verification_ready',{p_id:a.id});if(error)throw new Error(error.message);return {...a,backend_ready:data===true};}));
  return {apps:checked,evidence,audit,source:'live'};
}
export async function reviewVerification(id,decision,reason){
  if(!reason?.trim())throw new Error('A reviewer reason is required');
  if(local()){
    const state=demo(),a=state.apps.find(a=>a.id===id);if(!a)throw new Error('Application not found');
    if(!['approve','reject','request_more','suspend'].includes(decision))throw new Error('Invalid decision');
    if(decision==='approve'&&(['rejected','suspended'].includes(a.status)||verificationFailures(a,state.evidence).length))throw new Error('Approval blocked by failed evidence checks');
    a.status={approve:'approved',reject:'rejected',request_more:'needs_information',suspend:'suspended'}[decision];a.review_reason=reason;
    state.audit.push({application_id:id,action:decision,detail:{reason},created_at:new Date().toISOString(),simulated:true});localStorage.setItem(key,JSON.stringify(state));return a;
  }
  const {data,error}=await supabase.rpc('review_verification',{p_id:id,p_decision:decision,p_reason:reason});if(error)throw new Error(error.message);return data;
}
export async function extractVerification(id,text,consent){
  if(text.length<10||text.length>20000)throw new Error('Enter 10–20,000 characters');
  if(local())return {mode:'demo',summary:'Demo document assistant: uploaded text is not proof of authenticity. Independent checks and reviewer approval remain required.',advisoryOnly:true};
  if(!consent)throw new Error('Consent is required for external AI processing');
  const {data,error}=await supabase.functions.invoke('verification-extract',{body:{applicationId:id,text,consent}});if(error)throw new Error(error.message);return data;
}
