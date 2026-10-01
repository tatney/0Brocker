import test from 'node:test';import assert from 'node:assert/strict';import {createHash} from 'node:crypto';
import {validPassword,tokenFor,validToken} from '../server/test-auth.mjs';
const secret='test-session-secret-which-is-long-enough';const now=100000000;
test('password hash rejects incorrect or malformed input',()=>{const hash=createHash('sha256').update('correct').digest('hex');assert.equal(validPassword('correct',hash),true);assert.equal(validPassword('wrong',hash),false);assert.equal(validPassword({},hash),false);assert.equal(validPassword('correct',undefined),false);});
test('test session expires, rejects tampering and cannot use a different signing key',()=>{const token=tokenFor(secret,now);assert.equal(validToken(token,secret,now+1),true);assert.equal(validToken(token,secret,now+8*3600*1000),false);assert.equal(validToken(token+'x',secret,now),false);assert.equal(validToken(token,secret+'x',now),false);assert.equal(validToken(token,undefined,now),false);});
