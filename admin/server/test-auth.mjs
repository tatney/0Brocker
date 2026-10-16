import {createHash, createHmac, timingSafeEqual} from 'node:crypto';
export const COOKIE='__Host-0brocker-test';
export function validPassword(password, expectedHash) {
  if(typeof password!=='string'||password.length>200||! /^[a-f0-9]{64}$/.test(expectedHash??''))return false;
  const actual=createHash('sha256').update(password).digest();
  return timingSafeEqual(actual,Buffer.from(expectedHash,'hex'));
}
export function tokenFor(secret,now=Date.now()) {
  const payload=Buffer.from(JSON.stringify({scope:'test-workspace',expires:now+8*3600*1000})).toString('base64url');
  return payload+'.'+createHmac('sha256',secret).update(payload).digest('base64url');
}
export function validToken(token,secret,now=Date.now()) {
  if(!secret||!token||token.length>512)return false;
  try {
    const [payload,signature,extra]=token.split('.'); if(extra||!signature)return false;
    const expected=createHmac('sha256',secret).update(payload).digest(); const supplied=Buffer.from(signature,'base64url');
    if(expected.length!==supplied.length||!timingSafeEqual(expected,supplied))return false;
    const claims=JSON.parse(Buffer.from(payload,'base64url').toString());
    return claims.scope==='test-workspace'&&Number.isFinite(claims.expires)&&claims.expires>now&&claims.expires<=now+8*3600*1000;
  } catch {return false;}
}
