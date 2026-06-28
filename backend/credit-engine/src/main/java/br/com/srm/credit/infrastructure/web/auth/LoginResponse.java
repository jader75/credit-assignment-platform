package br.com.srm.credit.infrastructure.web.auth;

import java.time.OffsetDateTime;
import java.util.List;

public record LoginResponse(
        String tokenType, String accessToken, String subject, List<String> roles, OffsetDateTime expiresAt) {}
