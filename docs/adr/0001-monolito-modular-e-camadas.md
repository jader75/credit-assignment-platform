# ADR 0001 - Monolito modular e separacao em camadas

## Status

Aceito

## Contexto

A solucao SRM Credit Engine precisava atender um desafio tecnico com prazo curto, mantendo clareza de negocio, facilidade de validacao e baixo custo operacional.

Havia a possibilidade teorica de dividir a aplicacao em servicos menores, mas isso aumentaria a complexidade de deploy, integracao, observabilidade e manutencao sem entregar valor proporcional ao problema proposto.

## Decisao

Adotar um **monolito modular** com separacao em camadas:

- Presentation
- Application
- Domain
- Infrastructure

## Alternativas consideradas

- microsservicos por dominio
- arquitetura orientada a eventos
- monolito sem separacao clara de camadas

## Consequencias

- a solucao fica mais simples de entender, testar e entregar
- o dominio permanece isolado de detalhes de infraestrutura
- consultas analiticas podem usar acesso mais direto quando isso simplificar o caminho
- a aplicacao nao ganha o custo de operar varios servicos sem necessidade real

## Situacao atual

O repositorio segue este modelo e os controllers permanecem finos, delegando a orquestracao para `ApplicationService`.
