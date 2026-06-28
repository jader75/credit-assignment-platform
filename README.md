# credit-assignment-platform
Plataforma em Java 21 para apoiar a cessao, a precificacao e a consulta analitica de direitos crediticios multimoedas. A base atual possui backend Spring Boot, PostgreSQL com Flyway, motor de precificacao, APIs REST e automacoes de qualidade.

## O que existe no projeto

- `build.gradle`: "build" principal com `java`, `spring-boot`, `jacoco` e `spotless`
- `settings.gradle`: nome do projeto como `credit-assignment-platform`
- `gradlew` e `gradlew.bat`: wrapper do Gradle para execucao local e no hook
- `.githooks/pre-commit.ps1`: executa `./gradlew.bat spotlessApply` antes do commit
- `.githooks/pre-push.ps1`: executa `./gradlew.bat clean check` antes do push
- `docs/ai`: regras do desafio, decisões arquiteturais e padrões de código usados com apoio de IA
- `docs/adr`: registro das decisoes arquiteturais do projeto
- `docs/db/schema.sql`: schema de referência da base de dados
- `docs/db/data.sql`: seed e dados de referencia para testes
- `backend/credit-engine`: modulo inicial do backend com a aplicacao Spring Boot
- `AI_USAGE.md`: registro critico do uso de IA na entrega

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

## Fundamentacao tecnica da stack

O `build.gradle` deixa a escolha tecnica coerente com o problema proposto e com o prazo de entrega:

- **Java 21**: LTS, tipagem forte e features modernas com bom encaixe para codigo financeiro que pede previsibilidade.
- **Spring Boot**: ecossistema maduro para REST, validacao, seguranca, health checks e configuracao padronizada.
- **Spring Data JPA + PostgreSQL**: persistencia relacional com suporte a ACID, transacao e modelagem consistente do dominio.
- **Flyway**: controle de evolucao de schema com versionamento rastreavel.
- **Redis**: cache operacional para taxa de cambio por par de moeda.
- **Spring Security**: autenticacao e protecao basica dos endpoints.
- **Validation**: regras de entrada e protecao contra payload invalido.
- **Actuator + Micrometer + Prometheus**: observabilidade minima para operacao e debug.
- **springdoc-openapi**: documentacao da API a partir do contrato exposto.
- **Testcontainers**: testes mais proximos do ambiente real, principalmente para banco.
- **ArchUnit**: reforco das fronteiras arquiteturais.
- **Spotless + Palantir Java Format**: consistencia de formato.
- **JaCoCo**: medicao de cobertura como sinal de qualidade, nao como fim em si.

## Estrutura atual

```text
.
├── backend/
│   └── credit-engine/
├── docs/
│   ├── ai/
│   ├── adr/
│   │   ├── 0001-...0008.md
│   ├── api/
│   ├── c4/
│   │   ├── README_v1.md
│   │   └── README_v2.md
│   └── db/
│       ├── batch-import-tests/
│       ├── diagrams/
│       │   ├── v1/
│       │   └── v2/
│       ├── data.sql
│       └── schema.sql
├── frontend/
├── scripts/
├── .githooks/
├── AI_USAGE.md
├── build.gradle
├── docker-compose.yml
├── gradle/
├── gradlew
├── gradlew.bat
├── settings.gradle
├── agent.md
├── LICENSE
└── README.md
```

Os diretórios acima representam a estrutura funcional da entrega. Outros arquivos de suporte existem no repositório, mas foram omitidos da arvore para manter a leitura objetiva.

## Banco de dados

O arquivo `backend/credit-engine/src/main/resources/db/migration/V1__create_base_schema.sql` define o schema inicial da solucao:

- `currencies`
- `exchange_rates`
- `receivable_types`
- `assignors`
- `credit_batches`
- `credit_assignments`

Tambem ha indices para historico cambial, lote e consulta de liquidações.

O `docker-compose.yml` sobe PostgreSQL e Redis. O schema fica a cargo do Flyway quando a aplicacao inicia.

A documentacao visual da modelagem esta em [docs/db/diagrams/v1/README.md](docs/db/diagrams/v1/README.md) e o arquivo editavel do diagrama esta em [docs/db/diagrams/v1/credit-domain.drawio](docs/db/diagrams/v1/credit-domain.drawio).

O estado de apoio e evolucao da modelagem fica documentado em [docs/db/diagrams/v2/README.md](docs/db/diagrams/v2/README.md).

## Modelo do dominio

- `credit_batches` representa o lote recebido pela plataforma
- `credit_assignments` representa cada ítem do lote, com o snapshot de precificacao
- `receivable_types` define o tipo do ativo e a regra base de "spread"
- `exchange_rates` guarda o historico de câmbio com origem da taxa
- o fluxo operacional de cotação usa Redis por par de moeda; a chave segue o formato `credit:exchange-rate:FROM:TO`
- o banco fica como fallback de contingência apenas para taxas manuais/mockadas
- `assignors` guarda o cedente e a sua classificação atual

## Qualidade

O commit e o push estao ligados a gates de qualidade via `.githooks/pre-commit.ps1` e `.githooks/pre-push.ps1`.

O `pre-commit` executa:

```powershell
./gradlew.bat spotlessApply
```

O `pre-push` executa:

```powershell
./gradlew.bat clean check
```

Se a formatacao falhar, o commit e bloqueado. Se o `check` falhar, o push e bloqueado.

