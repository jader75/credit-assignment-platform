package br.com.srm.credit.application.settlement;

import br.com.srm.credit.domain.shared.CreditAssignmentStatus;
import java.util.List;

public record SettlementOperationFilter(
        List<CreditAssignmentStatus> statuses,
        String batchReference,
        String assignorDocumentNumber,
        int page,
        int size) {}
