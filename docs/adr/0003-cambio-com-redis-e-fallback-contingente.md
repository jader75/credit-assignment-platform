# ADR 0003 - Câmbio com Redis e fallback contingente

## Status

Aceito

## Contexto

A integracao de cambio precisava atender tres objetivos ao mesmo tempo:

- consultar cotacoes externas de forma segura
- reduzir dependencia direta da fonte externa no fluxo operacional
- manter um fallback simples para continuidade do desafio

Persistir a taxa integrada como fonte operacional principal foi reconsiderado, pois isso misturava cache, contingencia e historico de forma pouco clara.

## Decisao

Usar **Redis por par de moeda** como cache operacional de cambio, com refresh periodico pela API externa e fallback para a base manual/mock do banco quando a consulta externa falhar.

## Alternativas consideradas

- consultar a API externa a cada simulacao
- persistir a taxa integrada no banco como fonte principal
- usar apenas banco sem cache
- usar mensageria ou cache distribuido mais complexo

## Consequencias

- o fluxo operacional fica mais rapido e previsivel
- a taxa utilizada fica separada da base historica/manual
- o fallback fica claro e explicito
- se Redis e fallback falharem, a aplicacao devolve indisponibilidade de forma controlada

## Situacao atual

O backend resolve a taxa em ordem de prioridade operacional, com cache por chave `FROM:TO`, retry, circuit breaker e timeout curto no cliente HTTP.
