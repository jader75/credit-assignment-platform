package br.com.srm.credit.application.settlement;

import java.util.List;

public record SettlementOperationPage(
        List<SettlementOperationItem> items, int page, int size, long totalElements, long totalPages) {}
