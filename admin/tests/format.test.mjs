import { test } from 'node:test';
import assert from 'node:assert/strict';
import {
  formatInteger,
  formatMinor,
  formatOneDecimal,
  formatRelative,
  formatSignedMinor,
  formatUgx,
  titleCase,
  toCsv,
} from '../src/lib/format.js';

test('formatInteger groups thousands', () => {
  assert.equal(formatInteger(1234567), '1,234,567');
  assert.equal(formatInteger(0), '0');
  assert.equal(formatInteger(null), '—');
});

test('formatUgx prefixes the currency code', () => {
  assert.equal(formatUgx(750000), 'UGX 750,000');
  assert.equal(formatUgx(undefined), '—');
});

test('formatMinor converts minor units to whole shillings', () => {
  assert.equal(formatMinor(135000), 'UGX 1,350');
  assert.equal(formatMinor(100), 'UGX 1');
});

test('formatSignedMinor marks direction', () => {
  assert.equal(formatSignedMinor(135000), '+UGX 1,350');
  assert.equal(formatSignedMinor(-135000), '-UGX 1,350');
});

test('formatOneDecimal rounds to one place', () => {
  assert.equal(formatOneDecimal(4.85), '4.9');
  assert.equal(formatOneDecimal(5), '5.0');
});

test('formatRelative describes past timestamps', () => {
  const threeHoursAgo = new Date(Date.now() - 3 * 3600_000).toISOString();
  assert.equal(formatRelative(threeHoursAgo), '3 hours ago');
  assert.equal(formatRelative(null), '—');
});

test('titleCase unwraps SCREAMING_SNAKE', () => {
  assert.equal(titleCase('IN_PROGRESS'), 'In Progress');
  assert.equal(titleCase(''), '—');
});

test('toCsv quotes cells containing commas and quotes', () => {
  const columns = [
    { label: 'Title', value: (r) => r.title },
    { label: 'Amount', value: (r) => r.amount },
  ];
  const csv = toCsv(
    [
      { title: 'Deep clean, 3 bed', amount: 135000 },
      { title: 'He said "great"', amount: 0 },
    ],
    columns,
  );
  assert.equal(
    csv,
    'Title,Amount\n"Deep clean, 3 bed",135000\n"He said ""great""",0',
  );
});

test('toCsv renders null and undefined as empty cells', () => {
  const csv = toCsv([{ a: null, b: undefined }], [
    { label: 'A', value: (r) => r.a },
    { label: 'B', value: (r) => r.b },
  ]);
  assert.equal(csv, 'A,B\n,');
});
