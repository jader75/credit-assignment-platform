export interface PricingSimulationRequest {
  operationReference: string;
  receivableTypeCode: string;
  receivablePricingRuleCode: string;
  receivableTypeBaseSpread: number;
  receivableTypeActive: boolean;
  faceCurrencyCode: string;
  paymentCurrencyCode: string;
  faceAmount: number;
  baseTaxRate: number;
  termDays: number;
  exchangeRate: number;
}

export interface PricingSimulationResponse {
  operationReference: string;
  receivablePricingRuleCode: string;
  faceAmount: number;
  baseTaxRate: number;
  appliedSpread: number;
  termDays: number;
  discountedAmount: number;
  exchangeRate: number;
  netAmount: number;
  crossCurrency: boolean;
}

export interface SettlementStatementFilter {
  startDate: string;
  endDate: string;
  assignorDocumentNumber: string;
  paymentCurrencyCode: string;
  page: number;
  size: number;
}

export interface SettlementStatementItem {
  operationReference: string;
  batchReference: string;
  assignorDocumentNumber: string;
  assignorName: string;
  paymentCurrencyCode: string;
  faceAmount: number;
  netAmount: number;
  liquidatedAt: string;
  status: string;
}

export interface SettlementStatementPage {
  items: SettlementStatementItem[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}
