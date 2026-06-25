# credit-assignment-platform
Plataforma em Java 21 para apoiar a cessao e a precificacao de direitos crediticios. A base atual da Story 003 deixa a aplicacao Spring Boot pronta para subir com PostgreSQL em Docker e aplicar o schema via Flyway.

## O que existe no projeto

- `build.gradle`: build principal com `java`, `spring-boot`, `jacoco` e `spotless`
- `settings.gradle`: nome do projeto como `credit-assignment-platform`
- `gradlew` e `gradlew.bat`: wrapper do Gradle para execucao local e no hook
- `.githooks/pre-push.ps1`: executa `./gradlew.bat clean check` antes do push
- `docs/db/schema.sql`: schema de referencia da base de dados
- `docs/db/data.sql`: massa inicial ainda vazia
- `backend/credit-engine`: modulo inicial do backend com a aplicacao Spring Boot

## Tecnologias configuradas

- Java 21
- Gradle
- Spring Boot
- PostgreSQL
- Flyway
- JUnit 5
- ArchUnit
- JaCoCo
- Spotless com `palantirJavaFormat`
- Testcontainers

## Estrutura atual

```text
.
├── backend/
│   └── credit-engine/
├── docs/
│   └── db/
├── frontend/
├── .githooks/
├── build.gradle
├── settings.gradle
└── gradlew.bat
```

## Banco de dados

O arquivo `backend/credit-engine/src/main/resources/db/migration/V1__create_base_schema.sql` define o schema inicial da solucao:

- `currencies`
- `currency_rates`
- `product_types`
- `assignors`
- `credit_assignments`

Tambem ha indices para relatorios e consulta de cambio mais recente.

O `docker-compose.yml` sobe apenas o PostgreSQL. O schema fica a cargo do Flyway quando a aplicacao inicia.

## Qualidade

O push esta ligado a um gate de qualidade via `.githooks/pre-push.ps1`.

O script executa:

```powershell
./gradlew.bat clean check
```

Se o `check` falhar, o push e bloqueado.

## Como validar localmente

```powershell
docker compose up -d
.\gradlew.bat bootRun
.\gradlew.bat clean check
```

O teste de integracao sobe um PostgreSQL via Docker com Testcontainers e valida a inicializacao do contexto da aplicacao.

## Backlog do desafio

### Concluido

- **Story 001** - Estrutura inicial da plataforma
  - base do repositório
  - organização inicial do projeto
- **Story 002** - Base de qualidade e automação
  - `Spotless`
  - `Palantir Java Format`
  - `JaCoCo`
  - `ArchUnit`
  - `Gradle Wrapper`
  - estrutura dos hooks
- **Story 003** - Infraestrutura Spring Boot
  - aplicação Spring Boot
  - PostgreSQL em Docker
  - `application.yml`
  - Flyway
  - validação de conexão com Docker

### Próximas stories

- **Story 004** - Modelagem do domínio
  - `Currency`
  - `CurrencyRate`
  - `ProductType`
  - `Assignor`
  - `CreditAssignment`
- **Story 005** - Motor de precificação
  - `Strategy Pattern`
  - cálculo de valor presente
  - spread por tipo de recebível
  - conversão cross-currency
- **Story 006** - API de operação
  - endpoints REST
  - simulação de liquidação
  - documentação OpenAPI
- **Story 007** - Extrato e consultas analíticas
  - listagem de liquidações
  - filtros por período, cedente e moeda
  - SQL otimizado para relatórios
- **Story 008** - Observabilidade e resiliência
  - logs estruturados
  - métricas
  - tratamento de erro
  - concorrência com optimistic locking
- **Story 009** - Frontend do operador
  - formulário de simulação
  - grid de transações
  - paginação server-side
- **Story 010** - Documentação e entrega
  - `AI_USAGE.md`
  - diagrama C4
  - diagrama ER
  - SQL DDL final
  - tag da versão entregue
