import fs from 'node:fs';
import path from 'node:path';
import { spawn } from 'node:child_process';
const cwd=path.resolve(import.meta.dirname,'..');
const cli=path.join(cwd,'node_modules/vercel/dist/index.js');
const env=Object.fromEntries(fs.readFileSync(path.join(cwd,'.env.local'),'utf8').split(/\r?\n/).filter(l=>/^VITE_SUPABASE_\w+=/.test(l)).map(l=>{const i=l.indexOf('=');return [l.slice(0,i),l.slice(i+1).trim().replace(/^['"]|['"]$/g,'')];}));
const publicKey=env.VITE_SUPABASE_ANON_KEY;
if(!env.VITE_SUPABASE_URL||!publicKey)throw new Error('Public Supabase configuration is missing.');
if(new URL(env.VITE_SUPABASE_URL).protocol!=='https:')throw new Error('Supabase URL must use HTTPS.');
if(publicKey.startsWith('eyJ')){const claims=JSON.parse(Buffer.from(publicKey.split('.')[1],'base64url'));if(claims.role!=='anon')throw new Error('Only a public anon key may be deployed.');}
else if(!publicKey.startsWith('sb_publishable_'))throw new Error('Only a public anon or publishable key may be deployed.');
const scrub=s=>Object.values(env).reduce((text,value)=>value?text.replaceAll(value,'[public configuration]'):text,String(s));
async function run(args,input){return new Promise((resolve,reject)=>{
 const child=spawn(process.execPath,[cli,...args],{cwd,windowsHide:true,stdio:['pipe','pipe','pipe']});let output='';
 child.stdout.on('data',b=>{output+=b.toString();process.stdout.write(scrub(b));});
 child.stderr.on('data',b=>{output+=b.toString();process.stderr.write(scrub(b));});
 child.on('error',reject);child.on('close',code=>code===0?resolve(output):reject(new Error(`Vercel ${args[0]} failed (${code}).`)));
 child.stdin.end(input??'');
 });}
await run(['link','--yes','--project','0brocker-admin']);
for(const [name,value] of Object.entries(env))if(['VITE_SUPABASE_URL','VITE_SUPABASE_ANON_KEY'].includes(name)){
 for(const target of ['production'])await run(['env','add',name,target,'--value',value,'--yes','--force']);
}
const output=await run(['deploy','--prod','--yes']);
const urls=[...output.matchAll(/https:\/\/[a-zA-Z0-9.-]+\.vercel\.app/g)].map(m=>m[0]);
if(urls.length)fs.writeFileSync(path.join(cwd,'deployment.json'),JSON.stringify({project:'0brocker-admin',url:output.match(/Aliased: (https:\/\/[a-zA-Z0-9.-]+\.vercel\.app)/)?.[1]??urls.at(-1),deployedAt:new Date().toISOString()},null,2));
