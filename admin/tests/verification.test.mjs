import test from 'node:test';import assert from 'node:assert/strict';
import {verificationFailures} from '../src/lib/verification.js';
const a={id:'a',applicant_id:'u',kind:'provider',subject:'PLUMBING',jurisdiction:'UG',credential_number:'ABC',issuer:'Authority',expires_on:'2028-01-01'};
const base={application_id:'a',subject_id:'u',verified:true,source_reference:'registry:123',expires_at:'2028-01-01'};
const valid=[{...base,kind:'identity'},{...base,kind:'liveness'},{...base,kind:'credential',details:{service:'PLUMBING',jurisdiction:'UG',credential_number:'ABC',issuer:'Authority',expires_on:'2028-01-01',technician_id:'u'}}];
test('independent identity and matching credential required',()=>{assert.deepEqual(verificationFailures(a,valid),[]);assert.ok(verificationFailures(a,[]).length);assert.ok(verificationFailures(a,valid.map(e=>({...e,expires_at:'2020-01-01'}))).length);assert.ok(verificationFailures({...a,subject:'ELECTRICAL'},valid).length);});
test('broker cannot use an owner document',()=>{const owner={...a,kind:'property',subject:'TITLE',unit_number:'1',relationship:'broker'};const evidence=[...valid,{...base,kind:'ownership',details:{property_id:'TITLE',unit:'1',jurisdiction:'UG',owner_id:'u'}}];assert.ok(verificationFailures(owner,evidence).includes('Brokers and third parties cannot publish'));});
test('LLM approval fields have no effect',()=>{assert.ok(verificationFailures({...a,ai_summary:{approved:true}},[]).length);});