## API

A documentacao da simulacao de precificacao esta em [docs/api/pricing-simulation.md](docs/api/pricing-simulation.md).
A documentacao do extrato de liquidacao esta em [docs/api/settlement-statement.md](docs/api/settlement-statement.md).

## Diretrizes de IA e arquitetura

As regras consolidadas do desafio e as decisoes usadas para orientar o desenvolvimento estao em:

- [docs/ai/project-rules.md](docs/ai/project-rules.md)
- [docs/ai/architecture.md](docs/ai/architecture.md)
- [docs/ai/coding-standards.md](docs/ai/coding-standards.md)
- [docs/adr/README.md](docs/adr/README.md)
- [AI_USAGE.md](AI_USAGE.md)

O `agent.md` fica reservado para instrucoes operacionais do agente.

## Como validar localmente

```powershell
docker compose up -d
.\gradlew.bat spotlessApply
.\gradlew.bat bootRun
.\gradlew.bat clean check
```

Para subir tudo de uma vez e aguardar os health checks:

```powershell
.\scripts\start-dev.ps1
```

O script `start-dev.ps1` sobe PostgreSQL e Redis, aguarda os health checks dos dois e então inicia backend e frontend.

Frontend:

```powershell
cd frontend
npm install
npm start
```

Se o `npm start` falhar por ambiente local incompleto, use o script acima ou confirme que a instalação do Node está disponível na sessão atual. O projeto já chama o CLI local do Angular, então não deve exigir ajuste manual de PATH depois do `npm install`.

Fluxo manual equivalente ao hook:

```powershell
.\gradlew.bat spotlessApply
.\gradlew.bat clean check
git add .
git commit
git push
```

O teste de integracao sobe um PostgreSQL via Docker com Testcontainers e valida a inicializacao do contexto da aplicacao.

### Trust store para a integração de câmbio

A consulta externa de câmbio usa TLS validado pelo Java. Em algumas máquinas o JDK padrão pode não confiar no certificado raiz usado pelo endpoint da Frankfurter. Se isso acontecer, a aplicação vai cair no fallback local apesar de a URL estar correta.

Regras práticas de setup:

- Windows: o backend já tenta usar o trust store nativo `Windows-ROOT`.
- Linux: use o trust store padrão da JVM ou importe o certificado raiz da sua distro/JVM.
- JVM customizada: se o certificado não for confiável, importe a CA no `cacerts` ou aponte a JVM para um trust store compatível.

Exemplo de importação manual em um trust store JKS:

```powershell
keytool -importcert -alias frankfurter-ca -file frankfurter-ca.crt -keystore truststore.jks
```

Se precisar apontar a JVM para o trust store:

```powershell
$env:JAVA_TOOL_OPTIONS='-Djavax.net.ssl.trustStore=C:\caminho\truststore.jks -Djavax.net.ssl.trustStorePassword=senha'
```

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
- **Story 004** - Modelagem do domínio
  - lote de recebíveis
  - ítem de cessão/liquidação
  - câmbio
  - tipos de recebível
  - cedente
- **Story 005** - Motor de precificação
  - `Strategy Pattern`
  - cálculo de valor presente
  - "spread" por tipo de recebível
  - conversão cross-currency
- **Story 006** - Automação de qualidade e CI/CD
  - pipeline de validação
  - execução automatizada de testes e lint
  - governança de entrega

- **Story 007** - Refatoração e padronização do domínio
  - centralização de enums
  - centralização de mensagens
  - exceptions customizadas
  - factories e enriquecimento do domínio

- **Story 008** - API de operação
  - endpoints REST
  - simulação de liquidação
  - documentação OpenAPI

- **Story 009** - Extrato e consultas analíticas
  - listagem de liquidações
  - filtros por período, cedente e moeda
  - SQL otimizado para relatórios
- **Story 010** - Documentação de contexto e diretrizes de IA
  - `AI_USAGE.md`
  - regras do desafio
  - diretrizes operacionais do agente
- **Story 011** - Observabilidade e resiliência
  - logs estruturados
  - tratamento de exceções
  - métricas e monitoramento
  - concorrência com optimistic locking
- **Story 012** - Frontend do operador
  - formulário de simulação
  - grid de transações
  - paginação server-side

- **Story 013** - Varredura geral de gaps, ajustes e validação final
  - revisão de requisitos, regras e escopo
  - identificação de gaps, fixes e inconsistências
  - ajustes de qualidade e refatoração pontual
  - cobertura, testes e validação final
  - importação de lotes por arquivo para alimentar `credit_batches` e `credit_assignments`
  - mesa operacional para liquidacao e alteracao de status de recebiveis

- **Story 014** - Resiliência e segurança básica
  - integração com Frankfurter para consulta de câmbio
  - Redis como cache operacional por par de moeda
  - fallback em base manual/mock
  - retry, circuit breaker e timeout curto no cliente HTTP
  - JWT basico no backend
  - rastreabilidade de usuario e logs de auditoria basica

### Fechamento da entrega

- **Story 015** - Documentação e entrega final
  - `AI_USAGE.md`
  - diagrama C4
  - diagrama ER
  - SQL DDL final
  - ajustes finais de cobertura e gaps de teste
  - consolidacao do README e do estado final dos artefatos
  - validacao final concluida
  - tag da versao entregue: pendente de solicitacao explicita
