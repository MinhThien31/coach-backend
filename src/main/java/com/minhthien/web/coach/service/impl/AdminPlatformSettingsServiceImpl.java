package com.minhthien.web.coach.service.impl;

import com.minhthien.web.coach.dto.request.UpdateCommissionSettingsRequest;
import com.minhthien.web.coach.dto.request.UpdateSubscriptionPricesRequest;
import com.minhthien.web.coach.dto.response.AdminCommissionSettingsResponse;
import com.minhthien.web.coach.dto.response.AdminPlatformInfoResponse;
import com.minhthien.web.coach.dto.response.AdminPlatformSettingsResponse;
import com.minhthien.web.coach.dto.response.AdminSubscriptionPricesResponse;
import com.minhthien.web.coach.entity.PlatformSettings;
import com.minhthien.web.coach.repository.PlatformSettingsRepository;
import com.minhthien.web.coach.repository.UserRepository;
import com.minhthien.web.coach.service.AdminPlatformSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminPlatformSettingsServiceImpl implements AdminPlatformSettingsService {

    private static final long SETTINGS_ID = 1L;

    private final PlatformSettingsRepository platformSettingsRepository;
    private final UserRepository userRepository;

    @Value("${app.platform.display-name:CoachFinder}")
    private String platformDisplayName;

    @Value("${app.platform.version:v2.4.1}")
    private String platformVersion;

    @Value("${app.platform.environment:Production}")
    private String platformEnvironment;

    @Value("${app.platform.timezone:Asia/Ho_Chi_Minh (UTC+7)}")
    private String platformTimezone;

    @Value("${app.platform.monthly-uptime:99.98}")
    private Double monthlyUptime;

    @Override
    @Transactional
    public AdminPlatformSettingsResponse getSettings() {
        PlatformSettings settings = getOrCreateSettings();

        return AdminPlatformSettingsResponse.builder()
                .commissionRates(mapCommissionRates(settings))
                .subscriptionPrices(mapSubscriptionPrices(settings))
                .platformInfo(mapPlatformInfo(settings))
                .build();
    }

    @Override
    @Transactional
    public AdminCommissionSettingsResponse updateCommissionSettings(UpdateCommissionSettingsRequest request) {
        PlatformSettings settings = getOrCreateSettings();

        settings.setStarterCommissionRate(request.getStarter());
        settings.setProCoachCommissionRate(request.getProCoach());
        settings.setEliteCoachCommissionRate(request.getEliteCoach());

        PlatformSettings savedSettings = platformSettingsRepository.save(settings);
        return mapCommissionRates(savedSettings);
    }

    @Override
    @Transactional
    public AdminSubscriptionPricesResponse updateSubscriptionPrices(UpdateSubscriptionPricesRequest request) {
        PlatformSettings settings = getOrCreateSettings();

        settings.setTraineeFreePrice(request.getTrainee().getFree());
        settings.setTraineeProPrice(request.getTrainee().getPro());
        settings.setTraineePremiumPrice(request.getTrainee().getPremium());
        settings.setCoachStarterPrice(request.getCoach().getStarter());
        settings.setCoachProPrice(request.getCoach().getProCoach());
        settings.setCoachElitePrice(request.getCoach().getEliteCoach());

        PlatformSettings savedSettings = platformSettingsRepository.save(settings);
        return mapSubscriptionPrices(savedSettings);
    }

    private PlatformSettings getOrCreateSettings() {
        return platformSettingsRepository.findById(SETTINGS_ID)
                .orElseGet(() -> platformSettingsRepository.save(defaultSettings()));
    }

    private PlatformSettings defaultSettings() {
        return PlatformSettings.builder()
                .id(SETTINGS_ID)
                .starterCommissionRate(20)
                .proCoachCommissionRate(12)
                .eliteCoachCommissionRate(0)
                .traineeFreePrice(0L)
                .traineeProPrice(199000L)
                .traineePremiumPrice(499000L)
                .coachStarterPrice(0L)
                .coachProPrice(499000L)
                .coachElitePrice(1490000L)
                .build();
    }

    private AdminCommissionSettingsResponse mapCommissionRates(PlatformSettings settings) {
        return AdminCommissionSettingsResponse.builder()
                .starter(settings.getStarterCommissionRate())
                .proCoach(settings.getProCoachCommissionRate())
                .eliteCoach(settings.getEliteCoachCommissionRate())
                .build();
    }

    private AdminSubscriptionPricesResponse mapSubscriptionPrices(PlatformSettings settings) {
        return AdminSubscriptionPricesResponse.builder()
                .trainee(AdminSubscriptionPricesResponse.TraineePrices.builder()
                        .free(settings.getTraineeFreePrice())
                        .pro(settings.getTraineeProPrice())
                        .premium(settings.getTraineePremiumPrice())
                        .build())
                .coach(AdminSubscriptionPricesResponse.CoachPrices.builder()
                        .starter(settings.getCoachStarterPrice())
                        .proCoach(settings.getCoachProPrice())
                        .eliteCoach(settings.getCoachElitePrice())
                        .build())
                .build();
    }

    private AdminPlatformInfoResponse mapPlatformInfo(PlatformSettings settings) {
        return AdminPlatformInfoResponse.builder()
                .platformName(platformDisplayName)
                .version(platformVersion)
                .environment(platformEnvironment)
                .timezone(platformTimezone)
                .totalUsers(userRepository.count())
                .monthlyUptime(monthlyUptime)
                .lastUpdatedAt(settings.getUpdatedAt())
                .build();
    }
}
