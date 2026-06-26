# Agent Memory & Guidelines

## Context
- Projeto: desafio técnico SRM Credit Engine.
- Objetivo: entregar backend modular, documentação forte e frontend mínimo coerente.
- Regras já definidas pelo usuário têm prioridade sobre qualquer padrão genérico.

## O que manter em mente
- Use nomes em inglês para código, classes, enums, métodos e pacotes.
- Mensagens de negócio e documentação do projeto ficam em português.
- Preserve o que já foi decidido; não reabra escopo fechado sem pedido explícito.
- Evite overengineering. É um desafio técnico, não uma plataforma completa.
- Quando uma decisão já foi fechada, trate-a como memória de sessão e não reavalie sem motivo novo.

## Git e fluxo
- Antes de iniciar uma story nova, criar a branch no padrão `feature/story00x-...` a partir do estado que o usuário indicar.
- Mensagens de commit ficam em português e sem prefixo de story.
- Segmentar commits quando fizer sentido.
- Não insistir em `git diff` ou `git log` a cada passo curto.
- Só fechar commit/PR quando a story estiver realmente pronta.

## Qualidade
- Após alterar ou criar código, rodar `spotlessApply` antes de `check`.
- Depois rodar `clean check`.
- Se algo quebrar no build, corrigir antes de seguir.
- Manter os testes focados no comportamento relevante da story.

## Arquitetura
- Backend em camadas, com domínio protegido de infraestrutura.
- Preferir domínio rico, factories e mudanças de estado por método.
- Validar com exceptions customizadas e mensagens centralizadas quando isso melhorar legibilidade.
- Em regras novas, reaproveitar o padrão já adotado sempre que fizer sentido.

## Documentação
- Atualizar README quando a story mudar de estado.
- Guardar decisões relevantes para o `AI_USAGE.md`.
- Para a entrega final, revisar tudo o que foi feito e consolidar a documentação.

## Frontend
- A stack do frontend foi definida como Angular.
- Manter o escopo pequeno e aderente ao desafio.

# ENVIROMENT & TERMINAL EXECUTION (WINDOWS / POWERSHELL)
- O terminal padrão é o Windows PowerShell. Nunca tente aninhar chamadas do powershell.exe chamando outra string de comando com aspas duplas internas (evite problemas de quoting/escape de caracteres).
- Para executar comandos do Gradle com variáveis de ambiente temporárias no PowerShell, use estritamente a sintaxe direta em uma única linha separada por ponto e vírgula, exatamente assim:
  `$env:JAVA_HOME='D:\_DEV\JDK\jdk-21.0.1'; $env:PATH="$env:JAVA_HOME\bin;$env:PATH"; .\gradlew.bat check`
- Aplique exatamente a mesma regra de sintaxe de linha única acima para executar o comando `.\gradlew.bat spotlessApply`.
- Nunca use aspas triplas ou sequências de escape complexas (`\""`) que causem falhas de interpretador no PowerShell.
- Não imprima no chat a própria linha de comando quando ela já foi executada com sucesso; reporte apenas o resultado relevante.
- Não repita saída de comandos já validada quando o único objetivo for confirmar sucesso; use isso só como memória de sessão.

# FILE DISCOVERY & MEMORY OPTIMIZATION
- Proibido executar `Get-ChildItem -Recurse` ou comandos de varredura profunda no terminal a cada passo. Isso gera desperdício massivo de tokens.
- No primeiro passo da tarefa, execute um comando único para gerar a árvore de arquivos e guarde-a em sua memória de contexto de sessão: `Get-ChildItem -Recurse -Name` ou leia diretamente a estrutura de pacotes do projeto.
- Use a memória da sessão atual para rastrear os locais dos arquivos que você mesmo criou ou alterou. Não re-escaneie o disco se você já sabe onde o arquivo foi gerado.
- Se precisar localizar um arquivo específico e souber o nome dele, busque de forma cirúrgica pelo nome exato em vez de listar o projeto inteiro recursivamente.
- Se um diretório já foi inspecionado nesta sessão, prefira lembrar o caminho e reutilizá-lo em vez de consultar de novo.
- Não reabra leitura de arquivos já inspecionados na sessão sem mudança real de conteúdo ou novo objetivo.

# OUTPUT LOGGING & VERBOSITY (SILENT MODE)
- Opere em modo silencioso (Silent Mode). É proibido narrar pequenos passos, pensamentos intermediários ou motivos de falhas menores no chat.
- Execute os loops de comandos, correções do PowerShell e Gradle em segundo plano de forma contínua.
- Forneça uma resposta no chat apenas quando a subtarefa for totalmente concluída ou se encontrar um erro crítico de bloqueio que exija intervenção humana.
- Em status intermediário, responda só com o que mudou de forma objetiva, sem explicar passos óbvios.
- A resposta final deve conter apenas o diagnóstico fechado, cobertura e pontos de atenção.
