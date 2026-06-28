# Architecture Decisions

## Estilo geral

- A solucao segue um **monolito modular**.
- A separacao principal e entre Presentation, Application, Domain e Infrastructure.
- O dominio nao deve depender de detalhes de infraestrutura.
- O backend deve privilegiar leitura direta do codigo e das regras do negocio em vez de criar camadas abstratas sem retorno pratico.
- Relatorios e consultas analiticas podem acessar a persistencia de forma mais direta quando isso simplificar a leitura, a performance e a manutencao.
- Nao criar microsservicos, eventos ou mensageria sem um problema real que justifique o custo operacional.

## Backend

- Java 21 e Spring Boot.
- A stack foi escolhida por tipagem forte, maturidade do ecossistema e boa adequacao a um contexto financeiro.
- PostgreSQL e o banco relacional principal.
- Flyway e usado para versionamento de schema.
- Gradle e a ferramenta de build.
- Testcontainers e preferivel em testes de integracao que dependam de comportamento real do PostgreSQL.
- H2 so deve ser usado quando a diferenca de dialeto e comportamento transacional nao comprometer o teste.
- A camada de aplicacao deve usar `ApplicationService` como nomenclatura padrao.
- Controllers devem ser finos e delegar a orquestracao para a camada de aplicacao.
- Integracoes externas devem ficar na infraestrutura e entrar no dominio por contratos pequenos e claros.

## Dominio

- Preferir modelos ricos quando o comportamento justificar.
- Entidades devem proteger invariantes e expor mudancas de estado por metodos de negocio, nao por setters livres.
- Requests e comandos devem ser imutaveis sempre que possivel.
- Valores financeiros devem usar `BigDecimal`.
- Nomes tecnicos devem ficar em ingles.
- Mensagens de negocio, documentacao e apoio operacional devem ficar em portugues.
- Enums devem centralizar codigos de dominio quando isso reduzir strings soltas e melhorar rastreabilidade.
- Factories podem ser usadas quando houver atributos obrigatorios ou regras de criacao relevantes.
- Evitar abstracoes no dominio que apenas duplicam a estrutura do banco sem adicionar comportamento.

## Precificacao

- A regra de precificacao usa **Strategy Pattern**.
- Cada estrategia representa uma variacao de risco por tipo de recebivel.
- O spread base de cada tipo deve ficar fixado na estrategia ou em configuracao de dominio coerente com o desenho do desafio.
- A formula base deve ser mantida explicita e simples.
- Para operacoes cross-currency, calcular primeiro a precificacao e converter no final pela taxa vigente.
- A API deve expor request/response com nomenclatura clara e compreensivel para o avaliador.

## Currency Engine

- O motor de cambio deve tratar a taxa como dado operacional de consulta.
- O fluxo preferencial e: cache operacional -> fonte externa -> fallback de contingencia.
- O fallback deve ser explicitamente documentado como base manual/mock, nao como persistencia operacional principal.
- Se a integracao externa falhar, o comportamento deve continuar claro e previsivel.
- Se cache e fallback falharem, a aplicacao deve falhar de forma controlada e explicita.
- Nao persistir dados operacionais de cache como se fossem a unica fonte historica, a menos que isso esteja claramente documentado.

## Persistencia e integridade

- Transacoes financeiras devem respeitar ACID.
- Nenhuma liquidacao deve ficar pela metade.
- Usar controle de concorrencia quando a operacao puder ser executada simultaneamente por mais de um fluxo.
- Preferir optimistic locking quando ele resolver o problema sem adicionar complexidade desnecessaria.
- O desenho do schema deve refletir o dominio e nao apenas espelhar telas.
- Seeds e dados de referencia devem ser separados de mudancas estruturais de schema.

## API

- API First quando a definicao de contrato orientar a implementacao.
- RESTful com verbos HTTP corretos e codigos de status semanticos.
- Contratos devem ser claros e estaveis.
- OpenAPI/Swagger deve refletir o que realmente esta exposto.
- Erros devem ter tratamento global e respostas padronizadas.
- A classificacao do erro deve ser util para operacao, observabilidade e manutencao.
- Nao transformar todos os erros em uma unica resposta generica quando isso prejudicar a leitura operacional.

## Frontend

- A stack definida e Angular.
- O escopo do frontend deve ser pequeno e aderente ao desafio.
- A interface deve cobrir o painel do operador, simulacao e grid historico com filtros.
- O layout deve priorizar legibilidade, alinhamento e hierarquia visual.
- Evitar controles que nao entreguem valor ao fluxo principal.
- Mensagens para o usuario final devem ser amigaveis e consistentes com o estado real da operacao.

## Observabilidade e resiliencia

- Incluir logs estruturados.
- Os logs devem carregar correlationId e transactionId quando possivel.
- `step` deve ser o campo principal para observabilidade de fluxo.
- O payload do log deve ser enxuto e evitar repeticao do que o pattern ja entrega.
- Chamadas externas devem prever timeout, retry e circuit breaker quando isso fizer sentido para o prazo e para o risco.
- Resiliencia nao deve virar mascaramento de erro: o sistema precisa continuar previsivel mesmo quando o fallback for acionado.
- Quando um fallback for aplicado, isso deve ficar claro em log e, se necessario, em documentacao.
- Metrics e tracing sao diferenciais, mas nao devem ser simulados de forma artificial se nao houver suporte real no projeto.

## Validacao e testes

- Alteracoes que impactem contratos publicos, persistencia, seguranca ou integracoes devem vir com testes relevantes.
- Cobrir o comportamento mais importante da story, nao a superficie inteira sem necessidade.
- Se a mudanca tocar frontend, validar build do frontend.
- Se a mudanca tocar backend, validar `spotlessApply` e `clean check`.
- Se uma regressao aparecer, corrigir a causa mais local possivel antes de ampliar o alcance da mudanca.

## Git e entrega

- Branches no padrao `feature/story00x-...`.
- Commits pequenos, rastreaveis e em portugues.
- Usar rebase interativo quando fizer sentido para organizar commits antes do merge.
- Usar squash merge na PR.
- A documentacao final deve refletir o estado real da entrega.
- Marcar a entrega final com tag SemVer.
- Prompts, decisoes e tradeoffs relevantes devem ser registrados em `docs/ai/`.

## Limites de desenho

- Nao criar novas camadas por reflexo.
- Nao introduzir mensageria, cache distribuido, microservicos ou observabilidade externa sem justificativa real.
- Nao prometer resiliencia ou seguranca que nao esteja efetivamente implementada.
- Nao substituir simplicidade por abstracoes de baixo retorno apenas para parecer mais sofisticado.
- Se houver divergencia entre arquitetura ideal e prazo real, documentar explicitamente o tradeoff adotado.
