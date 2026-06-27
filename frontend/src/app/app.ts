import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { CreditEngineApiService } from './credit-engine-api.service';
import {
  PricingSimulationRequest,
  PricingSimulationResponse,
  SettlementStatementFilter,
  SettlementStatementPage,
} from './models';

@Component({
  selector: 'app-root',
  imports: [CommonModule, FormsModule],
  providers: [CreditEngineApiService],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  private readonly api = inject(CreditEngineApiService);

  protected readonly simulation: PricingSimulationRequest = {
    operationReference: 'OP-001',
    receivableTypeCode: 'COMMERCIAL_RECEIVABLE',
    receivablePricingRuleCode: 'COMMERCIAL_RECEIVABLE',
    receivableTypeBaseSpread: 0.015,
    receivableTypeActive: true,
    faceCurrencyCode: 'BRL',
    paymentCurrencyCode: 'BRL',
    faceAmount: 1000,
    baseTaxRate: 0.02,
    termDays: 30,
    exchangeRate: 1,
  };

  protected readonly settlementFilter: SettlementStatementFilter = {
    startDate: '',
    endDate: '',
    assignorDocumentNumber: '',
    paymentCurrencyCode: '',
    page: 0,
    size: 10,
  };

  protected simulationResult: PricingSimulationResponse | null = null;
  protected statementPage: SettlementStatementPage | null = null;
  protected simulationLoading = false;
  protected statementLoading = false;
  protected errorMessage = '';
  protected readonly currencyOptions = ['BRL', 'USD'];
  protected readonly ruleOptions = ['COMMERCIAL_RECEIVABLE', 'POST_DATED_CHECK'];
  protected readonly receivableTypeOptions = ['COMMERCIAL_RECEIVABLE', 'POST_DATED_CHECK'];

  ngOnInit(): void {
    this.loadStatement();
  }

  protected simulate(): void {
    this.errorMessage = '';
    this.simulationLoading = true;
    this.api.simulate(this.simulation).subscribe({
      next: (result) => {
        this.simulationResult = result;
        this.simulationLoading = false;
      },
      error: (error) => {
        this.errorMessage = this.extractErrorMessage(error);
        this.simulationLoading = false;
      },
    });
  }

  protected loadStatement(pageOverride?: number): void {
    this.errorMessage = '';
    if (pageOverride !== undefined) {
      this.settlementFilter.page = pageOverride;
    }

    this.statementLoading = true;
    this.api.searchSettlement(this.settlementFilter).subscribe({
      next: (page) => {
        this.statementPage = page;
        this.statementLoading = false;
      },
      error: (error) => {
        this.errorMessage = this.extractErrorMessage(error);
        this.statementLoading = false;
      },
    });
  }

  protected previousPage(): void {
    if ((this.statementPage?.page ?? 0) > 0) {
      this.loadStatement((this.statementPage?.page ?? 0) - 1);
    }
  }

  protected nextPage(): void {
    const currentPage = this.statementPage?.page ?? 0;
    const totalPages = this.statementPage?.totalPages ?? 0;
    if (currentPage + 1 < totalPages) {
      this.loadStatement(currentPage + 1);
    }
  }

  protected resetFilters(): void {
    this.settlementFilter.startDate = '';
    this.settlementFilter.endDate = '';
    this.settlementFilter.assignorDocumentNumber = '';
    this.settlementFilter.paymentCurrencyCode = '';
    this.settlementFilter.page = 0;
    this.settlementFilter.size = 10;
    this.loadStatement(0);
  }

  protected simulateExample(): void {
    this.simulation.operationReference = 'OP-002';
    this.simulation.receivableTypeCode = 'POST_DATED_CHECK';
    this.simulation.receivablePricingRuleCode = 'POST_DATED_CHECK';
    this.simulation.receivableTypeBaseSpread = 0.025;
    this.simulation.receivableTypeActive = true;
    this.simulation.faceCurrencyCode = 'BRL';
    this.simulation.paymentCurrencyCode = 'USD';
    this.simulation.faceAmount = 5000;
    this.simulation.baseTaxRate = 0.0185;
    this.simulation.termDays = 45;
    this.simulation.exchangeRate = 5.2;
  }

  protected trackByOperationReference(_: number, item: { operationReference: string }): string {
    return item.operationReference;
  }

  private extractErrorMessage(error: unknown): string {
    const response = error as {
      error?: { message?: string; error?: string };
      message?: string;
    };
    return response.error?.message ?? response.error?.error ?? response.message ?? 'Falha ao processar a requisicao.';
  }
}
