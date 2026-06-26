package br.com.srm.credit.application.settlement;

import java.util.List;

public record SettlementStatementPage(
        List<SettlementStatementItem> items, int page, int size, long totalElements, long totalPages) {}
