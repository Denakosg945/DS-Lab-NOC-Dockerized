package gr.hua.dit.noc.core.impl;

import gr.hua.dit.noc.config.RouteeProperties;
import gr.hua.dit.noc.core.SmsService;
import gr.hua.dit.noc.core.impl.RouteeAuthService;
import gr.hua.dit.noc.core.model.SendSmsRequest;
import gr.hua.dit.noc.core.model.SendSmsResult;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class RouteeSmsService implements SmsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RouteeSmsService.class);
    private static final String SMS_URL = "https://connect.routee.net/sms";

    private final RestTemplate restTemplate;
    private final RouteeProperties properties;
    private final RouteeAuthService authService;

    public RouteeSmsService(
            RestTemplate restTemplate,
            RouteeProperties properties,
            RouteeAuthService authService) {

        this.restTemplate = restTemplate;
        this.properties = properties;
        this.authService = authService;
    }

    @Override
    public SendSmsResult send(@Valid SendSmsRequest request) {

        String token = authService.getAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
                "body", request.content(),
                "to", request.e164(),
                "from", properties.getSender()
        );

        HttpEntity<Map<String, Object>> entity =
                new HttpEntity<>(body, headers);

        ResponseEntity<String> response =
                restTemplate.postForEntity(SMS_URL, entity, String.class);

        LOGGER.info("Routee SMS response: {}", response.getStatusCode());

        return new SendSmsResult(response.getStatusCode().is2xxSuccessful());
    }
}
