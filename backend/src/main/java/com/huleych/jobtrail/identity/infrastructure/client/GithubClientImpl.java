package com.huleych.jobtrail.identity.infrastructure.client;

import com.huleych.jobtrail.identity.domain.exceptions.GithubAuthenticationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Component
public class GithubClientImpl implements GithubClient {
    private final RestTemplate restTemplate = createRestTemplate();
    private final String clientId;
    private final String clientSecret;

    public GithubClientImpl(@Value("${spring.security.oauth2.client.registration.github.client-id}") String clientId, @Value("${spring.security.oauth2.client.registration.github.client-secret}") String clientSecret){
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }
    private static RestTemplate createRestTemplate() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(5));
        requestFactory.setReadTimeout(Duration.ofSeconds(10));
        return new RestTemplate(requestFactory);
    }

    @Override
    public GithubProfile fetchProfile(String code) {
        try {
            return requestProfile(code);
        } catch (RestClientException e) {
            throw new GithubAuthenticationException("Failed to communicate with GitHub");
        }
    }

    private GithubProfile requestProfile(String code) {
        HttpHeaders tokenHeaders = new HttpHeaders();
        tokenHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        tokenHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("code", code);
        ResponseEntity<Map<String,Object>> tokenResponse = restTemplate.exchange(
                "https://github.com/login/oauth/access_token",
                HttpMethod.POST,
                new HttpEntity<>(body, tokenHeaders),
                new ParameterizedTypeReference<>() {}
        );
        Map<String,Object> tokenBody = tokenResponse.getBody();
        if(tokenBody==null || tokenBody.get("access_token")==null) throw new GithubAuthenticationException("GitHub did not return an access token");
        String accessToken = (String) tokenBody.get("access_token");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<Map<String,Object>> userResponse = restTemplate.exchange("https://api.github.com/user",
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<>() {});
        Map<String,Object> userBody = userResponse.getBody();
        if(userBody==null) throw new GithubAuthenticationException("Failed to fetch GitHub user");
        String name = (String) userBody.get("name");
        if(name==null || name.isBlank()) name = (String) userBody.get("login");
        ResponseEntity<List<Map<String,Object>>> response = restTemplate.exchange("https://api.github.com/user/emails",
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<>() {});
        List<Map<String, Object>> emails = response.getBody();
        if(emails==null) throw new GithubAuthenticationException("Failed to find emails");
        String primaryEmail = null;
        for(Map<String, Object> emailEntry:emails){
            if(Boolean.TRUE.equals(emailEntry.get("primary")) && Boolean.TRUE.equals(emailEntry.get("verified"))){
                primaryEmail = (String) emailEntry.get("email");
                break;
            }
        }
        if(primaryEmail==null) throw new GithubAuthenticationException("GitHub account has no verified primary email");

        return new GithubProfile(primaryEmail, name);

    }
}
