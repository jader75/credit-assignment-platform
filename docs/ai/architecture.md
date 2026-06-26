# Architecture Decisions

## Estilo geral

- Backend em monolito modular.
- Separacao em camadas: Presentation, Application, Domain e Infrastructure.
- O dominio nao deve depender de detalhes de infraestrutura.
- Relatorios podem acessar persistencia diretamente quando isso simplificar consultas analiticas.
- Relatorios podem usar um fluxo mais direto em duas camadas quando nao houver regra de negocio relevante no meio.

## Backend

- Java 21 e Spring Boot.
- Stack escolhida por tipagem forte, maturidade do ecossistema e adequacao ao contexto financeiro.
- PostgreSQL como banco relacional principal.
- Flyway para versionamento de schema.
- Gradle como ferramenta de build.
- Testcontainers para testes de integracao com PostgreSQL quando a fidelidade ao banco real for relevante.
- H2 pode ser usado apenas em testes onde diferencas de dialeto e comportamento transacional nao afetem a validade do resultado.
- A nomenclatura da camada de aplicacao deve usar `ApplicationService`, evitando `UseCase` quando isso reduzir familiaridade para o projeto.

## Dominio

- Preferir modelos ricos.
- Entidades devem ser criadas por factories quando houver invariantes obrigatorias.
- Mudancas de estado devem ocorrer por metodos de negocio, nao por setters livres.
- Requests devem ser imutaveis quando possivel.
- Valores financeiros devem usar `BigDecimal`.
- Nomes de codigo devem estar em ingles.
- Mensagens de negocio e documentacao devem estar em portugues.
- Enums devem centralizar codigos de dominio quando isso reduzir strings soltas e aumentar rastreabilidade.

## Precificacao

- Usar Strategy Pattern para regras de precificacao.
- As estrategias representam variacoes de risco por tipo de recebivel.
- O resultado da precificacao exposto pela API deve seguir nomenclatura de mercado com request/response.

## Validacao e excecoes

- Usar mensagens centralizadas quando isso reduzir duplicacao e melhorar consistencia.
- Usar exceptions customizadas para regras de dominio e validacoes de negocio.
- Preservar classificacao por tipo de exception para facilitar observabilidade, sustentacao e metricas.
- Evitar transformar todos os erros em um unico tipo generico sem significado operacional.
- Preferir subclasses ou hierarquias de exception quando a classificacao por tipo ajudar operacao e monitoramento.
- Usar utilitarios de validacao como facilitadores, sem injecao obrigatoria e sem esconder a regra de negocio.

## API

- API First quando a definicao de contrato orientar a implementacao.
- API RESTful.
- Contratos claros.
- OpenAPI/Swagger.
- Tratamento global de excecoes.
- Responses padronizadas para erros.
- Controllers devem permanecer finos, delegando orquestracao para `ApplicationService`.

## Frontend

- Stack definida: Angular.
- Escopo inicial deve ser pequeno e aderente ao desafio.
- O frontend deve cobrir painel do operador, simulacao e grid historico com filtros.

## Observabilidade e resiliencia

- Incluir logs estruturados.
- Incluir metricas.
- Considerar tracing se couber no prazo.
- Chamadas externas futuras devem prever retry ou circuit breaker.
- `correlationId` e `transactionId` devem ser carregados automaticamente no contexto da request quando possivel.
- `transactionId` representa o fluxo de request ou a trilha de negocio que precisa ser correlacionada entre componentes.
- O payload do log deve permanecer enxuto e evitar repeticao do que o pattern do logger ja imprime.
- `step` deve ser o campo de fluxo mais importante para observabilidade de negocio e pode variar por processo.
- Para entidades e payloads grandes, a utility deve suportar inclusao seletiva e exclusao seletiva de campos sem logar o objeto inteiro por padrao.

## Git e entrega

- Branches no padrao `feature/story00x-...`.
- Commits pequenos, rastreaveis e em portugues.
- Usar rebase interativo quando fizer sentido para organizar commits antes do merge.
- Usar squash merge na PR.
- Marcar a entrega final com tag SemVer.
