# ADR 0005 - Schema V1 e seed V2

## Status

Aceito

## Contexto

A modelagem do banco precisou ser organizada para separar estrutura, seed e dados documentais.

Havia risco de tratar dados iniciais como se fossem alteracao estrutural, o que deixaria a documentacao confusa.

## Decisao

Manter:

- **V1** para o schema estrutural
- **V2** para seed de dados de referencia

## Alternativas consideradas

- misturar estrutura e seed na mesma versao
- criar novas versoes de schema sem alteracao estrutural
- documentar apenas um arquivo unico sem separacao de escopo

## Consequencias

- fica claro o que e estrutura e o que e carga inicial
- o diagrama ER oficial permanece em sintonia com a V1
- o seed pode evoluir sem aparentar uma mudanca de modelo

## Situacao atual

O `schema.sql` reflete a estrutura final, a `data.sql` reflete o seed inicial e a documentacao de banco separa corretamente os dois niveis.
