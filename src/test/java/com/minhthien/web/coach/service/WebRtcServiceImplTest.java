package com.minhthien.web.coach.service;

import com.minhthien.web.coach.config.WebRtcProperties;
import com.minhthien.web.coach.dto.response.WebRtcIceServerResponse;
import com.minhthien.web.coach.entity.User;
import com.minhthien.web.coach.enums.UserRole;
import com.minhthien.web.coach.service.impl.WebRtcServiceImpl;
import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

class WebRtcServiceImplTest {

    @Test
    void returnsStunAndTurnWithCoturnRestCredential() throws Exception {
        WebRtcProperties properties = new WebRtcProperties();
        properties.setStunUrls("stun:stun.l.google.com:19302");
        properties.setTurnUrls("turn:turn.minhthien.io.vn:3478?transport=udp,turn:turn.minhthien.io.vn:3478?transport=tcp");
        properties.setTurnSharedSecret("test-secret");
        properties.setTurnTtlSeconds(3600);
        properties.setIceTransportPolicy("all");
        User user = user("minh");

        WebRtcIceServerResponse response = new WebRtcServiceImpl(properties).getIceServers(user);

        assertThat(response.isTurnConfigured()).isTrue();
        assertThat(response.getIceServers()).hasSize(2);
        assertThat(response.getIceServers().get(0).getUrls()).containsExactly("stun:stun.l.google.com:19302");
        WebRtcIceServerResponse.IceServer turn = response.getIceServers().get(1);
        assertThat(turn.getUrls()).containsExactly(
                "turn:turn.minhthien.io.vn:3478?transport=udp",
                "turn:turn.minhthien.io.vn:3478?transport=tcp"
        );
        assertThat(turn.getUsername()).endsWith(":minh");
        assertThat(Long.parseLong(turn.getUsername().split(":")[0])).isEqualTo(response.getExpiresAt());
        assertThat(turn.getCredential()).isEqualTo(hmacSha1Base64("test-secret", turn.getUsername()));
        assertThat(turn.getCredential()).doesNotContain("test-secret");
        assertThat(response.getIceTransportPolicy()).isEqualTo("all");
    }

    @Test
    void missingTurnSecretReturnsStunOnlyWithoutCrashing() {
        WebRtcProperties properties = new WebRtcProperties();
        properties.setStunUrls("stun:stun.l.google.com:19302");
        properties.setTurnUrls("turn:turn.minhthien.io.vn:3478?transport=udp");
        properties.setTurnSharedSecret("");
        properties.setTurnTtlSeconds(3600);

        WebRtcIceServerResponse response = new WebRtcServiceImpl(properties).getIceServers(user("minh"));

        assertThat(response.isTurnConfigured()).isFalse();
        assertThat(response.getIceServers()).hasSize(1);
        assertThat(response.getIceServers().get(0).getUsername()).isNull();
        assertThat(response.getIceServers().get(0).getCredential()).isNull();
    }

    @Test
    void clampsUnsafeTtlValues() {
        WebRtcProperties low = new WebRtcProperties();
        low.setTurnTtlSeconds(1);
        assertThat(low.getSafeTurnTtlSeconds()).isEqualTo(60);

        WebRtcProperties high = new WebRtcProperties();
        high.setTurnTtlSeconds(999999);
        assertThat(high.getSafeTurnTtlSeconds()).isEqualTo(86400);
    }

    @Test
    void invalidIceTransportPolicyFallsBackToAll() {
        WebRtcProperties properties = new WebRtcProperties();
        properties.setIceTransportPolicy("invalid");

        WebRtcIceServerResponse response = new WebRtcServiceImpl(properties).getIceServers(user("minh"));

        assertThat(response.getIceTransportPolicy()).isEqualTo("all");
    }

    private User user(String username) {
        return User.builder()
                .id(7L)
                .username(username)
                .email(username + "@example.com")
                .role(UserRole.TRAINEES)
                .active(true)
                .build();
    }

    private String hmacSha1Base64(String secret, String username) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA1"));
        return Base64.getEncoder().encodeToString(mac.doFinal(username.getBytes(StandardCharsets.UTF_8)));
    }
}
