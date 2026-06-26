# Extrato de liquidacao

## Endpoint

`GET /api/v1/settlements/statements`

## Filtros

- `startDate`: data inicial da liquidacao no formato `YYYY-MM-DD`
- `endDate`: data final da liquidacao no formato `YYYY-MM-DD`
- `assignorDocumentNumber`: documento do cedente
- `paymentCurrencyCode`: moeda de pagamento
- `page`: pagina inicial a partir de `0`
- `size`: quantidade de itens por pagina

## Resposta

Retorna uma pagina com as liquidacoes encontradas, ordenadas por data de liquidacao em ordem decrescente.

## Exemplo de resposta

```json
{
  "items": [
    {
      "operationReference": "OP-001",
      "batchReference": "BATCH-001",
      "assignorDocumentNumber": "12345678000199",
      "assignorName": "Cedente ABC Ltda",
      "paymentCurrencyCode": "BRL",
      "faceAmount": 1000.00,
      "netAmount": 966.18,
      "liquidatedAt": "2026-06-20T12:00:00-03:00",
      "status": "LIQUIDATED"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1
}
```
