import {COOKIE,validPassword,tokenFor,validToken} from '../server/test-auth.mjs';
export default function handler(req,res) {
  res.setHeader('Cache-Control','no-store');
  const secret=process.env.TEST_ADMIN_SESSION_SECRET;
  const cookies=Object.fromEntries((req.headers.cookie??'').split(';').map(v=>v.trim().split('=')));
  if(req.method==='GET')return res.status(200).json({authenticated:validToken(cookies[COOKIE],secret),scope:'test-workspace'});
  const origin=req.headers.origin;
  if(!origin||new URL(origin).host!==req.headers.host)return res.status(403).json({error:'Invalid origin'});
  if(req.method==='DELETE') {
    res.setHeader('Set-Cookie',COOKIE+'=; HttpOnly; Secure; SameSite=Strict; Path=/; Max-Age=0');
    return res.status(200).json({authenticated:false});
  }
  if(req.method!=='POST') {res.setHeader('Allow','GET, POST, DELETE');return res.status(405).end();}
  if(!secret||secret.length<32)return res.status(503).json({error:'Test workspace is unavailable'});
  const body=req.body??{};
  const ok=validPassword(body.password,process.env.TEST_ADMIN_PASSWORD_SHA256)&&typeof body.email==='string'&&body.email.trim().toLowerCase()===process.env.TEST_ADMIN_EMAIL;
  if(!ok)return res.status(401).json({error:'Invalid test credentials'});
  res.setHeader('Set-Cookie',COOKIE+'='+tokenFor(secret)+'; HttpOnly; Secure; SameSite=Strict; Path=/; Max-Age=28800');
  return res.status(200).json({authenticated:true,scope:'test-workspace'});
}
