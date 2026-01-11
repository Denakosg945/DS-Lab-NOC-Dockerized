package gr.hua.dit.noc.core.impl;

import gr.hua.dit.noc.config.RouteeProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
public class RouteeAuthService {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(RouteeAuthService.class);

    private static final String AUTH_URL =
            "https://auth.routee.net/oauth/token";

    private final RestTemplate restTemplate;
    private final RouteeProperties properties;

    public RouteeAuthService(RestTemplate restTemplate,
                             RouteeProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    @SuppressWarnings("rawtypes")
    @Cacheable("routeeAccessToken")
    public String getAccessToken() {

        LOGGER.info("Requesting Routee access token");

        String credentials =
                properties.getAppId() + ":" + properties.getAppSecret();

        String encoded = Base64.getEncoder()
                .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + encoded);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<String> request =
                new HttpEntity<>("grant_type=client_credentials", headers);

        ResponseEntity<Map> response =
                restTemplate.exchange(
                        AUTH_URL,
                        HttpMethod.POST,
                        request,
                        Map.class
                );

        if (response.getBody() == null) {
            throw new IllegalStateException("No token returned from Routee");
        }

        return (String) response.getBody().get("access_token");
    }
}
