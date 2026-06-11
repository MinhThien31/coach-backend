package com.minhthien.web.coach.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "webrtc")
public class WebRtcProperties {
    private String stunUrls = "stun:stun.l.google.com:19302";
    private String turnUrls = "";
    private String turnSharedSecret = "";
    private long turnTtlSeconds = 3600;
    private String iceTransportPolicy = "all";

    public long getSafeTurnTtlSeconds() {
        if (turnTtlSeconds < 60) {
            return 60;
        }
        if (turnTtlSeconds > 86400) {
            return 86400;
        }
        return turnTtlSeconds;
    }
}
