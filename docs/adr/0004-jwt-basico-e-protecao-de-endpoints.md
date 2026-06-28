# ADR 0004 - JWT basico e protecao de endpoints

## Status

Aceito

## Contexto

O desafio pedia um minimo de seguranca para evitar acesso irrestrito aos endpoints e permitir rastreabilidade de usuario.

Nao havia tempo nem justificativa para montar um stack completo de IAM, SSO ou cofres de segredo.

## Decisao

Implementar **JWT basico no backend**, com protecao dos endpoints relevantes e propagacao do token pelo frontend.

## Alternativas consideradas

- deixar os endpoints livres
- implementar SSO completo
- usar autenticao apenas no frontend
- adicionar uma camada pesada de autorizacao sem necessidade imediata

## Consequencias

- o backend passa a exigir autenticacao para operacoes protegidas
- o frontend envia o token via interceptor
- os acessos ficam mais rastreaveis
- a solucao continua simples o suficiente para o prazo do desafio

## Situacao atual

O login expede token, o backend valida o JWT e as requisicoes passam a carregar contexto de usuario para logs e audicao basica.
