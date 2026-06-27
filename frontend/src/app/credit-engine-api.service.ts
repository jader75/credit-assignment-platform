import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';

import {
  PricingSimulationRequest,
  PricingSimulationResponse,
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
