package com.minhthien.web.coach.service;

import com.minhthien.web.coach.dto.request.GymOwnerRequests;
import com.minhthien.web.coach.dto.response.GymOwnerResponses;
import com.minhthien.web.coach.dto.response.WalletHistoryItemResponse;
import com.minhthien.web.coach.dto.response.WalletResponse;
import com.minhthien.web.coach.enums.GymProfileStatus;
import org.springframework.data.domain.Page;

import java.util.List;

public interface GymOwnerService {
    GymOwnerResponses.GymOverviewResponse getOverview(Long ownerUserId);
    GymOwnerResponses.GymProfileResponse getProfile(Long ownerUserId);
    GymOwnerResponses.GymProfileResponse updateProfile(Long ownerUserId, GymOwnerRequests.GymProfileUpdateRequest request);
    List<GymOwnerResponses.GymCoachResponse> getCoaches(Long ownerUserId);
    GymOwnerResponses.GymCoachResponse addCoach(Long ownerUserId, GymOwnerRequests.GymCoachAddRequest request);
    GymOwnerResponses.GymCoachResponse removeCoach(Long ownerUserId, Long coachProfileId);
    List<GymOwnerResponses.GymBookingResponse> getBookings(Long ownerUserId);
    WalletResponse getWallet(Long ownerUserId);
    Page<WalletHistoryItemResponse> getTransactions(Long ownerUserId, int page, int size);

    List<GymOwnerResponses.GymProfileResponse> getAdminGyms(GymProfileStatus status);
    GymOwnerResponses.GymProfileResponse getAdminGym(Long gymId);
    GymOwnerResponses.GymProfileResponse updateAdminGymStatus(Long gymId, GymOwnerRequests.AdminGymStatusUpdateRequest request);
    GymOwnerResponses.GymCoachResponse adminRemoveCoach(Long gymId, Long coachProfileId);
}
