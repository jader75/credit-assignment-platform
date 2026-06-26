# Project Rules

## Objetivo

Desenvolver a plataforma SRM Credit Engine para cessao de credito multimoedas, capaz de receber recebiveis, precificar ativos financeiros, calcular desagio, converter moedas quando necessario, liquidar operacoes e registrar transacoes de forma auditavel.

## Politica de uso de IA

- O uso de IA e permitido como apoio de produtividade, mas a autoria tecnica continua sendo do desenvolvedor.
- Todo codigo entregue deve ser compreendido, revisado e justificado.
- O arquivo `AI_USAGE.md` deve registrar prompts estrategicos, pontos em que a IA falhou ou alucinou, correcoes aplicadas e analise critica do uso.
- Decisoes tomadas por tradeoff com IA devem ser documentadas quando forem relevantes para arquitetura, qualidade ou escopo.

## Regras de negocio

### Currency Engine

- Armazenar taxas de cambio.
- Consultar taxa vigente.
- Permitir atualizacao manual de taxa.
- Manter desenho compativel com integracao externa futura ou mockada.

### Pricing Engine

- Aplicar Strategy Pattern para desacoplar regras de precificacao por tipo de recebivel.
- Usar a formula base:

```text
Valor Presente = Valor Face / (1 + Taxa Base + Spread)^Prazo
```

- Aplicar spread por tipo de recebivel.
- Para operacoes cross-currency, calcular primeiro o valor presente e converter no final pela taxa vigente.
- Usar precisao decimal adequada para valores financeiros.

### Recebiveis

- Duplicata mercantil possui spread de referencia de 1.5% ao mes.
- Cheque pre-datado possui spread de referencia de 2.5% ao mes.
- Novos tipos devem entrar por novas estrategias ou configuracoes coerentes com o modelo.

### Liquidacao

- Nenhuma liquidacao pode ficar pela metade.
- Operacoes financeiras devem respeitar ACID.
- O modelo deve permitir controle de concorrencia, preferencialmente com optimistic locking.

### Extrato analitico

- Disponibilizar consulta de liquidacoes por periodo, cedente e moeda.
- Suportar consulta de grandes volumes com paginacao e filtros.
- Usar SQL otimizado ou query builder para relatorios quando fizer mais sentido que ORM puro.

## API

- Seguir abordagem API First sempre que houver definicao de contrato relevante.
- Usar verbos HTTP corretos.
- Retornar codigos de status semanticos.
- Documentar contratos via OpenAPI/Swagger.

## Frontend

- Implementar painel do operador.
- Permitir entrada de valor, vencimento e tipo do recebivel.
- Exibir simulacao do valor liquido em tempo proximo do real.
- Implementar grid de transacoes com paginacao server-side.
- Implementar filtros dinamicos.
- Separar componentes de UI, estado e regras de negocio.

## Requisitos nao funcionais

- Implementar tratamento global de excecoes.
- Tratar erros inesperados de forma controlada.
- Planejar criterios de aceite que considerem usabilidade, seguranca, desempenho e escalabilidade.
- Considerar observabilidade, resiliencia e automacao como diferenciais de senioridade.

## Entregaveis obrigatorios

- API RESTful documentada.
- OpenAPI/Swagger.
- README claro com setup, design e decisoes.
- `AI_USAGE.md` ao final do projeto.
- Diagrama C4 nivel 1 e 2.
- Diagrama ER.
- Scripts DDL.
- Git hooks.
- CI/CD com build, testes e lint.
- Tag SemVer para a entrega final.
- Historico Git organizado, com uso de rebase interativo quando fizer sentido.

## Criterios de avaliacao

- Fundamentacao teorica para linguagem, framework e bibliotecas.
- Dominio do problema financeiro.
- Precisao numerica e consistencia transacional.
- Design aderente a SOLID, DRY e KISS.
- Historico Git limpo e rastreavel.
- Uso consciente de IA.
- Arquitetura compativel com senioridade, incluindo observabilidade, resiliencia e automacao.
