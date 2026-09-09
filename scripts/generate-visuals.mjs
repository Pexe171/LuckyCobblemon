// Code-native Minecraft mesh + pixel material authoring. No external dependencies.
// Run: node scripts/generate-visuals.mjs
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { deflateSync } from 'node:zlib';

export const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const assets = path.join(root, 'src/main/resources/assets/luckycobblemon');
const textures = path.join(assets, 'textures/block');
fs.mkdirSync(textures, { recursive: true });

function crc32(bytes) {
  let crc = -1;
  for (const b of bytes) {
    crc ^= b;
    for (let k = 0; k < 8; k++) crc = (crc >>> 1) ^ (0xedb88320 & -(crc & 1));
  }
  return (crc ^ -1) >>> 0;
}
function chunk(type, data) {
  const payload = Buffer.concat([Buffer.from(type), data]);
  const size = Buffer.alloc(4), crc = Buffer.alloc(4);
  size.writeUInt32BE(data.length); crc.writeUInt32BE(crc32(payload));
  return Buffer.concat([size, payload, crc]);
}
export function png(file, width, height, pixels) {
  const header = Buffer.alloc(13);
  header.writeUInt32BE(width, 0); header.writeUInt32BE(height, 4);
  header[8] = 8; header[9] = 6;
  const scanlines = Buffer.alloc((width * 4 + 1) * height);
  for (let y = 0; y < height; y++) {
    Buffer.from(pixels.buffer, pixels.byteOffset + y * width * 4, width * 4)
      .copy(scanlines, y * (width * 4 + 1) + 1);
  }
  fs.writeFileSync(file, Buffer.concat([
    Buffer.from([137, 80, 78, 71, 13, 10, 26, 10]),
    chunk('IHDR', header), chunk('IDAT', deflateSync(scanlines)), chunk('IEND', Buffer.alloc(0))
  ]));
}

const mix = (a, b, t) => a.map((v, i) => Math.round(v * (1 - t) + b[i] * t));
const clamp = n => Math.max(0, Math.min(255, Math.round(n)));
export const materials = {};
function material(name, top, bottom, metallic = false) {
  const data = new Uint8Array(32 * 32 * 4);
  for (let y = 0; y < 32; y++) for (let x = 0; x < 32; x++) {
    let rgb = mix(top, bottom, y / 31);
    const noise = (((x * 17 + y * 31 + x * y * 7) % 11) - 5) * 0.35;
    const sheen = metallic ? Math.max(0, 1 - Math.abs((x + y * .25) - 10) / 6) * 20 : 0;
    const edge = y === 0 ? 9 : y === 31 ? -6 : 0;
    rgb.forEach((v, c) => data[(y * 32 + x) * 4 + c] = clamp(v + noise + sheen + edge));
    data[(y * 32 + x) * 4 + 3] = 255;
  }
  materials[name] = data;
  png(path.join(textures, name + '.png'), 32, 32, data);
}
material('cherry', [207, 51, 74], [137, 22, 43]);
material('cherry_edge', [229, 80, 93], [175, 35, 57]);
material('porcelain', [249, 242, 223], [210, 210, 199]);
material('porcelain_edge', [223, 221, 205], [167, 179, 178]);
material('champagne', [242, 211, 140], [177, 122, 53], true);
material('bronze', [183, 131, 68], [102, 67, 34], true);
material('graphite', [43, 57, 68], [18, 27, 37]);
material('inlay', [29, 43, 53], [12, 20, 29]);

