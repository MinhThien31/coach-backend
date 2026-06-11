package com.minhthien.web.coach.service;

import com.minhthien.web.coach.dto.response.WebRtcIceServerResponse;
import com.minhthien.web.coach.entity.User;

public interface WebRtcService {
    WebRtcIceServerResponse getIceServers(User currentUser);
}
