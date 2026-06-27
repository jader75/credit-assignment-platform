package br.com.srm.credit.application.currency;

import br.com.srm.credit.infrastructure.persistence.entity.CurrencyEntity;
import br.com.srm.credit.infrastructure.persistence.repository.CurrencyJpaRepository;
import java.util.Comparator;
import java.util.List;

public class CurrencyQueryService {

    private final CurrencyJpaRepository currencyJpaRepository;

    public CurrencyQueryService(CurrencyJpaRepository currencyJpaRepository) {
        this.currencyJpaRepository = currencyJpaRepository;
    }

    public List<CurrencyItem> list() {
        return currencyJpaRepository.findAll().stream()
                .sorted(Comparator.comparing(CurrencyEntity::getCode))
                .map(entity ->
                        new CurrencyItem(entity.getCode(), entity.getName(), entity.getSymbol(), entity.getCreatedAt()))
                .toList();
    }
}
