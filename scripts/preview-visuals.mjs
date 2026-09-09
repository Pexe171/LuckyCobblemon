// Orthographic software render of the actual Minecraft JSON mesh and authored materials.
// This is an asset preview, not a screenshot from Minecraft.
import fs from 'node:fs';
import path from 'node:path';
import { root, png, elements, materials } from './generate-visuals.mjs';

const scale = 2, W = 1280 * scale, H = 900 * scale;
const image = new Uint8Array(W * H * 4);
const depth = new Float32Array(W * H).fill(-Infinity);
for (let y = 0; y < H; y++) for (let x = 0; x < W; x++) {
  const glow = Math.max(0, 1 - Math.hypot((x - W * .32) / W, (y - H * .42) / H) * 1.6);
  const c = [16 + glow * 16, 23 + glow * 21, 31 + glow * 26];
  image.set([...c.map(Math.round), 255], (y * W + x) * 4);
}
function rect(x, y, w, h, rgb) {
  for (let py = Math.round(y * scale); py < Math.round((y + h) * scale); py++)
    for (let px = Math.round(x * scale); px < Math.round((x + w) * scale); px++)
      image.set([...rgb, 255], (py * W + px) * 4);
}
const font = {
 A:['01110','10001','10001','11111','10001','10001','10001'], B:['11110','10001','10001','11110','10001','10001','11110'],
 C:['01111','10000','10000','10000','10000','10000','01111'], D:['11110','10001','10001','10001','10001','10001','11110'],
 E:['11111','10000','10000','11110','10000','10000','11111'], F:['11111','10000','10000','11110','10000','10000','10000'],
 G:['01111','10000','10000','10111','10001','10001','01110'], H:['10001','10001','10001','11111','10001','10001','10001'],
 I:['11111','00100','00100','00100','00100','00100','11111'], J:['00111','00010','00010','00010','10010','10010','01100'],
 K:['10001','10010','10100','11000','10100','10010','10001'], L:['10000','10000','10000','10000','10000','10000','11111'],
 M:['10001','11011','10101','10101','10001','10001','10001'], N:['10001','11001','11001','10101','10011','10011','10001'],
 O:['01110','10001','10001','10001','10001','10001','01110'], P:['11110','10001','10001','11110','10000','10000','10000'],
 Q:['01110','10001','10001','10001','10101','10010','01101'], R:['11110','10001','10001','11110','10100','10010','10001'],
 S:['01111','10000','10000','01110','00001','00001','11110'], T:['11111','00100','00100','00100','00100','00100','00100'],
 U:['10001','10001','10001','10001','10001','10001','01110'], V:['10001','10001','10001','10001','10001','01010','00100'],
 W:['10001','10001','10001','10101','10101','10101','01010'], X:['10001','10001','01010','00100','01010','10001','10001'],
 Y:['10001','10001','01010','00100','00100','00100','00100'], Z:['11111','00001','00010','00100','01000','10000','11111'],
 '0':['01110','10001','10011','10101','11001','10001','01110'], '4':['00010','00110','01010','10010','11111','00010','00010'],
 '.':['00000','00000','00000','00000','00000','00110','00110'], '/':['00001','00001','00010','00100','01000','10000','10000'],
 ' ':['00000','00000','00000','00000','00000','00000','00000']
};
function label(text, x, y, size, color) {
  for (const c of text) {
    const glyph = font[c] || font[' '];
    glyph.forEach((row, ry) => [...row].forEach((v, rx) => { if (v === '1') rect(x + rx * size, y + ry * size, size, size, color); }));
    x += size * 6;
  }
}
const gold = [220, 189, 128], muted = [124, 146, 160];
label('LUCKY COBBLEMON', 64, 51, 3, [239, 235, 218]);
label('0.4 / CHERRY COLLECTION', 65, 90, 1.4, gold);
rect(804, 147, 1, 646, [51, 64, 73]);
label('FRONTAL', 887, 153, 1.6, muted);
label('INVENTARIO', 887, 526, 1.6, muted);
label('MODELO / PREVIA', 64, 827, 1.5, muted);
label('PORCELANA  OURO  LUZ', 881, 827, 1.3, gold);

