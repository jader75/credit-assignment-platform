# Coding Standards

## Nomenclatura

- Codigo, classes, metodos, pacotes, enums e propriedades tecnicas em ingles.
- Mensagens de erro, documentacao e texto de apoio em portugues.
- Evitar mistura de portugues e ingles no mesmo identificador.
- Preferir nomes explicitos a abreviacoes pouco claras.
- Preferir sufixos reconheciveis no mercado e no projeto, como `Request`, `Response`, `ApplicationService`, `Repository` e `Controller`.

## Design

- Aplicar SOLID, DRY e KISS.
- Evitar overengineering para manter o desafio entregavel.
- Criar abstracao somente quando reduzir complexidade real, duplicacao relevante ou acoplamento.
- Preferir padroes ja existentes no projeto antes de criar um novo.

## Dominio

- Entidades devem proteger suas invariantes.
- Evitar construcao incompleta com `new` seguido de varios setters.
- Preferir factories ou metodos de criacao quando houver atributos obrigatorios.
- Mudancas de estado devem ser feitas por metodos de negocio.
- Usar objetos imutaveis para requests e dados de entrada quando possivel.
- Preservar records quando eles representarem bem dados imutaveis e nao prejudicarem validacao ou legibilidade.

## Validacoes

- Reaproveitar utilitarios de validacao do dominio quando existirem.
- Centralizar mensagens recorrentes.
- Usar exceptions especificas para manter rastreabilidade.
- Evitar `NullPointerException` como mecanismo de regra de negocio.
- Mensagens soltas devem ser evitadas quando forem mensagens de dominio, validacao ou erro exposto.
- `Objects.requireNonNull` pode ser usado para invariantes internas simples, mas validacoes de negocio devem lancar exceptions de negocio.

## Testes

- Testar comportamento relevante da story.
- Ampliar cobertura quando o codigo tocar contratos publicos, regras financeiras, persistencia ou tratamento de excecoes.
- Usar Testcontainers para validar comportamento dependente de PostgreSQL.
- Usar H2 apenas quando a diferenca de dialeto nao afetar o comportamento testado.

## Qualidade automatizada

- Rodar `spotlessApply` apos alterar ou criar codigo.
- Rodar `clean check` antes de fechar story.
- CI/CD deve executar build, testes e lint.
- Hooks locais devem reduzir falhas triviais antes do push.

## Documentacao

- Atualizar o README quando o estado da story mudar.
- Manter documentos especificos em `docs/`.
- Registrar decisoes relevantes para o `AI_USAGE.md` final.
- Evitar duplicar o texto bruto do desafio em varios arquivos.
