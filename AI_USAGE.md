# AI_USAGE

## Objetivo

Este documento registra como a IA foi usada ao longo do projeto SRM Credit Engine, com foco em organizacao de trabalho, apoio em documentacao, revisao tecnica e iteracao sobre bugs e ajustes.

O texto foi consolidado a partir do historico do repositorio, das decisoes documentadas em `docs/ai/`, do backlog da entrega e das conversas de desenvolvimento.

## Diretriz geral de uso

- A IA foi usada como apoio pontual de engenharia, nao como substituto de autoria tecnica.
- As sugestoes foram validadas no contexto do codigo, do fluxo de negocio e do escopo do desafio.
- Quando a IA propôs um caminho simplificado demais, excessivo ou pouco aderente, a proposta foi corrigida antes de virar implementacao.
- O criterio principal foi manter a entrega aderente ao desafio e aos artefatos ja produzidos.

## Padroes de prompt mais usados

### 1. Leitura de contexto e planejamento de story

**Padrao usado**
- pedir leitura do README, das regras em `docs/ai/` e do `agent.md`
- pedir analise do estado atual antes de qualquer alteracao
- pedir plano de execucao antes de editar arquivos

**Objetivo**
- entender o contexto do projeto sem abrir escopo indevido
- respeitar a ordem de stories e o estado real da branch
- evitar alterar codigo antes de alinhar a intencao

**Valor gerado**
- organizacao do trabalho por story
- reducao de retrabalho
- melhor rastreabilidade das decisoes

### 2. Refatoracao e correcao pontual

**Padrao usado**
- indicar arquivo ou area especifica
- informar o comportamento esperado
- pedir alteracao minima necessaria
- exigir validacao posterior por build ou teste

**Objetivo**
- corrigir bug sem inflar escopo
- manter aderencia ao padrao do projeto
- evitar reescrita desnecessaria

**Valor gerado**
- ajustes localizados
- menor risco de regressao
- historico de commit mais legivel

### 3. Documentacao tecnica e PR

**Padrao usado**
- pedir resumo em Markdown copiavel
- pedir titulo, branch, base, commits, validacao e observacoes
- pedir documentos finais com formato pronto para banca

**Objetivo**
- acelerar fechamento de story
- manter consistencia na documentacao
- padronizar o texto de PR e README

**Valor gerado**
- reducao de tempo em escrita manual
- padrao uniforme de documentacao
- facil reutilizacao em outros projetos

### 4. Revisao de modelagem, diagramas e banco

**Padrao usado**
- pedir revalidacao entre migrations, schema, seed e diagramas
- pedir leitura critica do modelo real vs. modelo documentado

**Objetivo**
- evitar documentacao defasada
- separar seed, estrutura e dados de referencia
- deixar claro o que e artefato final e o que e material de estudo

**Valor gerado**
- alinhamento entre modelagem e entrega

### 5. UI, layout e mensagens

**Padrao usado**
- pedir ajuste de bloco especifico da tela
- pedir comparacao entre tipografia, alinhamento e hierarquia visual
- pedir reducao de ruido visual

**Objetivo**
- manter interface simples e profissional
- evitar solucoes esteticas improvisadas
- preservar leitura do fluxo principal

**Valor gerado**
- refinamento progressivo da simulacao
- consistencia visual com o restante do painel

### 6. Seguranca, integracao e resiliencia

**Padrao usado**
- descrever o objetivo funcional e o limite de escopo da entrega
- pedir consolidacao das alternativas que o desenvolvedor ja tinha em analise
- pedir revisao de riscos operacionais, fallback e pontos de observabilidade
- pedir checagem de aderencia entre o que foi implementado e o que ficou como melhoria futura

**Objetivo**
- evitar overengineering
- manter o desenho aderente ao desafio e ao prazo
- documentar escolhas tecnicas ja validadas pelo autor do projeto
- preservar clareza entre fluxo principal e comportamento de contingencia

**Valor gerado**
- registro dos tradeoffs usados na implementacao
- apoio na organizacao do racional tecnico para seguranca, cache e fallback
- alinhamento entre a implementacao final e o escopo ja decidido

## Onde a IA ajudou de forma objetiva

- Organizar o desenvolvimento em stories curtas e auditaveis.
- Estruturar commits menores e com contexto claro.
- Ajudar a consolidar documentacao da arquitetura sem inventar complexidade.
- Revisar o fluxo do cache de cambio e o papel do fallback.
- Ajudar a transformar discussoes dispersas em decisoes documentadas.
- Sugerir formatos de PR mais diretos e copiaveis.
- Revisar a relacao entre V1 estrutural e V2 de seed no banco.
- Ajudar a consolidar gaps identificados e a reorganizacao do backlog, o que reabriu o escopo das stories finais e trouxe de volta itens como importacao de lotes, cache de cambio e telas administrativas que tinham ficado de fora.

## Pontos em que a IA errou ou precisou ser corrigida

### 1. Erro na definicao de stories a partir das diretivas recebidas

**O que aconteceu**
- em alguns momentos a IA consolidou o backlog de forma mais ampla do que o necessario.
- isso levou a uma interpretacao mais expansiva do fechamento das stories finais.
- isso aumentou o tamanho das stories finais e reintroduziu funcionalidades que tinham sido deixadas de fora, como importacao, cache e ajustes administrativos.

