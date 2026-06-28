# ADR 0002 - Precificacao por Strategy e spread fixo por tipo

## Status

Aceito

## Contexto

O desafio exige precificacao por tipo de recebivel, com spreads diferentes por perfil de risco.

Era importante evitar espalhar regras de precificacao em condicionais soltas ou em configuracoes que deixassem a regra pouco rastreavel.

## Decisao

Aplicar **Strategy Pattern** para a precificacao e manter o spread base ligado ao tipo de recebivel e a estrategia correspondente.

## Alternativas consideradas

- if/else centralizado no service de precificacao
- configuracao totalmente externa em tabela sem estrategia
- motor generico com regras dinamicas

## Consequencias

- a regra de negocio fica explicita e separada por tipo
- novas estrategias entram com baixo impacto no restante do sistema
- o calculo fica mais facil de testar e justificar
- a solucao evita overengineering de motor dinamico sem ganho real para o desafio

## Situacao atual

As estrategias estao implementadas para os tipos centrais do desafio, com a formula base do valor presente e conversao cross-currency no final quando necessario.