// A 64-tick breathing cycle. Interpolation keeps the pulse gentle, including in GUI/hand.
const frames = 16;
const pulse = new Uint8Array(32 * 32 * frames * 4);
for (let f = 0; f < frames; f++) {
  const breath = (1 - Math.cos(f / frames * Math.PI * 2)) / 2;
  for (let y = 0; y < 32; y++) for (let x = 0; x < 32; x++) {
    const core = Math.max(0, 1 - Math.hypot(x - 15.5, y - 15.5) / 23);
    const rgb = mix([50, 147, 137], [205, 255, 225], .22 + breath * .6 + core * .14);
    const offset = (f * 32 * 32 + y * 32 + x) * 4;
    pulse.set([...rgb, 255], offset);
  }
}
materials.pulse = pulse.slice(8 * 32 * 32 * 4, 9 * 32 * 32 * 4);
png(path.join(textures, 'pulse.png'), 32, 32 * frames, pulse);
fs.writeFileSync(path.join(textures, 'pulse.png.mcmeta'), JSON.stringify({
  animation: { frametime: 4, interpolate: true, width: 32, height: 32 }
}, null, 2) + '\n');

export const elements = [];
const faces = ['north', 'south', 'east', 'west', 'up', 'down'];
function box(name, from, to, material, only = faces, shade = true) {
  elements.push({ name, from, to, shade,
    faces: Object.fromEntries(only.map(face => [face, { uv: [0, 0, 16, 16], texture: '#' + material }]))
  });
}
// Three adjacent strips form a pixel-beveled footprint without coplanar overlaps.
function band(name, y0, y1, inset, cut, mat) {
  const lo = inset, hi = 16 - inset;
  box(name + ' core', [lo + cut, y0, lo], [hi - cut, y1, hi], mat, ['up', 'down', 'north', 'south']);
  box(name + ' left', [lo, y0, lo + cut], [lo + cut, y1, hi - cut], mat, ['up', 'down', 'west', 'north', 'south']);
  box(name + ' right', [hi - cut, y0, lo + cut], [hi, y1, hi - cut], mat, ['up', 'down', 'east', 'north', 'south']);
  // Reveal the short steps at each corner (no hidden full internal face).
  for (const x of [lo + cut, hi - cut]) for (const z of [lo, hi - cut]) {
    box(name + ' bevel', [x, y0, z], [x, y1, z + cut], mat,
      [x < 8 ? 'west' : 'east']);
  }
}
band('Rubber foot', 0, .5, 3.5, .7, 'graphite');
band('Base rim', .5, 1.1, 3, .7, 'champagne');
band('Lower bevel', 1.1, 1.8, 2.5, .7, 'porcelain_edge');
band('Porcelain shell', 1.8, 5.25, 2, 1, 'porcelain');
band('Lower seam', 5.25, 5.65, 1.95, 1, 'champagne');
band('Equatorial band', 5.65, 7.05, 1.9, 1, 'graphite');
band('Upper seam', 7.05, 7.45, 1.95, 1, 'champagne');
band('Cherry enamel', 7.45, 11.1, 2, 1, 'cherry');
band('Lid bevel', 11.1, 11.8, 2.5, .7, 'cherry_edge');
band('Lid gold rim', 11.8, 12.2, 3, .7, 'champagne');
band('Lid inset', 12.2, 12.75, 3.5, .6, 'cherry');
band('Crown bezel', 12.75, 13.1, 5.4, .7, 'bronze');
band('Crown gold', 13.1, 13.35, 5.65, .65, 'champagne');
band('Crown button', 13.35, 13.7, 6.15, .5, 'pulse');

