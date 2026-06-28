# ADR 0007 - Mensageria adiada para persistencia assincrona

## Status

Aceito

## Contexto

Em um desenho mais ambicioso, a persistencia de operacoes poderia ser desacoplada por mensageria para reduzir latencia percebida e absorver picos de carga.

Para o desafio atual, esse caminho exigiria mais infraestrutura, mais pontos de falha, mais observabilidade distribuida e mais tempo de validacao do que o disponivel.

## Decisao

Manter a persistencia direta em banco relacional nesta entrega e registrar mensageria assíncrona como evolucao futura.

## Alternativas consideradas

- persistencia via fila com consumidor dedicado
- outbox pattern com publicação assíncrona
- event-driven para liquidações e importacoes
- escrita direta no banco com transacao ACID

## Consequencias

- o fluxo fica mais simples de operar e de explicar na banca
- a entrega preserva consistencia transacional local
- reduz-se o custo de infraestrutura e de testes integrados
- a evolucao para fila continua possivel em uma revisao futura

## Situacao atual

O backend continua com persistencia relacional direta e a fila fica registrada apenas como melhoria de arquitetura para uma proxima iteracao.
