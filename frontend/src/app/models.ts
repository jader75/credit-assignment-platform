export interface PricingSimulationRequest {
  operationReference: string;
  receivableTypeCode: string;
  faceCurrencyCode: string;
  paymentCurrencyCode: string;
  faceAmount: number;
  baseTaxRate: number;
  termDays: number;
}

export interface CurrencyItem {
  code: string;
  name: string;
  symbol: string;
  createdAt: string;
}

export interface ExchangeRateItem {
  id: number;
  fromCurrencyCode: string;
  toCurrencyCode: string;
  rate: number;
  quotedAt: string;
  source: 'MANUAL' | 'MOCK' | 'INTEGRATION';
  createdAt: string;
}

export interface ExchangeRateRequest {
  fromCurrencyCode: string;
  toCurrencyCode: string;
  rate: number;
  quotedAt: string;
  source: 'MANUAL' | 'MOCK' | 'INTEGRATION';
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

export interface BatchImportResult {
  batchReference: string;
  status: string;
  assignmentsImported: number;
  assignorsCreated: number;
  receivableTypesCreated: number;
  processedAt: string;
}

export interface SettlementOperationFilter {
  status: string[];
  batchReference: string;
  assignorDocumentNumber: string;
  page: number;
  size: number;
}

export interface SettlementOperationItem {
  operationReference: string;
  batchReference: string;
  assignorDocumentNumber: string;
  assignorName: string;
  receivableTypeCode: string;
  faceCurrencyCode: string;
  paymentCurrencyCode: string;
  faceAmount: number;
  netAmount: number;
  dueDate: string;
  pricingAt: string;
  liquidatedAt: string;
  status: string;
}

export interface SettlementOperationPage {
  items: SettlementOperationItem[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface SettlementOperationStatusRequest {
  targetStatus: 'PENDING' | 'PRICED' | 'LIQUIDATED' | 'REJECTED';
}
