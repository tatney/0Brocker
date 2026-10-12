export function verificationFailures(application,evidence,now=Date.now()) {
  const scoped=evidence.filter(e=>e.application_id===application.id&&e.subject_id===application.applicant_id);
  const checks=scoped.filter(e=>e.verified===true&&e.source_reference?.trim()&&Date.parse(e.expires_at)>now);
  const failures=[];
  if(scoped.some(e=>e.verified===false||e.details?.revoked===true||e.details?.suspended===true||e.details?.disputed===true||e.details?.transferred===true))failures.push('Authoritative failure, suspension, dispute or transfer must be resolved');
  if(!checks.some(e=>e.kind==='identity'))failures.push('Independent identity check required');
  if(!checks.some(e=>e.kind==='liveness'))failures.push('Liveness check required');
  if(application.kind==='provider'){
    if(!(Date.parse(application.expires_on)>now))failures.push('Declared credential expiry must be current');
    if(!checks.some(e=>e.kind==='credential'&&e.details?.service===application.subject&&e.details?.jurisdiction===application.jurisdiction&&e.details?.credential_number===application.credential_number&&e.details?.issuer===application.issuer&&e.details?.expires_on===application.expires_on&&e.details?.technician_id===application.applicant_id))failures.push('Credential must match issuer, expiry, technician, trade and jurisdiction');
  }else if(application.kind==='property'){
    if(!['individual_owner','owning_company_representative'].includes(application.relationship))failures.push('Brokers and third parties cannot publish');
    const ownership=checks.find(e=>e.kind==='ownership'&&e.details?.property_id===application.subject&&e.details?.unit===application.unit_number&&e.details?.jurisdiction===application.jurisdiction&&e.details?.disputed!==true&&e.details?.transferred!==true);
    if(!ownership)failures.push('Exact property/unit ownership check required');
    else if(application.relationship==='individual_owner'&&ownership.details.owner_id!==application.applicant_id)failures.push('Applicant is not the registered owner');
    else if(application.relationship==='owning_company_representative'&&!checks.some(e=>e.kind==='company_authority'&&e.details?.company_id===ownership.details.owner_id&&e.details?.representative_id===application.applicant_id&&e.details?.company_ownership_verified===true))failures.push('Verified company ownership and representative authority required');
  }else failures.push('Unknown application type');
  if(application.backend_ready===false)failures.push('Backend rules or authoritative registry configuration are not satisfied');
  return failures;
}
