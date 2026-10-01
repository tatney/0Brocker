export const BOOKING_STATUSES=['SEARCHING','PROVIDER_FOUND','ASSIGNED','EN_ROUTE','ARRIVED','INSPECTION','IN_PROGRESS','MATERIALS_REQUIRED','COMPLETED','PAYMENT','REVIEWED','CANCELLED'];
export const NEXT_ACTION={
  PROVIDER_FOUND:{label:'Assign',value:'ASSIGNED'},
  ASSIGNED:{label:'Mark en route',value:'EN_ROUTE'},
  EN_ROUTE:{label:'Mark arrived',value:'ARRIVED'},
  ARRIVED:{label:'Inspect',value:'INSPECTION'},
  INSPECTION:{label:'Start',value:'IN_PROGRESS'},
  IN_PROGRESS:{label:'Complete',value:'COMPLETED'},
  MATERIALS_REQUIRED:{label:'Resume',value:'IN_PROGRESS'},
  COMPLETED:{label:'Request payment',value:'PAYMENT'},
};
export function summarizeBookings(rows){
  const open=rows.filter(b=>!['COMPLETED','REVIEWED','CANCELLED','PAYMENT'].includes(b.status));
  return {open,completed:rows.filter(b=>['COMPLETED','REVIEWED'].includes(b.status)),unpaid:rows.filter(b=>b.status!=='CANCELLED'&&!b.is_paid)};
}
export function summarizeMoney(rows){
  const sum=credit=>rows.filter(t=>Boolean(t.is_credit)===credit).reduce((n,t)=>n+Math.abs(Number(t.amount_minor)||0),0);
  return {credits:sum(true),debits:sum(false),net:sum(true)-sum(false)};
}
