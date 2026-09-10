# Roteiro de testes — Lucky Cobblemon 0.6.1

Use este roteiro em uma instalação normal do Minecraft 1.21.1 com Fabric API, Cobblemon e o JAR `lucky-cobblemon-0.6.1.jar`.

## Antes de começar

- Faça backup do mundo.
- Retire versões antigas do Lucky Cobblemon da pasta `mods`.
- Confirme que existe apenas o JAR `0.6.1`.
- Inicie um mundo de teste com comandos permitidos.
- Execute `/luckycobblemon logs` e confira se o caminho termina em `logs/luckycobblemon/session-....log`.
- Deixe `eventLogging` como `true` em `config/luckycobblemon.json`.

## 1. Inicialização e aparência

- Confirme que o jogo abre sem crash.
- Abra a aba Funcionais e procure as quatro variantes.
- Coloque no chão o bloco Comum, Raro, Lendário e Amaldiçoado.
- Confira texturas, animação, partículas, iluminação, colisão e nome em PT-BR.
- Segure cada bloco e confira a sorte no tooltip: Comum `0`, Raro `+35`, Lendário `+75` e Amaldiçoado `-65`.

## 2. Receitas e preservação da sorte

- Fabrique o bloco Comum.
- Fabrique as variantes Rara, Lendária e Amaldiçoada.
- Ajuste a sorte de cada variante na bancada com os materiais indicados no README.
- Coloque e quebre novamente um bloco ajustado; confira se a mensagem mostra o mesmo valor de sorte.
- Execute `/luckycobblemon chances` segurando cada variante e confira se as porcentagens mudam.

## 3. Abertura dos blocos

Abra pelo menos:

- 20 blocos Comuns;
- 20 blocos Raros;
- 20 blocos Lendários;
- 20 blocos Amaldiçoados.

Para cada abertura, observe se:

- aparece uma mensagem correspondente ao resultado;
- Pokémon surgem perto do bloco e no nível informado;
- itens realmente caem e podem ser coletados;
- efeitos, partículas e sons terminam normalmente;
- santuários não substituem blocos sólidos importantes;
- nenhum resultado causa crash, travamento ou spam no chat.

Não é necessário obter todos os 26 eventos em uma única sessão. O log mostrará exatamente quais categorias e variações apareceram.

## 4. Raid Dens opcional

### Sem Cobblemon Raid Dens

- Jogue sem o mod de Raid Dens.
- Quando a categoria `raid` aparecer, confirme que surge um Pokémon raro no lugar.
- Confira no log a linha `event="raid_fallback" detail="cobblemonraiddens_not_installed"`.

### Com Cobblemon Raid Dens

- Instale uma versão compatível e repita o teste.
- Confirme que o Raid Den aparece e pode ser usado.
- Se falhar, veja se o Pokémon raro alternativo aparece e se o log registra `command_failed`.

## 5. Criativo, configuração e recarga

- No Criativo, quebre um bloco com `allowCreativeActivation: false`; nenhum evento deve ocorrer.
- Mude para `true`, execute `/luckycobblemon reload` e teste novamente.
- Altere temporariamente um peso e confirme a mudança com `/luckycobblemon chances`.
- Coloque um valor inválido em `maxLogFiles` (por exemplo, `0`) e confirme que a recarga é rejeitada sem perder a configuração anterior.
- Restaure `maxLogFiles` para `20`.

## 6. Geração natural

- Explore chunks ainda não gerados no mundo normal.
- Confirme que blocos naturais aparecem apenas em locais válidos na superfície.
- Quebre alguns e confira no log valores de sorte diferentes, inclusive negativos.

## O que enviar se encontrar um problema

Envie:

1. o arquivo da sessão mostrado por `/luckycobblemon logs`;
2. `logs/latest.log` se houve crash ou erro;
3. captura de tela ou vídeo;
4. lista de mods e versões;
5. descrição curta do que você fez, do que esperava e do que aconteceu.

Antes de enviar publicamente, abra o arquivo e confira se você deseja compartilhar o nome e UUID registrados nele.
