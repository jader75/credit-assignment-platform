# Agent Guidelines

## Escopo
- Este arquivo define como o agente deve trabalhar neste projeto.
- Regras de negócio, decisões arquiteturais e padrões de código ficam em `docs/ai/`.
- As diretivas do usuário têm prioridade sobre instruções genéricas.

## Fluxo de trabalho
- Antes de iniciar uma story nova, criar a branch no padrão `feature/story00x-...` a partir do estado indicado pelo usuário.
- Não atualizar a `main` local automaticamente; o usuário faz isso manualmente quando necessário.
- Segmentar commits quando fizer sentido.
- Mensagens de commit ficam em português e sem prefixo de story.
- Quando o usuário pedir insumos de commit, informar commits sugeridos, arquivos e mensagens.
- Quando o usuário pedir título e descrição de PR, usar a branch atual e entregar Markdown copiável.
- Só fechar commit ou PR quando a story estiver pronta.

## Qualidade
- Após alterar ou criar código, rodar `spotlessApply` antes de `check`.
- Depois rodar `clean check`.
- Se o build quebrar, corrigir antes de seguir.
- Manter testes focados no comportamento relevante da story.

## Terminal e PowerShell
- O terminal padrão é Windows PowerShell.
- Não aninhar chamadas de `powershell.exe` com strings complexas.
- Para Gradle com `JAVA_HOME`, usar uma linha direta:
  `$env:JAVA_HOME='D:\_DEV\JDK\jdk-21.0.1'; $env:PATH="$env:JAVA_HOME\bin;$env:PATH"; .\gradlew.bat check`
- Usar a mesma forma para `spotlessApply`.
- Não imprimir no chat linhas de comando já executadas com sucesso; reportar só o resultado relevante.

## Leitura e economia de contexto
- Preferir `rg` e `rg --files` para localizar arquivos e texto.
- Evitar varreduras profundas repetidas.
- Reutilizar caminhos e decisões já identificados na sessão.
- Não reler arquivos sem mudança real de conteúdo ou novo objetivo.

## Comunicação
- Ser direto, técnico e objetivo.
- Enviar atualizações intermediárias apenas quando forem úteis.
- Na resposta final, resumir resultado, validação e pontos de atenção.
