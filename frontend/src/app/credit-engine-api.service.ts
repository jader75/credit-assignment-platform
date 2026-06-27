import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';

import {
  BatchImportResult,
  CurrencyItem,
  PricingSimulationRequest,
  PricingSimulationResponse,
  ExchangeRateItem,
  ExchangeRateRequest,
  SettlementOperationFilter,
  SettlementOperationItem,
  SettlementOperationPage,
  SettlementOperationStatusRequest,
  SettlementStatementFilter,
  SettlementStatementPage,
} from './models';

@Injectable()
export class CreditEngineApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = 'http://localhost:8080/api/v1';

  simulate(request: PricingSimulationRequest) {
    return this.http.post<PricingSimulationResponse>(`${this.baseUrl}/pricing/simulations`, request);
  }

  listCurrencies() {
    return this.http.get<CurrencyItem[]>(`${this.baseUrl}/currencies`);
  }

  listExchangeRates() {
    return this.http.get<ExchangeRateItem[]>(`${this.baseUrl}/exchange-rates`);
  }

  createExchangeRate(request: ExchangeRateRequest) {
    return this.http.post<ExchangeRateItem>(`${this.baseUrl}/exchange-rates`, request);
  }

  updateExchangeRate(id: number, request: ExchangeRateRequest) {
    return this.http.put<ExchangeRateItem>(`${this.baseUrl}/exchange-rates/${id}`, request);
  }

  deleteExchangeRate(id: number) {
    return this.http.delete<void>(`${this.baseUrl}/exchange-rates/${id}`);
  }

  importBatch(batchReference: string, file: File) {
    const formData = new FormData();
    formData.append('file', file);

    const params = new HttpParams().set('batchReference', batchReference);
    return this.http.post<BatchImportResult>(`${this.baseUrl}/batches/imports`, formData, { params });
  }

  listSettlementOperations(filter: SettlementOperationFilter) {
    let params = new HttpParams()
      .set('page', filter.page)
      .set('size', filter.size);

    filter.status.forEach((status) => {
      params = params.append('status', status);
    });

    if (filter.batchReference) {
      params = params.set('batchReference', filter.batchReference);
    }
    if (filter.assignorDocumentNumber) {
      params = params.set('assignorDocumentNumber', filter.assignorDocumentNumber);
    }

    return this.http.get<SettlementOperationPage>(`${this.baseUrl}/settlements/operations`, { params });
  }

  changeSettlementOperationStatus(operationReference: string, request: SettlementOperationStatusRequest) {
    return this.http.patch<SettlementOperationItem>(
      `${this.baseUrl}/settlements/operations/${operationReference}/status`,
      request,
    );
  }

  searchSettlement(filter: SettlementStatementFilter) {
    let params = new HttpParams().set('page', filter.page).set('size', filter.size);

    if (filter.startDate) {
      params = params.set('startDate', filter.startDate);
    }
    if (filter.endDate) {
      params = params.set('endDate', filter.endDate);
    }
    if (filter.assignorDocumentNumber) {
      params = params.set('assignorDocumentNumber', filter.assignorDocumentNumber);
    }
    if (filter.paymentCurrencyCode) {
      params = params.set('paymentCurrencyCode', filter.paymentCurrencyCode);
    }

    return this.http.get<SettlementStatementPage>(`${this.baseUrl}/settlements/statements`, { params });
  }
}
