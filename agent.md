# Agent Guidelines

## Escopo
- Este arquivo define como o agente deve trabalhar neste projeto.
- Regras de negocio, decisoes arquiteturais e padroes de codigo ficam em `docs/ai/`.
- As diretivas do usuario tem prioridade sobre instrucoes genericas.

## Fluxo de trabalho
- Antes de iniciar uma story nova, criar a branch no padrao `feature/story00x-...` a partir do estado indicado pelo usuario.
- Nao atualizar a `main` local automaticamente; o usuario faz isso manualmente quando necessario.
- Segmentar commits quando fizer sentido.
- Mensagens de commit ficam em portugues e sem prefixo de story.
- Quando o usuario pedir insumos de commit, informar commits sugeridos, arquivos e mensagens.
- Quando o usuario pedir titulo e descricao de PR, usar a branch atual e entregar Markdown copiavel.
- So fechar commit ou PR quando a story estiver pronta.

## Qualidade
- Apos alterar ou criar codigo, rodar `spotlessApply` antes de `check`.
- Depois rodar `clean check`.
- Se o build quebrar, corrigir antes de seguir.
- Manter testes focados no comportamento relevante da story.
- Se um comando falhar, isolar a causa com o menor reproduzir possivel antes de repetir um build amplo.
- Nao disparar `test`, `check` ou `clean check` em paralelo contra o mesmo diretorio `build`.
- Sempre executar `spotlessApply` antes de `clean check`, inclusive apos correcoes pequenas de linha ou formatacao.

## Terminal e PowerShell
- O terminal padrao e Windows PowerShell.
- Nao aninhar chamadas de `powershell.exe` com strings complexas.
- Para Gradle com `JAVA_HOME`, usar uma linha direta:
  `$env:JAVA_HOME='D:\_DEV\JDK\jdk-21.0.1'; $env:PATH="$env:JAVA_HOME\bin;$env:PATH"; .\gradlew.bat check`
- Usar a mesma forma para `spotlessApply`.
- Ao montar strings com linha e indice, nao usar interpolacao direta como `"$i:$($_)"`; prefira `'{0}:{1}' -f $i, $_` ou `"$($i):$($_)"`.
- Se um comando falhar por parsing do PowerShell, reescrever no formato mais simples possivel antes de tentar outra variacao.
- Nao imprimir no chat linhas de comando ja executadas com sucesso; reportar so o resultado relevante.

## Leitura e economia de contexto
- Preferir `rg` e `rg --files` para localizar arquivos e texto.
- Evitar varreduras profundas repetidas.
- Reutilizar caminhos e decisoes ja identificados na sessao.
- Nao reler arquivos sem mudanca real de conteudo ou novo objetivo.

## Comunicacao
- Ser direto, tecnico e objetivo.
- Enviar atualizacoes intermediarias apenas quando forem uteis.
- Na resposta final, resumir resultado, validacao e pontos de atencao.

## Documentacao e entregas
- Manter a documentacao coerente com o estado real do codigo.
- Se um artefato de documentacao ja existir, atualizar o existente antes de criar um novo.
- Para PR, entregar sempre em Markdown copiavel quando o usuario pedir titulo, resumo ou descricao.
- Quando houver backlog ou historias, respeitar a ordem definida pelo usuario antes de expandir escopo.
- Se houver diagramas, README, scripts ou arquivos de apoio, manter a versao documental alinhada ao que esta realmente implementado.

## Modelagem e contratos
- Antes de alterar schema, contrato de API ou modelo de dados, revisar o estado atual do banco, migrations e artefatos de documentacao.
- Diferenciar claramente seed, dados de referencia e mudanca estrutural de schema.
- Quando houver divergencia entre documento e implementacao, corrigir o artefato que estiver defasado.
- Em mudancas de modelo, preferir evolucao versionada a sobrescrever historico sem registro.
- Se um novo diagrama ou arquivo documental for criado, indicar explicitamente qual versao ele representa.

## Backend e testes
- Controllers devem permanecer finos e delegar orquestracao para a camada de aplicacao.
- Priorizar tipos de erro e exceptions especificas quando isso melhorar rastreabilidade.
- Ao tocar contratos publicos, revisar os testes afetados e ampliar cobertura do comportamento relevante.
- Evitar refatoracao ampla quando um ajuste localizado resolve o problema com menos risco.
- Se uma alteracao mexer em persistencia, integracao externa ou seguranca, validar com testes de integracao ou build completo quando possivel.

## Frontend
- Preservar o padrao visual ja adotado no projeto.
- Evitar inflar a interface com controles que nao entreguem valor real ao fluxo principal.
- Ajustes de layout devem manter legibilidade, hierarquia visual e alinhamento entre rotulos e valores.
- Depois de alterar telas, validar build do frontend e revisar mensagens de erro expostas ao usuario.

## Seguranca e integracao
- Preferir a menor superficie de alteracao necessaria para entregar valor de seguranca.
- Integracoes externas devem deixar claro o fluxo principal, retry, timeout, fallback e limites do que foi implementado.
- Sempre diferenciar comportamento ativo de melhoria futura nao implementada.
- Para autenticacao, autorizacao e rastreabilidade, manter a implementacao simples, mas coerente com operacao real.
- Em integracoes de rede, explicitar o comportamento quando a fonte primaria falhar e quando o fallback tambem nao estiver disponivel.

## IA e prompts
- Usar IA como apoio, nao como substituto de entendimento.
- Quando o usuario pedir prompts reutilizaveis, produzir texto copiavel e com formato padronizado.
- Quando solicitado, registrar prompts, tradeoffs e limitacoes em documentos especificos do projeto.
- Preferir instrucoes curtas, reutilizaveis e descritivas em vez de textos longos e abstratos.
- Se um fluxo puder ser reaproveitado em outros projetos, documentar a versao generica e nao apenas a variante local.
