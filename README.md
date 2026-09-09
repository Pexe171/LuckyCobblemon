# Lucky Cobblemon

Standalone lucky block mod for Fabric 1.21.1 and Cobblemon 1.7.3.

## Natural generation (0.5.0)

- Lucky Blocks generate on the Overworld surface, averaging one attempt every
  48 chunks. Existing chunks are unchanged; explore new terrain to find them.
- Every generated block stores independent luck. The distribution favors values
  near zero, covers -90 through +90 in steps of five, and gives +100 and -100 a
  special 2.5% chance each.

## Gameplay

- Craft a Lucky Cobble Block with a dropper, four Poké Balls and four gold ingots.
- Break it to roll a weighted Cobblemon-themed event.
- Outcomes include Pokémon in six rarity pools, shiny jackpots, item bundles,
  trio encounters, a small shrine and harmless unlucky effects.
- Edit `config/luckycobblemon.json` after the first launch to tune weights,
  levels and species pools.

## Build

Run `gradle build` with JDK 21. The remapped jar is written to `build/libs/`.

## Per-block luck

Each Lucky Cobble Block stores its own luck value from -100 to +100. Combine one
block with one or more modifiers anywhere in a crafting grid. Positive blocks
glint and their tooltip shows the stored value. Placing the block preserves it.

| Modifier | Luck |
| --- | ---: |
| Iron ingot / block | +3 / +30 |
| Gold ingot / block | +6 / +60 |
| Emerald / block | +8 / +80 |
| Diamond / block | +12 / +100 |
| Golden apple / enchanted | +40 / +100 |
| Nether star | +100 |
| Rotten flesh | -5 |
| Spider eye / fermented | -10 / -20 |
| Poisonous potato | -10 |
| Pufferfish | -20 |

## Raid and Pokemon structures

- A raid outcome replaces the broken Lucky Block with a real Raid Den from
  Cobblemon Raid Dens. Interact with the Den when ready to start the battle.
- Positive luck increases the raid outcome weight.
- The only built-in structure outcome is a small Pokemon shrine, which always
  summons an epic guardian and contains Cobblemon supplies.
- Generic structures from unrelated mods are intentionally not generated.

## Visual update (0.4.0)

- Compact stepped shell in cherry enamel, porcelain and champagne gold, with
  raised question-mark medallions on all four sides.
- Nine dedicated 32px pixel materials; mint inlays breathe over a 3.2-second
  interpolated texture animation in the world, hand and inventory.
- Separate first-person, third-person, ground and GUI transforms. First-person
  scale is 0.42; third-person is 0.36, instead of the old untransformed full cube.
- Outline/collision fits the smaller capsule. Sparse client-side sparkles use
  vanilla display ticks without a block-entity animation ticker.
- Geometry and materials are authored by `scripts/generate-visuals.mjs`.
  Run `node scripts/preview-visuals.mjs` for an orthographic asset preview.
- Run `gradle runClient -PvisualCheck` to bake the actual models in an isolated
  Minecraft client, capture two renderer frames in `visual-run/screenshots`,
  and close automatically. The check mod is not included in the release JAR.
  This visual-only run omits Cobblemon because its Kotlin reflection uses
  production names; the dependency override is confined to `visual-run/config`.
  It verifies graphics, not raid battles or the full modpack.
