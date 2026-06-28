# Diagrama de Banco - V2

Este diretório documenta a migration `V2__seed_currency_reference_data.sql`.

## Escopo da V2

- não altera a estrutura fisica do modelo
- adiciona dados de referencia iniciais
- mantém o mesmo ER da V1

## Relacao com a V1

O desenho do banco permanece o mesmo da [V1](../v1/README.md).
A diferença da V2 é apenas o seed de referência para moedas e taxas manuais iniciais.

## Arquivos relacionados

- Migration: `../../../../backend/credit-engine/src/main/resources/db/migration/V2__seed_currency_reference_data.sql`
- Seed documental: `../../data.sql`
- Modelo estrutural: [V1](../v1/README.md)
