# Architecture Decision Records

Este diretório registra decisoes arquiteturais relevantes da solucao.

## Objetivo

- manter rastreavel o raciocinio por tras das escolhas mais importantes
- separar decisoes de estrutura, negocio, seguranca e operacao
- deixar claro o que foi implementado e quais tradeoffs foram aceitos

## Estrutura

- `0001-monolito-modular-e-camadas.md`
- `0002-precificacao-por-strategy-e-spread-fixo.md`
- `0003-cambio-com-redis-e-fallback-contingente.md`
- `0004-jwt-basico-e-protecao-de-endpoints.md`
- `0005-schema-v1-e-seed-v2.md`
- `0006-observabilidade-estruturada.md`
- `0007-mensageria-adiada-para-persistencia-assincrona.md`
- `0008-vault-e-gestao-de-segredos-como-evolucao-futura.md`

## Uso

Cada ADR deve registrar:

- contexto
- decisao
- alternativas consideradas
- consequencias
- situacao atual

Os ADRs devem refletir o estado real do repositorio e nao uma arquitetura idealizada.
