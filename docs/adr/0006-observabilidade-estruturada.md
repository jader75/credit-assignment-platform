# ADR 0006 - Observabilidade estruturada

## Status

Aceito

## Contexto

A aplicacao precisava registrar eventos operacionais de forma util para depuracao, auditoria basica e leitura de fluxo.

Somente logs soltos nao ajudariam o bastante, e uma stack de observabilidade completa seria excessiva para o desafio.

## Decisao

Adotar:

- logs estruturados
- `correlationId`
- `transactionId`
- campo `step` para marcar fases do fluxo
- tratamento global de excecoes

## Alternativas consideradas

- logs livres sem padrao
- tracing completo com infraestrutura extra
- metrics e dashboards externos desde o inicio

## Consequencias

- a leitura dos fluxos fica mais clara
- os erros podem ser rastreados por request e por negocio
- a solucao permanece simples e defensavel
- metricas e tracing ficam como evolucao futura quando houver justificativa real

## Situacao atual

O projeto usa logs estruturados em pontos relevantes do fluxo, com enfase em cambio, autenticacao, alteracao de status e tratamento de excecoes.