**Correcao**
- as stories foram reordenadas e ajustadas com base na decisao do desenvolvedor e no estado real do repositorio.
- funcionalidades que ficaram de fora foram retomadas como adicoes especificas das stories finais, e nao como uma reinterpretacao total do escopo.

**Licao**
- em projeto avaliado, a IA pode ajudar a mapear gaps, mas a fronteira do que entra em cada story precisa ficar sob decisao humana.

### 2. Risco de tratar a taxa integrada como persistencia operacional principal

**O que aconteceu**
- o desenho inicial precisou ser refinado para separar melhor fonte operacional, cache e contingencia.

**Correcao**
- a taxa de cambio passou a ser tratada como cache operacional em Redis por par de moeda.
- o banco ficou explicitamente como fallback de contingencia manual/mock, nao como fonte principal da operacao online.

**Licao**
- em integracoes financeiras, cache, auditoria e persistencia nao devem ser confundidos.

### 3. Tendencia a gerar layout visual excessivamente contrastado

**O que aconteceu**
- algumas propostas iniciais de UI usaram cores ou blocos mais fortes do que o padrao do projeto.

**Correcao**
- a hierarquia visual foi reduzida.
- o bloco cambial foi refinado iterativamente ate ficar consistente com o restante do card de resultado.

**Licao**
- em interface operacional, a tipografia e o alinhamento frequentemente valem mais que cores fortes.

### 4. Estilo inicial do C4 com contraste visual inadequado

**O que aconteceu**
- a primeira versao do C4 ficou pouco legivel em alguns renderers.

**Correcao**
- a paleta foi ajustada para fundo claro, caixas brancas e linhas em cinza controlado.
- depois foi criado um clone educacional separado com L3 e L4 para estudo.

**Licao**
- diagramas devem privilegiar legibilidade no renderer real da banca, nao apenas em teoria.

### 5. Risco de misturar seed com desenho estrutural de banco

**O que aconteceu**
- foi necessario revalidar se os artefatos de banco representavam estrutura, seed ou ambos.

**Correcao**
- `docs/db/schema.sql` e o diagrama V1 foram tratados como estrutura final.
- `V2__seed_currency_reference_data.sql` e `docs/db/data.sql` passaram a representar seed/dados de referencia.

**Licao**
- a documentacao do banco deve separar claramente desenho fisico e carga inicial.

### 6. Formato de PR e commits

**O que aconteceu**
- as primeiras propostas de sumario de PR precisaram ser ajustadas para o padrao exato do projeto.

**Correcao**
- o formato passou a ser Markdown copiavel, com branch, base, commits, validacao e observacoes.
- os commits foram reorganizados em mensagens curtas e contextuais.

**Licao**
- para avaliacao, formato e clareza contam tanto quanto o conteudo tecnico.

## Decisoes importantes registradas com apoio da IA

- A entrega foi mantida como monolito modular.
- O backend permaneceu em camadas com controllers finos e ApplicationServices claros.
- O fluxo de cambio foi desenhado como cache operacional com fallback controlado.
- A documentacao foi tratada como parte da entrega, nao como apendice.
- O C4 oficial ficou em L1, L2 e L3, enquanto L4 nao fazia parte do escopo.
- O banco foi documentado com separacao entre estrutura, seed e diagramas.

## Analise critica do uso da IA

### O que funcionou bem

- A IA foi utilizada para transformar discussoes em decisoes e artefatos concretos.
- Funcionou bem como apoio de documentacao, revisao textual e organizacao de backlog.
- Ajudou a manter consistencia entre README, prompts, diagramas e historico da story.

### Onde a IA atrapalhou

- Quando tentou sugerir caminhos que pareciam genericamente bons, mas nao eram os mais aderentes ao prazo ou ao desenho do desafio.
- Quando a resposta precisou ser validada visualmente, a primeira versao nem sempre ficou boa de primeira.
- Quando o risco era de sofisticar demais a solucao para um desafio que pedia principalmente clareza e entrega defensavel.

### Como isso foi corrigido

- Toda sugestao foi confrontada com o contexto real do repo.
- As alteracoes relevantes foram validas com build, testes ou inspecao de artefatos.
- Decisoes mais sensiveis foram registradas em `docs/ai/`.

### Conclusao critica

A IA atuou como apoio pontual de organizacao, revisao e documentacao. Na pratica, participou menos como fonte de decisao e mais como acelerador controlado do trabalho ja definido pelo desenvolvedor.
No contexto deste desafio, o criterio tecnico permaneceu com o desenvolvedor: as decisoes principais vieram da leitura do codigo, do backlog, das regras do desafio e da validacao pratica das alteracoes.
A participacao da IA foi limitada a suporte de texto, organizacao e revisao pontual.
A contribuicao da IA foi util para acelerar o trabalho, mas de forma controlada e subordinada ao julgamento tecnico humano.

## Como este projeto recomenda usar IA daqui para frente

- Pedir sempre com contexto, escopo e criterio de saida.
- Referenciar os arquivos relevantes.
- Dizer explicitamente quando a resposta deve ser apenas analise.
- Exigir formato copiavel quando o resultado for PR, README ou documento final.
- Validar qualquer sugestao que toque arquitetura, seguranca, dados ou UX.
- Registrar tradeoffs relevantes em `docs/ai/`.

## Referencias internas

- `docs/ai/project-rules.md`
- `docs/ai/architecture.md`
- `docs/ai/coding-standards.md`
- `docs/ai/srm-credit-engine.prompt`
- `agent.md`
