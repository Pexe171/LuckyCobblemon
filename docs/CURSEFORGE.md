# Publicação no CurseForge

O projeto está configurado com o ID **1689137**. O CurseForge exige título, resumo e descrição em inglês; os textos abaixo estão prontos para colar.

## Campos iniciais

| Campo | Valor recomendado |
| --- | --- |
| Name | Lucky Cobblemon: Fortune Blocks |
| Summary | Cobblemon-themed lucky blocks with per-block luck, wild encounters, loot, shrines and rare jackpots. |
| Class | Mods |
| Main category | Adventure and RPG |
| Additional categories | Addons, World Gen e Server Utility, se disponíveis |
| License | MIT |
| Logo | `docs/media/lucky-cobblemon-icon.png` |

## Description

```markdown
# 🍀 Lucky Cobblemon: Fortune Blocks

**Break the block, test your luck, and discover a Cobblemon-themed surprise!**

Lucky Cobblemon is a Fabric mod that adds craftable and naturally generated Lucky Blocks. Every block stores its own luck value from **-100 to +100** and triggers a weighted event when broken.

## ✨ Features

- Pokémon encounters across six rarity tiers
- Shiny encounters and a legendary shiny jackpot
- Poké Ball and Rare Candy supply bundles
- Three-Pokémon encounters
- Lucky Shrines with loot and an epic guardian
- Harmless unlucky effects
- Optional integration with Cobblemon Raid Dens
- Per-block luck values preserved when placed
- Configurable event weights, Pokémon levels and species pools
- Natural generation in newly generated Overworld chunks
- Four craftable variants: Common, Rare, Legendary and Cursed
- 15 new event variations for supplies, group encounters, shrines and non-lethal bad luck
- Brazilian Portuguese and English localization
- In-game probability viewer for the Lucky Block in your main hand
- Safe configuration reload command for server operators
- Creative-mode activation disabled by default

## 🎲 Luck System

Combine a Lucky Block with minerals or special items to change its luck. Positive luck favors rarer rewards, while negative luck increases unlucky outcomes.

## 🧰 Requirements

- Minecraft 1.21.1
- Fabric Loader 0.17.2 or newer
- Fabric API 0.116.6+1.21.1 or newer
- Cobblemon 1.7.3 or newer
- Java 21 or newer

Cobblemon Raid Dens 0.11.7 or newer is optional. When it is absent, the Raid Den outcome becomes a rare Pokémon encounter without executing an unavailable command.

Install the mod and its required dependencies on both the server and every connecting client.

## ⚙️ Configuration

Edit `config/luckycobblemon.json` after the first launch to customize event weights, level ranges, species pools and Creative-mode activation. Invalid configurations are automatically backed up before defaults are restored.

Use `/luckycobblemon chances` while holding a Lucky Block to inspect its exact outcome chances. Server operators can use `/luckycobblemon reload` to validate and apply configuration changes without restarting.

## 🔗 Links

- [Source Code](https://github.com/Pexe171/LuckyCobblemon)
- [Issue Tracker](https://github.com/Pexe171/LuckyCobblemon/issues)

Lucky Cobblemon is an independent, unofficial community add-on and is not affiliated with or endorsed by Mojang Studios, The Pokémon Company, Cobblemon or Cobblemon Raid Dens.
```

## Dependências no arquivo

| Projeto | Project ID | Relação |
| --- | ---: | --- |
| Fabric API | `306612` | Required Dependency |
| Cobblemon | `687131` | Required Dependency |
| Cobblemon Raid Dens | `1349231` | Optional Dependency |

## Arquivo a enviar

Envie `build/libs/lucky-cobblemon-0.6.0.jar`. Não envie o JAR `-sources` como arquivo principal.

## Automação

O workflow `.github/workflows/curseforge.yml` compila e envia o JAR pela API oficial quando uma GitHub Release é publicada ou quando executado manualmente. Crie no GitHub Actions o segredo `CURSEFORGE_API_TOKEN` com um token da sua conta CurseForge.

## Checklist final

- [ ] Executar `.\gradlew.bat clean build` com JDK 21.
- [ ] Testar em Minecraft 1.21.1 com Fabric API e Cobblemon.
- [ ] Testar servidor dedicado e cliente.
- [ ] Testar blocos com sorte negativa, neutra e positiva.
- [ ] Confirmar a geração em chunks novos.
- [ ] Testar com e sem Cobblemon Raid Dens.
- [ ] Confirmar a configuração `config/luckycobblemon.json`.
- [ ] Confirmar os comandos em PT-BR e inglês.
- [ ] Confirmar que o modo Criativo não ativa eventos por padrão.
- [ ] Testar as quatro variantes e suas receitas.
- [ ] Conferir os 15 novos eventos e variações.
- [ ] Criar a tag e a GitHub Release `v0.6.0`.
- [ ] Conferir o arquivo no painel do CurseForge após o upload.
