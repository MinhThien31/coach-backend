package com.minhthien.web.coach.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebRtcIceServerResponse {
    private List<IceServer> iceServers;
    private String iceTransportPolicy;
    private boolean turnConfigured;
    private long expiresAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IceServer {
        private List<String> urls;
        private String username;
        private String credential;
    }
}