const dot = (a, b) => a.reduce((s, v, i) => s + v * b[i], 0);
const cross2 = (a, b, p) => (b[0] - a[0]) * (p[1] - a[1]) - (b[1] - a[1]) * (p[0] - a[0]);
const normals = { north: [0,0,-1], south: [0,0,1], east: [1,0,0], west: [-1,0,0], up: [0,1,0], down: [0,-1,0] };
function vertices(element, face) {
  const [x,y,z] = element.from, [X,Y,Z] = element.to;
  return { north:[[X,Y,z],[x,Y,z],[x,y,z],[X,y,z]], south:[[x,Y,Z],[X,Y,Z],[X,y,Z],[x,y,Z]],
    east:[[X,Y,Z],[X,Y,z],[X,y,z],[X,y,Z]], west:[[x,Y,z],[x,Y,Z],[x,y,Z],[x,y,z]],
    up:[[x,Y,z],[X,Y,z],[X,Y,Z],[x,Y,Z]], down:[[x,y,Z],[X,y,Z],[X,y,z],[x,y,z]] }[face];
}
function shadow(cx, cy, rx, ry) {
  for (let y = Math.max(0, Math.floor((cy - ry) * scale)); y < Math.min(H, (cy + ry) * scale); y++)
    for (let x = Math.max(0, Math.floor((cx - rx) * scale)); x < Math.min(W, (cx + rx) * scale); x++) {
      const d = ((x/scale-cx)/rx)**2 + ((y/scale-cy)/ry)**2;
      const a = Math.max(0, 1 - d) ** 2 * .5;
      for (let c = 0; c < 3; c++) image[(y * W + x) * 4 + c] *= 1 - a;
    }
}
function render(cx, cy, zoom, yaw, pitch) {
  const right = [Math.cos(yaw), 0, Math.sin(yaw)];
  const up = [-Math.sin(yaw)*Math.sin(pitch), Math.cos(pitch), Math.cos(yaw)*Math.sin(pitch)];
  const view = [Math.sin(yaw)*Math.cos(pitch), Math.sin(pitch), -Math.cos(yaw)*Math.cos(pitch)];
  const project = p => {
    const v = [p[0]-8, p[1]-6.8, p[2]-8];
    return [(cx + dot(v,right)*zoom)*scale, (cy-dot(v,up)*zoom)*scale, dot(v,view)];
  };
  for (const element of elements) for (const [face, attrs] of Object.entries(element.faces)) {
    const normal = normals[face];
    if (dot(normal,view) <= .001) continue;
    const v = vertices(element, face).map(project);
    const tex = materials[attrs.texture.slice(1)];
    const uv = [[0,0],[1,0],[1,1],[0,1]];
    const light = element.shade === false ? 1 : .68 + .32 * Math.max(0, dot(normal, [-.3,.83,-.47]));
    for (const idx of [[0,1,2],[0,2,3]]) {
      const [a,b,c] = idx.map(i=>v[i]);
      const area = cross2(a,b,c);
      if (Math.abs(area)<.001) continue;
      const x0 = Math.max(0,Math.floor(Math.min(a[0],b[0],c[0]))), x1=Math.min(W-1,Math.ceil(Math.max(a[0],b[0],c[0])));
      const y0 = Math.max(0,Math.floor(Math.min(a[1],b[1],c[1]))), y1=Math.min(H-1,Math.ceil(Math.max(a[1],b[1],c[1])));
      for (let y=y0;y<=y1;y++) for(let x=x0;x<=x1;x++) {
        const p=[x+.5,y+.5], wa=cross2(b,c,p)/area, wb=cross2(c,a,p)/area, wc=1-wa-wb;
        if (Math.min(wa,wb,wc)<-1e-6) continue;
        const d=wa*a[2]+wb*b[2]+wc*c[2], offset=y*W+x;
        if (d<depth[offset]) continue;
        depth[offset]=d;
        const u=Math.min(31,Math.max(0,Math.floor((wa*uv[idx[0]][0]+wb*uv[idx[1]][0]+wc*uv[idx[2]][0])*32)));
        const t=Math.min(31,Math.max(0,Math.floor((wa*uv[idx[0]][1]+wb*uv[idx[1]][1]+wc*uv[idx[2]][1])*32)));
        for(let ch=0;ch<3;ch++) image[offset*4+ch]=Math.round(tex[(t*32+u)*4+ch]*light);
      }
    }
  }
}
shadow(425,735,287,53);
render(426,452,36,Math.PI/4,.48);
shadow(1020,441,101,22);
render(1020,322,15,0,.07);
for(let i=0;i<3;i++) {
  rect(882+i*86,583,76,76,[47,61,71]); rect(885+i*86,586,70,70,[28,41,51]);
}
render(1006,621,3.6,Math.PI/4,.48);
label('COMPACTA NA MAO', 887,701,1.5,muted);
render(1128,755,3.1,Math.PI/4,.35);

const result=new Uint8Array(1280*900*4);
for(let y=0;y<900;y++) for(let x=0;x<1280;x++) for(let c=0;c<4;c++) {
  let total=0;
  for(let dy=0;dy<scale;dy++) for(let dx=0;dx<scale;dx++) total+=image[((y*scale+dy)*W+x*scale+dx)*4+c];
  result[(y*1280+x)*4+c]=Math.round(total/(scale*scale));
}
const out=path.join(root,'build/visual-preview'); fs.mkdirSync(out,{recursive:true});
png(path.join(out,'lucky-cobblemon-0.4-preview.png'),1280,900,result);
console.log('Asset preview: '+path.join(out,'lucky-cobblemon-0.4-preview.png'));
