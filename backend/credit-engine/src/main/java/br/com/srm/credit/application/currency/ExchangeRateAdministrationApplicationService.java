package br.com.srm.credit.application.currency;

import br.com.srm.credit.domain.currency.CurrencyBusinessException;
import br.com.srm.credit.domain.currency.CurrencyMessage;
import br.com.srm.credit.domain.currency.CurrencyValidationException;
import br.com.srm.credit.domain.shared.ExchangeRateSource;
import br.com.srm.credit.infrastructure.persistence.entity.ExchangeRateEntity;
import br.com.srm.credit.infrastructure.persistence.repository.CurrencyJpaRepository;
import br.com.srm.credit.infrastructure.persistence.repository.ExchangeRateJpaRepository;
import java.util.List;

public class ExchangeRateAdministrationApplicationService {

    private final CurrencyJpaRepository currencyJpaRepository;
    private final ExchangeRateJpaRepository exchangeRateJpaRepository;

    public ExchangeRateAdministrationApplicationService(
            CurrencyJpaRepository currencyJpaRepository, ExchangeRateJpaRepository exchangeRateJpaRepository) {
        this.currencyJpaRepository = currencyJpaRepository;
        this.exchangeRateJpaRepository = exchangeRateJpaRepository;
    }

    public List<ExchangeRateItem> list() {
        return exchangeRateJpaRepository
                .findAllBySourceInOrderByQuotedAtDescIdDesc(List.of(ExchangeRateSource.MANUAL, ExchangeRateSource.MOCK))
                .stream()
                .map(ExchangeRateAdministrationApplicationService::map)
                .toList();
    }

    public ExchangeRateItem create(ExchangeRateCommand command) {
        var normalized = normalize(command);
        validateCurrencies(normalized.fromCurrencyCode(), normalized.toCurrencyCode());
        validateSource(normalized.source());

        var saved = exchangeRateJpaRepository.save(new ExchangeRateEntity(
                normalized.fromCurrencyCode(),
                normalized.toCurrencyCode(),
                normalized.rate(),
                normalized.quotedAt(),
                normalized.source()));
        return map(saved);
    }

    public ExchangeRateItem update(Long id, ExchangeRateCommand command) {
        if (id == null) {
            throw new CurrencyValidationException(CurrencyMessage.EXCHANGE_RATE_ID_INVALID);
        }

        var normalized = normalize(command);
        validateCurrencies(normalized.fromCurrencyCode(), normalized.toCurrencyCode());
        validateSource(normalized.source());

        var exchangeRate = exchangeRateJpaRepository
                .findById(id)
                .orElseThrow(() -> new CurrencyBusinessException(CurrencyMessage.EXCHANGE_RATE_NOT_FOUND));
        exchangeRate.update(
                normalized.fromCurrencyCode(),
                normalized.toCurrencyCode(),
                normalized.rate(),
                normalized.quotedAt(),
                normalized.source());

        return map(exchangeRateJpaRepository.save(exchangeRate));
    }

    public void delete(Long id) {
        if (id == null) {
            throw new CurrencyValidationException(CurrencyMessage.EXCHANGE_RATE_ID_INVALID);
        }
        if (!exchangeRateJpaRepository.existsById(id)) {
            throw new CurrencyBusinessException(CurrencyMessage.EXCHANGE_RATE_NOT_FOUND);
        }
        exchangeRateJpaRepository.deleteById(id);
    }

    private void validateCurrencies(String fromCurrencyCode, String toCurrencyCode) {
        if (!currencyJpaRepository.existsById(fromCurrencyCode)) {
            throw new CurrencyBusinessException(CurrencyMessage.CURRENCY_NOT_FOUND);
        }
        if (!currencyJpaRepository.existsById(toCurrencyCode)) {
            throw new CurrencyBusinessException(CurrencyMessage.CURRENCY_NOT_FOUND);
        }
        if (fromCurrencyCode.equals(toCurrencyCode)) {
            throw new CurrencyBusinessException(CurrencyMessage.CURRENCY_PAIR_INVALID);
        }
    }

    private static ExchangeRateCommand normalize(ExchangeRateCommand command) {
        if (command == null) {
            throw new CurrencyValidationException(CurrencyMessage.EXCHANGE_RATE_ID_INVALID);
        }
        if (command.fromCurrencyCode() == null || command.fromCurrencyCode().isBlank()) {
            throw new CurrencyValidationException(CurrencyMessage.FROM_CURRENCY_INVALID);
        }
        if (command.toCurrencyCode() == null || command.toCurrencyCode().isBlank()) {
            throw new CurrencyValidationException(CurrencyMessage.TO_CURRENCY_INVALID);
        }
        if (command.rate() == null || command.rate().signum() <= 0) {
            throw new CurrencyValidationException(CurrencyMessage.EXCHANGE_RATE_VALUE_INVALID);
        }
        if (command.quotedAt() == null) {
            throw new CurrencyValidationException(CurrencyMessage.EXCHANGE_RATE_ID_INVALID);
        }
        if (command.source() == null) {
            throw new CurrencyValidationException(CurrencyMessage.EXCHANGE_RATE_SOURCE_INVALID);
        }

        return new ExchangeRateCommand(
                command.fromCurrencyCode().trim().toUpperCase(),
                command.toCurrencyCode().trim().toUpperCase(),
                command.rate(),
                command.quotedAt(),
                command.source());
    }

    private static void validateSource(ExchangeRateSource source) {
        if (source == ExchangeRateSource.INTEGRATION) {
            throw new CurrencyBusinessException(CurrencyMessage.EXCHANGE_RATE_SOURCE_NOT_SUPPORTED);
        }
    }

    private static ExchangeRateItem map(ExchangeRateEntity entity) {
        return new ExchangeRateItem(
                entity.getId(),
                entity.getFromCurrencyCode(),
                entity.getToCurrencyCode(),
                entity.getRate(),
                entity.getQuotedAt(),
                entity.getSource(),
                entity.getCreatedAt());
    }
}
