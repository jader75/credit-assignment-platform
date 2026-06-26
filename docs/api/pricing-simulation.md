# Simulacao de precificacao

## Endpoint

`POST /api/v1/pricing/simulations`

## Descricao

Simula a precificacao de um recebivel informando tipo, moeda, prazo, spread base e taxa de cambio.

## Request

```json
{
  "operationReference": "OP-001",
  "receivableTypeCode": "TRADE_RECEIVABLE",
  "receivablePricingRuleCode": "TRADE_RECEIVABLE",
  "receivableTypeBaseSpread": 0.0150,
  "receivableTypeActive": true,
  "faceCurrencyCode": "BRL",
  "paymentCurrencyCode": "BRL",
  "faceAmount": 1000.00,
  "baseTaxRate": 0.0200,
  "termDays": 30,
  "exchangeRate": 1.00000000
}
```

## Response de sucesso

```json
{
  "operationReference": "OP-001",
  "receivablePricingRuleCode": "TRADE_RECEIVABLE",
  "faceAmount": 1000.00,
  "baseTaxRate": 0.0200,
  "appliedSpread": 0.0150,
  "termDays": 30,
  "discountedAmount": 966.18,
  "exchangeRate": 1.00000000,
  "netAmount": 966.18,
  "crossCurrency": false
}
```

## Erros

- `400 Bad Request`: payload invalido ou corpo malformado
- `422 Unprocessable Entity`: regra de negocio violada

## Observacao

A documentacao OpenAPI gerada pela aplicacao tambem descreve esse contrato e os modelos de erro.