// Rotate a front-facing decorative element to each of the four faces.
function panelBox(name, x0, y0, z0, x1, y1, z1, mat, side, shade = true) {
  let from = [x0, y0, z0], to = [x1, y1, z1];
  for (let i = 0; i < side; i++) {
    [from, to] = [[16 - to[2], from[1], from[0]], [16 - from[2], to[1], to[0]]];
  }
  box(name + ' ' + side, from, to, mat, faces, shade);
}
function plaque(side, width, height, z0, z1, mat) {
  const x0 = 8 - width / 2, y0 = 6.5 - height / 2, cut = .45;
  panelBox('Medallion center', x0 + cut, y0, z0, 16 - x0 - cut, y0 + height, z1, mat, side);
  panelBox('Medallion left', x0, y0 + cut, z0, x0 + cut, y0 + height - cut, z1, mat, side);
  panelBox('Medallion right', 16 - x0 - cut, y0 + cut, z0, 16 - x0, y0 + height - cut, z1, mat, side);
}
for (let side = 0; side < 4; side++) {
  plaque(side, 5.8, 6.6, 1.64, 2.03, 'bronze');
  plaque(side, 5.4, 6.2, 1.43, 1.66, 'champagne');
  plaque(side, 4.65, 5.45, 1.25, 1.45, 'inlay');
  const glyph = ['01110', '11011', '00011', '00110', '00100', '00000', '00100'];
  for (let row = 0; row < glyph.length; row++) {
    // Merge neighboring pixels into one cuboid per stroke.
    for (let col = 0; col < 5;) {
      if (glyph[row][col] !== '1') { col++; continue; }
      const begin = col;
      while (col < 5 && glyph[row][col] === '1') col++;
      const pixel = .55, y1 = 8.45 - row * pixel;
      panelBox('Raised question mark', 6.625 + begin * pixel, y1 - pixel, 1.02,
        6.625 + col * pixel, y1, 1.27, row === 6 ? 'pulse' : 'champagne', side, row !== 6);
    }
  }
  // Recessed luminous vents in the belt, and restrained gold corner protectors.
  for (const x of [3.55, 11.3]) {
    panelBox('Vent housing', x - .12, 6.04, 1.72, x + 1.27, 6.68, 1.94, 'inlay', side);
    panelBox('Breathing vent', x, 6.22, 1.67, x + 1.15, 6.50, 1.74, 'pulse', side, false);
  }
  for (const x of [3.05, 12.5]) {
    panelBox('Lid corner guard', x, 9.85, 1.93, x + .45, 11.06, 2.10, 'champagne', side);
    panelBox('Lower corner guard', x, 1.9, 1.93, x + .45, 2.8, 2.10, 'champagne', side);
  }
}

export const display = {
  gui: { rotation: [24, 225, 0], translation: [0, 1, 0], scale: [.86, .86, .86] },
  ground: { rotation: [0, 0, 0], translation: [0, 2.5, 0], scale: [.4, .4, .4] },
  fixed: { rotation: [0, 180, 0], translation: [0, 0, 0], scale: [.72, .72, .72] },
  thirdperson_righthand: { rotation: [75, 45, 0], translation: [0, 2.5, 0], scale: [.36, .36, .36] },
  thirdperson_lefthand: { rotation: [75, -45, 0], translation: [0, 2.5, 0], scale: [.36, .36, .36] },
  firstperson_righthand: { rotation: [0, 225, 0], translation: [0, 1.5, 0], scale: [.42, .42, .42] },
  firstperson_lefthand: { rotation: [0, 135, 0], translation: [0, 1.5, 0], scale: [.42, .42, .42] },
  head: { rotation: [0, 180, 0], translation: [0, 0, 0], scale: [1, 1, 1] }
};
const model = {
  credit: 'Lucky Cobblemon 0.4 - Cherry / Porcelain / Champagne',
  ambientocclusion: true, gui_light: 'side',
  textures: { ...Object.fromEntries(Object.keys(materials).map(name => [name, 'luckycobblemon:block/' + name])),
    particle: 'luckycobblemon:block/cherry' },
  elements, display
};
fs.writeFileSync(path.join(assets, 'models/block/lucky_block.json'), JSON.stringify(model, null, 2) + '\n');
fs.writeFileSync(path.join(assets, 'models/item/lucky_block.json'), JSON.stringify({
  parent: 'luckycobblemon:block/lucky_block', display
}, null, 2) + '\n');
console.log(`Generated ${elements.length} mesh elements, ${Object.keys(materials).length} materials, ${frames} animation frames.`);
