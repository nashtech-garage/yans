package com.yas.order.service;

import com.yas.order.config.ServiceUrlConfig;
import com.yas.order.viewmodel.tax.TaxRateVm;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class TaxService extends AbstractCircuitBreakFallbackHandler {
    private final RestClient webClient;
    private final ServiceUrlConfig serviceUrlConfig;

    @Retry(name = "restApi")
    @CircuitBreaker(name = "restCircuitBreaker", fallbackMethod = "handleDoubleFallback")
    public Double getTaxPercentByAddress(Long taxClassId, Long countryId, Long stateOrProvinceId, String zipCode) {
        final URI url = UriComponentsBuilder.fromHttpUrl(serviceUrlConfig.tax())
            .path("/backoffice/tax-rates/tax-percent")
                .queryParam("taxClassId", taxClassId)
                .queryParam("countryId", countryId)
                .queryParam("stateOrProvinceId", stateOrProvinceId)
                .queryParam("zipCode", zipCode)
                .build().toUri();

        final String jwt = ((Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal())
            .getTokenValue();
        return webClient.get()
                .uri(url)
                .headers(h -> h.setBearerAuth(jwt))
                .retrieve()
                .body(Double.class);
    }

    protected Double handleDoubleFallback(Throwable throwable) throws Throwable {
        return handleTypedFallback(throwable);
    }

    @Retry(name = "restApi")
    @CircuitBreaker(name = "restCircuitBreaker", fallbackMethod = "handleListFallback")
    public List<TaxRateVm> getTaxRate(List<Long> taxClassIds, Long countryId, Long stateOrProvinceId, String zipCode) {
        final URI url = UriComponentsBuilder.fromHttpUrl(serviceUrlConfig.tax())
            .path("/backoffice/tax-rates/location-based-batch")
            .queryParam("taxClassIds", taxClassIds)
            .queryParam("countryId", countryId)
            .queryParam("stateOrProvinceId", stateOrProvinceId)
            .queryParam("zipCode", zipCode)
            .build().toUri();

        final String jwt = ((Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal())
            .getTokenValue();

        return webClient.get()
            .uri(url)
            .headers(h -> h.setBearerAuth(jwt))
            .retrieve()
            .toEntity(new ParameterizedTypeReference<List<TaxRateVm>>() {
            })
            .getBody();

    }

    protected List<TaxRateVm> handleListFallback(Throwable throwable) throws Throwable {
        return handleTypedFallback(throwable);
    }
}
