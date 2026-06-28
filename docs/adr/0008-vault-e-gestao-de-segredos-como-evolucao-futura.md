# ADR 0008 - Vault e gestao de segredos como evolucao futura

## Status

Aceito

## Contexto

A aplicacao precisaria idealmente ler credenciais, chaves e segredos a partir de um cofre centralizado, evitando exposicao em arquivos locais ou configuracoes estaticas.

No recorte do desafio, isso aumentaria o volume de integracao sem trazer ganho proporcional para a avaliacao principal, desde que as credenciais nao sejam expostas no repositorio.

## Decisao

Adiar a integracao com Vault ou Secrets Manager e registrar o uso de configuracoes locais seguras e variaveis de ambiente como abordagem transitória.

## Alternativas consideradas

- Vault centralizado
- AWS Secrets Manager
- credenciais em arquivo de configuracao versionado
- variaveis de ambiente e configuracao local fora do repositorio

## Consequencias

- a entrega atual permanece viavel no prazo
- a superficie operacional fica menor
- o desenho de seguranca fica menos completo do que seria ideal em producao
- a evolucao para cofre de segredos continua documentada como melhoria futura

## Situacao atual

O projeto registra o desejo de evolucao para cofre de segredos, mas a entrega priorizou o que gera valor imediato no desafio.
