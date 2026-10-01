import { test } from 'node:test';
import assert from 'node:assert/strict';
import { BOOKING_STATUSES, NEXT_ACTION, summarizeBookings, summarizeMoney } from '../src/lib/metrics.js';
import { toCsv } from '../src/lib/format.js';
test('all workflow actions use mobile-app statuses and terminal bookings cannot be reopened',()=>{
 for(const [from,action] of Object.entries(NEXT_ACTION)){assert.ok(BOOKING_STATUSES.includes(from));assert.ok(BOOKING_STATUSES.includes(action.value));}
 assert.equal(NEXT_ACTION.CANCELLED,undefined);assert.equal(NEXT_ACTION.REVIEWED,undefined);assert.equal(NEXT_ACTION.SEARCHING,undefined);
});
test('active booking totals include assignment, travel and materials stages',()=>{
 const rows=BOOKING_STATUSES.map(status=>({status,is_paid:false}));const summary=summarizeBookings(rows);
 assert.equal(summary.open.length,8);assert.equal(summary.completed.length,2);assert.equal(summary.unpaid.length,11);
});
test('wallet debit sign does not invert the net-flow calculation',()=>{
 assert.deepEqual(summarizeMoney([{is_credit:true,amount_minor:150000},{is_credit:false,amount_minor:-25000},{is_credit:false,amount_minor:10000}]),{credits:150000,debits:35000,net:115000});
});
test('CSV prevents spreadsheet formulas in user-provided cells',()=>{
 const columns=[{label:'Name',value:r=>r.name}];
 const csv=toCsv([{name:'=HYPERLINK("https://example.com")'},{name:'+SUM(1,2)'},{name:'@SUM(A1)'}],columns);
 assert.ok(csv.includes("'=HYPERLINK"));assert.ok(csv.includes("'+SUM"));assert.ok(csv.includes("'@SUM"));
});
