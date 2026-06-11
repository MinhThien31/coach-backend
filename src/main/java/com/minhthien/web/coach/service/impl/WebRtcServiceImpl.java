package com.minhthien.web.coach.service.impl;

import com.minhthien.web.coach.config.WebRtcProperties;
import com.minhthien.web.coach.dto.response.WebRtcIceServerResponse;
import com.minhthien.web.coach.entity.User;
import com.minhthien.web.coach.service.WebRtcService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WebRtcServiceImpl implements WebRtcService {

    private static final Logger logger = LoggerFactory.getLogger(WebRtcServiceImpl.class);
    private static final String HMAC_SHA1 = "HmacSHA1";

    private final WebRtcProperties properties;

    @Override
    public WebRtcIceServerResponse getIceServers(User currentUser) {
        long ttlSeconds = properties.getSafeTurnTtlSeconds();
        long expiresAt = Instant.now().getEpochSecond() + ttlSeconds;
        List<WebRtcIceServerResponse.IceServer> iceServers = new ArrayList<>();

        List<String> stunUrls = splitUrls(properties.getStunUrls());
        if (!stunUrls.isEmpty()) {
            iceServers.add(WebRtcIceServerResponse.IceServer.builder()
                    .urls(stunUrls)
                    .build());
        }

        List<String> turnUrls = splitUrls(properties.getTurnUrls());
        boolean turnConfigured = !turnUrls.isEmpty() && StringUtils.hasText(properties.getTurnSharedSecret());
        if (turnConfigured) {
            String username = expiresAt + ":" + currentUser.getUsername();
            iceServers.add(WebRtcIceServerResponse.IceServer.builder()
                    .urls(turnUrls)
                    .username(username)
                    .credential(createCredential(username, properties.getTurnSharedSecret()))
                    .build());
        } else if (!turnUrls.isEmpty() || StringUtils.hasText(properties.getTurnSharedSecret())) {
            logger.warn("TURN is not fully configured; returning STUN-only ICE configuration");
        }

        return WebRtcIceServerResponse.builder()
                .iceServers(iceServers)
                .iceTransportPolicy(normalizeIceTransportPolicy(properties.getIceTransportPolicy()))
                .turnConfigured(turnConfigured)
                .expiresAt(expiresAt)
                .build();
    }

    private List<String> splitUrls(String urls) {
        if (!StringUtils.hasText(urls)) {
            return List.of();
        }
        return Arrays.stream(urls.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .toList();
    }

    private String normalizeIceTransportPolicy(String value) {
        if (!StringUtils.hasText(value)) {
            return "all";
        }
        String normalized = value.trim().toLowerCase();
        if ("all".equals(normalized) || "relay".equals(normalized)) {
            return normalized;
        }
        logger.warn("Invalid WEBRTC_ICE_TRANSPORT_POLICY value; falling back to all");
        return "all";
    }

    private String createCredential(String username, String sharedSecret) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA1);
            mac.init(new SecretKeySpec(sharedSecret.getBytes(StandardCharsets.UTF_8), HMAC_SHA1));
            return Base64.getEncoder().encodeToString(mac.doFinal(username.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to create TURN credential", ex);
        }
    }
}
