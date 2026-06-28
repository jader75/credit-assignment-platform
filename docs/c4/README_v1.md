# Diagrama C4 - V2

Este diretório guarda a documentação C4 da solução alinhada ao estado final da aplicação
## Nivel 1 - Contexto

```mermaid
%%{init: {"theme":"base","themeVariables":{"background":"#f8fafc","primaryColor":"#ffffff","secondaryColor":"#ffffff","tertiaryColor":"#ffffff","primaryBorderColor":"#cbd5e1","secondaryBorderColor":"#cbd5e1","tertiaryBorderColor":"#cbd5e1","lineColor":"#64748b","fontColor":"#334155","primaryTextColor":"#334155","secondaryTextColor":"#334155","tertiaryTextColor":"#334155","edgeLabelBackground":"#ffffff","edgeLabelColor":"#334155","fontFamily":"Inter, Arial, sans-serif"}}}%%
C4Context
title SRM Credit Engine - Contexto

Person(operator, "Operador da mesa", "Simula operacoes, consulta extrato e administra o fluxo operacional.")
System(system, "SRM Credit Engine", "Plataforma de cessao de credito multimoedas para simulacao, liquidacao e consulta analitica.")
System_Ext(frankfurter, "Frankfurter API", "Fonte externa de cotacao de moedas.")
System_Ext(postgres, "PostgreSQL", "Persistencia relacional principal.")
System_Ext(redis, "Redis", "Cache operacional de taxas de cambio por par de moeda.")

Rel(operator, system, "Usa")
Rel(system, frankfurter, "Consulta taxas de cambio")
Rel(system, postgres, "Persiste lotes, recebiveis, usuarios e taxas manuais/mockadas")
Rel(system, redis, "Lê e grava cache de cambio")
```

## Nivel 2 - Container

```mermaid
%%{init: {"theme":"base","themeVariables":{"background":"#f8fafc","primaryColor":"#ffffff","secondaryColor":"#ffffff","tertiaryColor":"#ffffff","primaryBorderColor":"#cbd5e1","secondaryBorderColor":"#cbd5e1","tertiaryBorderColor":"#cbd5e1","lineColor":"#64748b","fontColor":"#334155","primaryTextColor":"#334155","secondaryTextColor":"#334155","tertiaryTextColor":"#334155","edgeLabelBackground":"#ffffff","edgeLabelColor":"#334155","fontFamily":"Inter, Arial, sans-serif"}}}%%
C4Container
title SRM Credit Engine - Containers

Person(operator, "Operador da mesa", "Acessa a aplicacao via navegador.")

System_Boundary(s1, "SRM Credit Engine") {
  Container(spa, "Frontend Angular", "SPA", "Interface do operador para simulacao, extrato, backoffice e cadastro de taxas.")
  Container(api, "Backend Spring Boot", "Java 21 / Spring Boot", "API REST, regras de negocio, seguranca, simulacao, importacao e integracoes.")
  ContainerDb(db, "PostgreSQL", "Banco relacional", "Schema principal da solucao.")
  ContainerDb(cache, "Redis", "Cache chave por par", "Armazena taxas de cambio operacionais com TTL curto.")
}

System_Ext(frankfurter, "Frankfurter API", "Consulta externa de cotacao de moedas.")

Rel(operator, spa, "Interage")
Rel(spa, api, "Chama API REST")
Rel(api, db, "Lê e grava dados")
Rel(api, cache, "Lê e grava taxas cacheadas")
Rel(api, frankfurter, "Consulta cotacao externa")
```

## Leitura rapida

- o frontend e uma SPA Angular separada do backend
- o backend concentra a orquestracao das regras de negocio
- PostgreSQL permanece como persistencia relacional principal
- Redis atua como cache operacional da taxa de cambio
- Frankfurter permanece como integracao externa de cotacao
