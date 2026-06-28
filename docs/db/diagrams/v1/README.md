# Diagrama de Banco - V1

Este diretório guarda a versão estrutural do diagrama alinhada à migration principal da Story 004.

- Migration base: `backend/credit-engine/src/main/resources/db/migration/V1__create_base_schema.sql`
- Arquivo editável: `credit-domain.drawio`

O diagrama deve ser atualizado junto de mudanças relevantes no schema. Ajustes menores podem permanecer na mesma versão.

## Relacao com a V2

A migration `V2__seed_currency_reference_data.sql` adiciona apenas dados de referência. Ela não altera o desenho estrutural do banco, então o ER da V1 continua válido.
