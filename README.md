# credit-assignment-platform
Plataforma em Java 21 para apoiar a cessão e a precificação de direitos creditórios. Neste momento o repositório contém a base de build, a estrutura inicial do backend, scripts de banco e o hook de qualidade do Git.

## O que existe no projeto

- `build.gradle`: build principal com `java`, `jacoco` e `spotless`
- `settings.gradle`: nome do projeto como `credit-assignment-platform`
- `gradlew` e `gradlew.bat`: wrapper do Gradle para execução local e no hook
- `.githooks/pre-push.ps1`: executa `./gradlew.bat clean check` antes do push
- `docs/db/schema.sql`: esquema inicial do banco
- `docs/db/data.sql`: massa de dados inicial, ainda vazia
- `backend/credit-engine`: módulo inicial do backend

## Tecnologias configuradas

- Java 21
- Gradle
- JUnit 5
- ArchUnit
- JaCoCo
- Spotless com `palantirJavaFormat`

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

O arquivo `docs/db/schema.sql` define as tabelas iniciais da solução:

- `currencies`
- `currency_rates`
- `product_types`
- `assignors`
- `credit_assignments`

Também há índices para relatórios e consulta de câmbio mais recente.

## Qualidade

O push está ligado a um gate de qualidade via `.githooks/pre-push.ps1`.

O script executa:

```powershell
./gradlew.bat clean check
```

Se o `check` falhar, o push é bloqueado.

## Como validar localmente

```powershell
.\gradlew.bat clean check
```

Esse comando roda testes, verificação de formatação e geração do relatório do JaCoCo conforme a configuração atual do projeto.
