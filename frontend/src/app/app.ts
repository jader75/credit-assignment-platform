import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { AuthService } from './auth.service';
import { CreditEngineApiService } from './credit-engine-api.service';
import {
  BatchImportResult,
  CurrencyItem,
  ExchangeRateItem,
  ExchangeRateRequest,
  PricingSimulationRequest,
  PricingSimulationResponse,
  SettlementOperationFilter,
  SettlementOperationItem,
  SettlementOperationPage,
  SettlementOperationStatusRequest,
  SettlementStatementFilter,
  SettlementStatementPage,
} from './models';

type OptionItem = {
  value: string;
  label: string;
};

type SectionKey = 'simulation' | 'statement' | 'exchange' | 'batch' | 'operations';
type OperationView = 'OPEN' | 'ALL' | 'LIQUIDATED' | 'REJECTED';

type ExchangeRateFormState = {
  id: number | null;
  fromCurrencyCode: string;
  toCurrencyCode: string;
  rate: number;
  quotedAt: string;
  source: 'MANUAL' | 'MOCK' | 'INTEGRATION';
};

@Component({
  selector: 'app-root',
  imports: [CommonModule, FormsModule],
  providers: [CreditEngineApiService],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App implements OnInit {
  private readonly api = inject(CreditEngineApiService);
  private readonly auth = inject(AuthService);

  protected readonly pageTitle = 'Plataforma de Cessão de Crédito SRM';

  protected activeSection: SectionKey = 'simulation';

  protected readonly simulation: PricingSimulationRequest = {
    operationReference: 'OP-001',
    receivableTypeCode: 'TRADE_RECEIVABLE',
    faceCurrencyCode: 'BRL',
    paymentCurrencyCode: 'BRL',
    faceAmount: 1000,
    baseTaxRate: 0.02,
    termDays: 30,
  };

  protected readonly settlementFilter: SettlementStatementFilter = {
    startDate: this.toDateInputValue(new Date()),
    endDate: this.toDateInputValue(new Date()),
    assignorDocumentNumber: '',
    paymentCurrencyCode: '',
    page: 0,
    size: 10,
  };

  protected readonly initialCurrencyOptions: OptionItem[] = [
    { value: 'BRL', label: 'Real brasileiro' },
    { value: 'USD', label: 'Dólar americano' },
  ];

  protected currencyOptions: OptionItem[] = [...this.initialCurrencyOptions];

  protected readonly strategyOptions: OptionItem[] = [
    { value: 'TRADE_RECEIVABLE', label: 'Duplicata mercantil (estratégia)' },
    { value: 'POST_DATED_CHECK', label: 'Cheque pré-datado (estratégia)' },
  ];

  protected readonly receivableTypeOptions: OptionItem[] = [
    { value: 'TRADE_RECEIVABLE', label: 'Duplicata mercantil' },
    { value: 'POST_DATED_CHECK', label: 'Cheque pré-datado' },
  ];

  protected readonly exchangeRateSourceOptions: OptionItem[] = [
    { value: 'MANUAL', label: 'Manual' },
    { value: 'MOCK', label: 'Mock' },
  ];

  protected readonly batchTemplate = `operationReference,assignorDocumentNumber,assignorName,riskRating,receivableTypeCode,receivableTypeName,faceCurrencyCode,faceAmount,dueDate,baseTaxRate,termDays,paymentCurrencyCode
OP-001,12345678901,Acme LTDA,A,TRADE_RECEIVABLE,Duplicata Mercantil,BRL,1000.00,2026-07-20,0.0200,30,BRL`;

  protected readonly operationViewOptions: OptionItem[] = [
    { value: 'OPEN', label: 'Pendentes' },
    { value: 'ALL', label: 'Todas' },
    { value: 'LIQUIDATED', label: 'Liquidadas' },
    { value: 'REJECTED', label: 'Rejeitadas' },
  ];

  protected readonly genericErrorMessage =
    'Não foi possível concluir a operação no momento. Verifique os dados e tente novamente.';
  protected readonly exchangeRateForbiddenMessage =
    'Seu perfil não tem permissão para inclusão, edição e exclusão de taxas de câmbio.';
  protected readonly operationForbiddenMessage =
    'Seu perfil não tem permissão para alterar o status das operações.';

  protected loginUsername = 'operator';
  protected loginPassword = 'operator123';
  protected loginLoading = false;
  protected loginError = '';

  protected simulationResult: PricingSimulationResponse | null = null;
  protected statementPage: SettlementStatementPage | null = null;
  protected operationPage: SettlementOperationPage | null = null;
  protected exchangeRates: ExchangeRateItem[] = [];
  protected batchImportResult: BatchImportResult | null = null;
  protected selectedOperation: SettlementOperationItem | null = null;

  protected simulationLoading = false;
  protected statementLoading = false;
  protected operationLoading = false;
  protected exchangeRatesLoading = false;
  protected exchangeRateSaving = false;
  protected batchImportLoading = false;
  protected operationSaving = false;
  protected errorMessage = '';
  protected exchangeRateError = '';
  protected batchImportError = '';
  protected operationError = '';
  protected batchFileName = '';

  protected exchangeRateForm: ExchangeRateFormState = this.buildEmptyExchangeRateForm();
  protected batchReference = 'BATCH-001';
  protected operationView: OperationView = 'OPEN';
  protected operationPageNumber = 0;
  protected operationFilterBatchReference = '';
  protected operationFilterAssignorDocumentNumber = '';
  private batchFile: File | null = null;

  ngOnInit(): void {
    if (this.auth.isAuthenticated()) {
      this.loadInitialData();
    }
  }

  protected get isAuthenticated(): boolean {
    return this.auth.isAuthenticated();
  }

  protected get authenticatedUser(): string {
    return this.auth.username();
  }

  protected get authenticatedRoles(): string[] {
    return this.auth.roles();
  }

  protected get authenticatedRoleLabels(): string[] {
    return this.auth.roles().map((role) => this.roleLabel(role));
  }

  protected login(): void {
    this.loginError = '';
    this.loginLoading = true;
    this.auth.login({ username: this.loginUsername, password: this.loginPassword }).subscribe({
      next: () => {
        this.loginLoading = false;
        this.loadInitialData();
      },
      error: (error) => {
        this.loginError = this.extractErrorMessage(error);
        this.loginLoading = false;
      },
    });
  }

  protected logout(): void {
    this.auth.logout();
    this.simulationResult = null;
    this.statementPage = null;
    this.operationPage = null;
    this.exchangeRates = [];
    this.batchImportResult = null;
    this.selectedOperation = null;
    this.errorMessage = '';
    this.exchangeRateError = '';
    this.batchImportError = '';
    this.operationError = '';
  }

  protected openSection(section: SectionKey): void {
    this.activeSection = section;
    if (section === 'statement' && this.statementPage === null && !this.statementLoading) {
      this.loadStatement(0);
    }
    if (section === 'exchange' && !this.exchangeRatesLoading) {
      this.loadExchangeRates();
    }
    if (section === 'operations' && !this.operationLoading) {
      this.loadOperations(0);
    }
  }

  protected onBatchFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.batchFile = input.files?.item(0) ?? null;
    this.batchFileName = this.batchFile?.name ?? '';
  }

  protected simulate(): void {
    this.errorMessage = '';
    this.simulationResult = null;
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

    this.statementPage = null;
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
    this.settlementFilter.startDate = this.toDateInputValue(new Date());
    this.settlementFilter.endDate = this.toDateInputValue(new Date());
    this.settlementFilter.assignorDocumentNumber = '';
    this.settlementFilter.paymentCurrencyCode = '';
    this.settlementFilter.page = 0;
    this.settlementFilter.size = 10;
    this.statementPage = null;
    this.loadStatement(0);
  }

  protected simulateExample(): void {
    this.simulation.operationReference = 'OP-002';
    this.simulation.receivableTypeCode = 'POST_DATED_CHECK';
    this.simulation.faceCurrencyCode = 'BRL';
    this.simulation.paymentCurrencyCode = 'USD';
    this.simulation.faceAmount = 5000;
    this.simulation.baseTaxRate = 0.0185;
    this.simulation.termDays = 45;
  }

  protected loadExchangeRates(): void {
    this.exchangeRateError = '';
    this.exchangeRatesLoading = true;
    this.api.listExchangeRates().subscribe({
      next: (items) => {
        this.exchangeRates = items;
        this.exchangeRatesLoading = false;
      },
      error: (error) => {
        this.exchangeRateError = this.extractErrorMessage(error);
        this.exchangeRatesLoading = false;
      },
    });
  }

  protected submitExchangeRate(): void {
    this.exchangeRateError = '';
    this.exchangeRateSaving = true;

    const request = this.buildExchangeRateRequest();
    const operation$ =
      this.exchangeRateForm.id === null
        ? this.api.createExchangeRate(request)
        : this.api.updateExchangeRate(this.exchangeRateForm.id, request);

    operation$.subscribe({
      next: () => {
        this.exchangeRateSaving = false;
        this.resetExchangeRateForm();
        this.loadExchangeRates();
      },
      error: (error) => {
        this.exchangeRateError = this.extractErrorMessage(error, this.exchangeRateForbiddenMessage);
        this.exchangeRateSaving = false;
      },
    });
  }

  protected editExchangeRate(item: ExchangeRateItem): void {
    this.exchangeRateForm = {
      id: item.id,
      fromCurrencyCode: item.fromCurrencyCode,
      toCurrencyCode: item.toCurrencyCode,
      rate: item.rate,
      quotedAt: this.toDatetimeLocalValue(item.quotedAt),
      source: item.source,
    };
    this.activeSection = 'exchange';
  }

  protected deleteExchangeRate(item: ExchangeRateItem): void {
    if (!confirm(`Remover a taxa ${item.fromCurrencyCode}/${item.toCurrencyCode} de ${item.rate}?`)) {
      return;
    }

    this.exchangeRateError = '';
    this.exchangeRateSaving = true;
    this.api.deleteExchangeRate(item.id).subscribe({
      next: () => {
        this.exchangeRateSaving = false;
        if (this.exchangeRateForm.id === item.id) {
          this.resetExchangeRateForm();
        }
        this.loadExchangeRates();
      },
      error: (error) => {
        this.exchangeRateError = this.extractErrorMessage(error, this.exchangeRateForbiddenMessage);
        this.exchangeRateSaving = false;
      },
    });
  }

  protected resetExchangeRateForm(): void {
    this.exchangeRateForm = this.buildEmptyExchangeRateForm();
  }

  protected importBatch(): void {
    if (!this.batchFile) {
      this.batchImportError = 'Selecione um arquivo CSV antes de importar.';
      return;
    }

    this.batchImportError = '';
    this.batchImportLoading = true;
    this.api.importBatch(this.batchReference, this.batchFile).subscribe({
      next: (result) => {
        this.batchImportResult = result;
        this.batchImportLoading = false;
      },
      error: (error) => {
        this.batchImportError = this.extractErrorMessage(error);
        this.batchImportLoading = false;
      },
    });
  }

  protected resetBatchImportForm(): void {
    this.batchReference = 'BATCH-001';
    this.batchFile = null;
    this.batchFileName = '';
    this.batchImportResult = null;
    this.batchImportError = '';
  }

  protected loadOperations(pageOverride?: number): void {
    this.operationError = '';
    if (pageOverride !== undefined) {
      this.operationPageNumber = pageOverride;
    }

    this.operationPage = null;
    this.selectedOperation = null;
    this.operationLoading = true;
    this.api.listSettlementOperations(this.buildOperationFilter()).subscribe({
      next: (page) => {
        this.operationPage = page;
        this.operationLoading = false;
      },
      error: (error) => {
        this.operationError = this.extractErrorMessage(error);
        this.operationLoading = false;
      },
    });
  }

  protected setOperationView(view: OperationView): void {
    this.operationView = view;
    this.loadOperations(0);
  }

  protected liquidateOperation(item: SettlementOperationItem): void {
    this.changeOperationStatus(item, 'LIQUIDATED');
  }

  protected rejectOperation(item: SettlementOperationItem): void {
    this.changeOperationStatus(item, 'REJECTED');
  }

  protected reopenOperation(item: SettlementOperationItem): void {
    this.changeOperationStatus(item, 'PENDING');
  }

  protected resetOperationFilters(): void {
    this.operationView = 'OPEN';
    this.operationPageNumber = 0;
    this.operationFilterBatchReference = '';
    this.operationFilterAssignorDocumentNumber = '';
    this.operationPage = null;
    this.selectedOperation = null;
    this.loadOperations(0);
  }

  protected previousOperationPage(): void {
    if ((this.operationPage?.page ?? 0) > 0) {
      this.loadOperations((this.operationPage?.page ?? 0) - 1);
    }
  }

  protected nextOperationPage(): void {
    const currentPage = this.operationPage?.page ?? 0;
    const totalPages = this.operationPage?.totalPages ?? 0;
    if (currentPage + 1 < totalPages) {
      this.loadOperations(currentPage + 1);
    }
  }

  protected trackByOperationReference(_: number, item: { operationReference: string }): string {
    return item.operationReference;
  }

  protected trackByExchangeRateId(_: number, item: ExchangeRateItem): number {
    return item.id;
  }

  protected trackByOperationRow(_: number, item: SettlementOperationItem): string {
    return item.operationReference;
  }

  protected currencyLabel(code: string): string {
    return this.findLabel(this.currencyOptions, code);
  }

  protected strategyLabel(code: string): string {
    return this.findLabel(this.strategyOptions, code);
  }

  protected receivableTypeLabel(code: string): string {
    return this.findLabel(this.receivableTypeOptions, code);
  }

  protected sourceLabel(source: string): string {
    return (
      {
        MANUAL: 'Manual',
        MOCK: 'Mock',
        INTEGRATION: 'Integração',
      }[source] ?? this.findLabel(this.exchangeRateSourceOptions, source)
    );
  }

  protected statusLabel(status: string): string {
    return {
      PENDING: 'Pendente',
      PRICED: 'Precificado',
      LIQUIDATED: 'Liquidado',
      REJECTED: 'Rejeitado',
    }[status] ?? status;
  }

  protected canLiquidate(item: SettlementOperationItem): boolean {
    return item.status === 'PENDING' || item.status === 'PRICED';
  }

  protected canReject(item: SettlementOperationItem): boolean {
    return item.status === 'PENDING' || item.status === 'PRICED';
  }

  protected canReopen(item: SettlementOperationItem): boolean {
    return item.status === 'REJECTED';
  }

  private loadCurrencies(): void {
    this.api.listCurrencies().subscribe({
      next: (items) => {
        if (items.length > 0) {
          this.currencyOptions = items.map((item: CurrencyItem) => ({
            value: item.code,
            label: `${item.code} - ${item.name}`,
          }));
        }
      },
      error: () => {
        this.currencyOptions = [...this.initialCurrencyOptions];
      },
    });
  }

  private loadInitialData(): void {
    this.loadCurrencies();
    this.loadExchangeRates();
  }

  private changeOperationStatus(item: SettlementOperationItem, targetStatus: SettlementOperationStatusRequest['targetStatus']): void {
    this.operationError = '';
    this.operationSaving = true;
    this.api.changeSettlementOperationStatus(item.operationReference, { targetStatus }).subscribe({
      next: (updated) => {
        this.selectedOperation = updated;
        this.operationSaving = false;
        this.loadOperations();
      },
      error: (error) => {
        this.operationError = this.extractErrorMessage(error, this.operationForbiddenMessage);
        this.operationSaving = false;
      },
    });
  }

  private buildOperationFilter(): SettlementOperationFilter {
    const status =
      this.operationView === 'OPEN'
        ? ['PENDING', 'PRICED']
        : this.operationView === 'ALL'
          ? ['PENDING', 'PRICED', 'LIQUIDATED', 'REJECTED']
          : [this.operationView];

    return {
      status,
      batchReference: this.operationFilterBatchReference,
      assignorDocumentNumber: this.operationFilterAssignorDocumentNumber,
      page: this.operationPageNumber,
      size: this.operationPage?.size ?? 10,
    };
  }

  private buildExchangeRateRequest(): ExchangeRateRequest {
    return {
      fromCurrencyCode: this.exchangeRateForm.fromCurrencyCode,
      toCurrencyCode: this.exchangeRateForm.toCurrencyCode,
      rate: this.exchangeRateForm.rate,
      quotedAt: this.toIsoDateTime(this.exchangeRateForm.quotedAt),
      source: this.exchangeRateForm.source,
    };
  }

  private buildEmptyExchangeRateForm(): ExchangeRateFormState {
    return {
      id: null,
      fromCurrencyCode: 'BRL',
      toCurrencyCode: 'USD',
      rate: 1,
      quotedAt: this.toDatetimeLocalValue(new Date().toISOString()),
      source: 'MANUAL',
    };
  }

  private findLabel(options: OptionItem[], value: string): string {
    return options.find((option) => option.value === value)?.label ?? value;
  }

  private extractErrorMessage(error: unknown, forbiddenMessage?: string): string {
    const response = error as HttpErrorResponse;

    if (response?.status === 0) {
      return 'Não foi possível conectar ao servidor. Verifique a rede e tente novamente.';
    }
    if (response?.status === 401) {
      return 'Sua sessão não está autenticada. Entre novamente.';
    }
    if (response?.status === 403) {
      return forbiddenMessage ?? 'Seu perfil não tem permissão para executar esta ação.';
    }
    if (response?.status === 404) {
      return 'Registro não encontrado para os filtros informados.';
    }
    if (response?.status === 409) {
      return 'Já existe um registro com esses dados.';
    }
    if (response?.status === 422) {
      return 'Alguns campos estão inválidos. Revise os dados e tente novamente.';
    }
    if (response?.status && response.status >= 500) {
      return 'Falha inesperada no servidor. Tente novamente em instantes.';
    }

    const responseError = response?.error as { message?: string; error?: string } | string | undefined;
    const message =
      (typeof responseError === 'string'
        ? responseError
        : responseError?.message ?? responseError?.error ?? response?.message) ?? '';
    if (!message) {
      return this.genericErrorMessage;
    }
    if (message.includes('Erro inesperado') || message.includes('Falha inesperada')) {
      return this.genericErrorMessage;
    }
    return message;
  }

  private roleLabel(code: string): string {
    return {
      ADMIN: 'Administrador',
      OPERATOR: 'Operador',
    }[code] ?? code;
  }

  private toDatetimeLocalValue(isoDateTime: string): string {
    const date = new Date(isoDateTime);
    const offsetMinutes = date.getTimezoneOffset();
    const localDate = new Date(date.getTime() - offsetMinutes * 60_000);
    return localDate.toISOString().slice(0, 16);
  }

  private toIsoDateTime(localDateTime: string): string {
    return new Date(localDateTime).toISOString();
  }

  private toDateInputValue(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }
}



