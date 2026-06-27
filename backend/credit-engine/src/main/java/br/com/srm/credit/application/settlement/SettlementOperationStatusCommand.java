package br.com.srm.credit.application.settlement;

import br.com.srm.credit.domain.shared.CreditAssignmentStatus;

public record SettlementOperationStatusCommand(String operationReference, CreditAssignmentStatus targetStatus) {}
