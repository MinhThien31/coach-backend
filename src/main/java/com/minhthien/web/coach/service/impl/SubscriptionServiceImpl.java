package com.minhthien.web.coach.service.impl;

import com.minhthien.web.coach.dto.request.ChangeSubscriptionPlanRequest;
import com.minhthien.web.coach.dto.response.*;
import com.minhthien.web.coach.entity.PlatformSettings;
import com.minhthien.web.coach.entity.TraineeProfile;
import com.minhthien.web.coach.entity.User;
import com.minhthien.web.coach.entity.UserSubscription;
import com.minhthien.web.coach.enums.SubscriptionBillingCycle;
import com.minhthien.web.coach.enums.SubscriptionPlanCode;
import com.minhthien.web.coach.enums.UserRole;
import com.minhthien.web.coach.exception.BadRequestException;
import com.minhthien.web.coach.exception.ResourceNotFoundException;
import com.minhthien.web.coach.repository.PlatformSettingsRepository;
import com.minhthien.web.coach.repository.TraineeProfileRepository;
import com.minhthien.web.coach.repository.UserRepository;
import com.minhthien.web.coach.repository.UserSubscriptionRepository;
import com.minhthien.web.coach.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private static final long PLATFORM_SETTINGS_ID = 1L;
    private static final int YEARLY_DISCOUNT_MONTHS = 2;

    private final UserRepository userRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final PlatformSettingsRepository platformSettingsRepository;
    private final TraineeProfileRepository traineeProfileRepository;

    @Override
    @Transactional(readOnly = true)
    public SubscriptionCatalogResponse getTraineeCatalog(Long currentUserId, SubscriptionBillingCycle billingCycle) {
        User user = getCurrentUser(currentUserId);
        validateUserRole(user, UserRole.TRAINEES);

        CurrentSubscriptionResponse currentSubscription = buildCurrentSubscription(user);

        return SubscriptionCatalogResponse.builder()
                .audience("TRAINEE")
                .pageTitle("Chọn gói phù hợp với bạn")
                .pageSubtitle("Nâng cấp bất kỳ lúc nào · Huỷ dễ dàng · Không ràng buộc")
                .selectedBillingCycle(resolveBillingCycle(billingCycle))
                .yearlyDiscountMonths(YEARLY_DISCOUNT_MONTHS)
                .currentSubscription(currentSubscription)
                .plans(buildTraineePlans(user, resolveBillingCycle(billingCycle)))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public SubscriptionCatalogResponse getCoachCatalog(Long currentUserId, SubscriptionBillingCycle billingCycle) {
        User user = getCurrentUser(currentUserId);
        validateUserRole(user, UserRole.COACHES);

        CurrentSubscriptionResponse currentSubscription = buildCurrentSubscription(user);

        return SubscriptionCatalogResponse.builder()
                .audience("COACH")
                .pageTitle("Chọn gói phù hợp với bạn")
                .pageSubtitle("Nâng cấp gói để mở khóa thêm tính năng và tăng cơ hội tiếp cận học viên")
                .selectedBillingCycle(resolveBillingCycle(billingCycle))
                .yearlyDiscountMonths(YEARLY_DISCOUNT_MONTHS)
                .currentSubscription(currentSubscription)
                .plans(buildCoachPlans(user, resolveBillingCycle(billingCycle)))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public CurrentSubscriptionResponse getCurrentSubscription(Long currentUserId) {
        User user = getCurrentUser(currentUserId);
        validateSupportedRole(user);
        return buildCurrentSubscription(user);
    }

    @Override
    @Transactional
    public SubscriptionChangeResponse changePlan(Long currentUserId, ChangeSubscriptionPlanRequest request) {
        User user = getCurrentUser(currentUserId);
        validateSupportedRole(user);
        validatePlanForUserRole(user.getRole(), request.getPlanCode());

        PlatformSettings settings = getOrCreateSettings();
        SubscriptionBillingCycle billingCycle = resolveBillingCycle(request.getBillingCycle());
        Long monthlyPrice = getMonthlyPrice(user.getRole(), request.getPlanCode(), settings);
        Long billingPrice = calculateBillingPrice(monthlyPrice, billingCycle);

        UserSubscription subscription = userSubscriptionRepository.findByUserId(user.getId())
                .orElseGet(() -> UserSubscription.builder()
                        .user(user)
                        .build());

        subscription.setPlanCode(request.getPlanCode());
        subscription.setBillingCycle(billingCycle);
        subscription.setMonthlyPriceSnapshot(monthlyPrice);
        subscription.setBillingPriceSnapshot(billingPrice);
        subscription.setActive(true);
        subscription.setStartedAt(LocalDateTime.now());
        subscription.setExpiresAt(calculateExpiryDate(request.getPlanCode(), billingCycle));

        UserSubscription savedSubscription = userSubscriptionRepository.save(subscription);

        return SubscriptionChangeResponse.builder()
                .message(buildChangeMessage(user.getRole(), request.getPlanCode()))
                .subscription(buildCurrentSubscription(user, savedSubscription))
                .build();
    }

    @Override
    @Transactional
    public SubscriptionChangeResponse changeCoachPlan(Long currentUserId, ChangeSubscriptionPlanRequest request) {
        User user = getCurrentUser(currentUserId);
        validateUserRole(user, UserRole.COACHES);
        return changePlan(currentUserId, request);
    }


    private List<SubscriptionPlanCardResponse> buildTraineePlans(User user, SubscriptionBillingCycle billingCycle) {
        PlatformSettings settings = getOrCreateSettings();
        CurrentSubscriptionResponse currentSubscription = buildCurrentSubscription(user);

        return List.of(
                buildPlanCard(
                        user.getRole(),
                        SubscriptionPlanCode.FREE,
                        "Gói Thường",
                        "Phù hợp cho người mới bắt đầu, chỉ cần các tính năng cơ bản.",
                        getMonthlyPrice(user.getRole(), SubscriptionPlanCode.FREE, settings),
                        billingCycle,
                        currentSubscription,
                        false,
                        null,
                        List.of(
                                feature("Tạo tài khoản học viên", true),
                                feature("Xem hồ sơ HLV", true),
                                feature("Tìm kiếm và lọc HLV theo môn học, trình độ, mục tiêu", true),
                                feature("Đặt lịch học", true),
                                feature("Lưu HLV yêu thích", true),
                                feature("Xem lịch sử buổi học", true),
                                feature("Nhận hỗ trợ cơ bản từ hệ thống", true)
                        )
                ),
                buildPlanCard(
                        user.getRole(),
                        SubscriptionPlanCode.PRO,
                        "Gói Pro",
                        "Phù hợp cho học viên muốn học nghiêm túc và cải thiện nhanh hơn.",
                        getMonthlyPrice(user.getRole(), SubscriptionPlanCode.PRO, settings),
                        billingCycle,
                        currentSubscription,
                        true,
                        "Phổ biến nhất",
                        List.of(
                                feature("Bao gồm toàn bộ quyền lợi gói thường", true),
                                feature("Upload video để nhận đánh giá", true),
                                feature("AI feedback kỹ thuật chi tiết hơn", true),
                                feature("Theo dõi tiến bộ theo thời gian", true),
                                feature("Gợi ý lỗi sai và cách sửa", true),
                                feature("Lộ trình luyện tập cá nhân hóa", true),
                                feature("Ưu tiên hỗ trợ", true),
                                feature("Ưu tiên ghép với HLV phù hợp hơn", true)
                        )
                )
        );
    }

    private List<SubscriptionPlanCardResponse> buildCoachPlans(User user, SubscriptionBillingCycle billingCycle) {
        PlatformSettings settings = getOrCreateSettings();
        CurrentSubscriptionResponse currentSubscription = buildCurrentSubscription(user);

        return List.of(
                buildPlanCard(
                        user.getRole(),
                        SubscriptionPlanCode.FREE,
                        "Gói Thường",
                        "Phù hợp cho HLV mới tham gia nền tảng.",
                        getMonthlyPrice(user.getRole(), SubscriptionPlanCode.FREE, settings),
                        billingCycle,
                        currentSubscription,
                        false,
                        null,
                        List.of(
                                feature("Tạo hồ sơ HLV", true),
                                feature("Đăng thông tin cá nhân, kinh nghiệm, môn dạy", true),
                                feature("Nhận booking từ học viên", true),
                                feature("Quản lý lịch dạy cơ bản", true),
                                feature("Quản lý danh sách học viên cơ bản", true),
                                feature("Xem đánh giá từ học viên", true),
                                feature("Upload video 360° không giới hạn", false),
                                feature("Công cụ quản lý học viên nâng cao", false),
                                feature("Theo dõi tiến bộ của học viên", false),
                                feature("Xem thống kê lượt xem hồ sơ, lượt booking", false),
                                feature("Ưu tiên hiển thị trong kết quả tìm kiếm", false),
                                feature("Dashboard doanh thu và hiệu suất", false)
                        )
                ),
                buildPlanCard(
                        user.getRole(),
                        SubscriptionPlanCode.PRO,
                        "Gói Pro",
                        "Phù hợp cho HLV muốn tăng độ chuyên nghiệp và thu hút thêm học viên.",
                        getMonthlyPrice(user.getRole(), SubscriptionPlanCode.PRO, settings),
                        billingCycle,
                        currentSubscription,
                        true,
                        currentSubscription.getPlanCode() == SubscriptionPlanCode.PRO ? "Đang dùng" : null,
                        List.of(
                                feature("Bao gồm toàn bộ quyền lợi gói thường", true),
                                feature("Hồ sơ nổi bật hơn trên nền tảng", true),
                                feature("Upload video 360° không giới hạn", true),
                                feature("Công cụ quản lý học viên tốt hơn", true),
                                feature("Theo dõi tiến bộ của học viên", true),
                                feature("Xem thống kê lượt xem hồ sơ, lượt booking", true),
                                feature("Hỗ trợ xây dựng hình ảnh chuyên nghiệp", true),
                                feature("Ưu tiên hiển thị trong kết quả tìm kiếm", true),
                                feature("Gắn nhãn HLV nổi bật / xác minh", true),
                                feature("Dashboard doanh thu và hiệu suất chi tiết", false),
                                feature("Quản lý lịch dạy và nhắc lịch tự động", false),
                                feature("Hỗ trợ thương hiệu cá nhân nâng cao", false)
                        )
                ),
                buildPlanCard(
                        user.getRole(),
                        SubscriptionPlanCode.PREMIUM,
                        "Gói Premium",
                        "Phù hợp cho HLV muốn tối đa hóa doanh thu và thương hiệu cá nhân.",
                        getMonthlyPrice(user.getRole(), SubscriptionPlanCode.PREMIUM, settings),
                        billingCycle,
                        currentSubscription,
                        true,
                        currentSubscription.getPlanCode() == SubscriptionPlanCode.PREMIUM ? "Đang dùng" : "Đề xuất",
                        List.of(
                                feature("Bao gồm toàn bộ quyền lợi gói Pro", true),
                                feature("Ưu tiên hiển thị cao hơn trong kết quả tìm kiếm", true),
                                feature("Gắn nhãn HLV nổi bật / xác minh", true),
                                feature("Dashboard doanh thu và hiệu suất chi tiết", true),
                                feature("Quản lý học viên nâng cao", true),
                                feature("Quản lý lịch dạy và nhắc lịch tự động", true),
                                feature("Ưu tiên tiếp cận học viên tiềm năng", true),
                                feature("Hỗ trợ thương hiệu cá nhân trên nền tảng ở mức cao hơn", true)
                        )
                )
        );
    }

    private SubscriptionPlanCardResponse buildPlanCard(
            UserRole userRole,
            SubscriptionPlanCode planCode,
            String displayName,
            String description,
            Long monthlyPrice,
            SubscriptionBillingCycle billingCycle,
            CurrentSubscriptionResponse currentSubscription,
            boolean highlighted,
            String ribbonText,
            List<SubscriptionFeatureResponse> features
    ) {
        Long billingPrice = calculateBillingPrice(monthlyPrice, billingCycle);

        return SubscriptionPlanCardResponse.builder()
                .planCode(planCode)
                .displayName(displayName)
                .description(description)
                .monthlyPrice(monthlyPrice)
                .billingPrice(billingPrice)
                .displayPrice(formatPrice(billingPrice))
                .billingLabel(buildBillingLabel(billingPrice, billingCycle))
                .highlighted(highlighted)
                .ribbonText(ribbonText)
                .current(currentSubscription.getPlanCode() == planCode)
                .exactCurrent(currentSubscription.getPlanCode() == planCode && currentSubscription.getBillingCycle() == billingCycle)
                .actionLabel(buildActionLabel(userRole, currentSubscription.getPlanCode(), currentSubscription.getBillingCycle(), planCode, billingCycle))
                .actionType(buildActionType(currentSubscription.getPlanCode(), currentSubscription.getBillingCycle(), planCode, billingCycle))
                .features(features)
                .build();
    }

    private CurrentSubscriptionResponse buildCurrentSubscription(User user) {
        UserSubscription subscription = userSubscriptionRepository.findByUserId(user.getId())
                .orElseGet(() -> buildDefaultSubscription(user));
        return buildCurrentSubscription(user, subscription);
    }

    private CurrentSubscriptionResponse buildCurrentSubscription(User user, UserSubscription subscription) {
        PlatformSettings settings = getOrCreateSettings();
        SubscriptionPlanCode planCode = subscription.getPlanCode() == null ? SubscriptionPlanCode.FREE : subscription.getPlanCode();
        SubscriptionBillingCycle billingCycle = resolveBillingCycle(subscription.getBillingCycle());

        Long monthlyPrice = Optional.ofNullable(subscription.getMonthlyPriceSnapshot())
                .orElseGet(() -> getMonthlyPrice(user.getRole(), planCode, settings));
        Long billingPrice = Optional.ofNullable(subscription.getBillingPriceSnapshot())
                .orElseGet(() -> calculateBillingPrice(monthlyPrice, billingCycle));

        return CurrentSubscriptionResponse.builder()
                .planCode(planCode)
                .displayName(getDisplayName(user.getRole(), planCode))
                .statusLabel("Gói hiện tại")
                .note(buildCurrentPlanNote(user.getRole(), planCode))
                .monthlyPrice(monthlyPrice)
                .billingPrice(billingPrice)
                .displayPrice(formatPrice(billingPrice))
                .billingLabel(buildBillingLabel(billingPrice, billingCycle))
                .billingCycle(billingCycle)
                .streakDays(user.getRole() == UserRole.TRAINEES ? calculateTraineeStreakDays(user.getId(), user.getCreatedAt()) : null)
                .startedAt(subscription.getStartedAt())
                .expiresAt(subscription.getExpiresAt())
                .build();
    }

    private UserSubscription buildDefaultSubscription(User user) {
        PlatformSettings settings = getOrCreateSettings();
        Long monthlyPrice = getMonthlyPrice(user.getRole(), SubscriptionPlanCode.FREE, settings);

        return UserSubscription.builder()
                .user(user)
                .planCode(SubscriptionPlanCode.FREE)
                .billingCycle(SubscriptionBillingCycle.MONTHLY)
                .monthlyPriceSnapshot(monthlyPrice)
                .billingPriceSnapshot(monthlyPrice)
                .active(true)
                .startedAt(user.getCreatedAt())
                .expiresAt(null)
                .build();
    }

    private User getCurrentUser(Long currentUserId) {
        return userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUserId));
    }

    private void validateSupportedRole(User user) {
        if (user.getRole() != UserRole.TRAINEES && user.getRole() != UserRole.COACHES) {
            throw new BadRequestException("This account does not support subscription plans");
        }
    }

    private void validateUserRole(User user, UserRole expectedRole) {
        validateSupportedRole(user);
        if (user.getRole() != expectedRole) {
            throw new BadRequestException("This subscription page is not available for your role");
        }
    }

    private void validatePlanForUserRole(UserRole userRole, SubscriptionPlanCode planCode) {
        if (userRole == UserRole.TRAINEES && planCode == SubscriptionPlanCode.PREMIUM) {
            throw new BadRequestException("Trainee accounts only support FREE and PRO plans");
        }
        if (userRole != UserRole.TRAINEES && userRole != UserRole.COACHES) {
            throw new BadRequestException("This account does not support subscription plans");
        }
    }

    private PlatformSettings getOrCreateSettings() {
        return platformSettingsRepository.findById(PLATFORM_SETTINGS_ID)
                .orElseGet(() -> platformSettingsRepository.save(defaultSettings()));
    }

    private PlatformSettings defaultSettings() {
        return PlatformSettings.builder()
                .id(PLATFORM_SETTINGS_ID)
                .starterCommissionRate(20)
                .proCoachCommissionRate(12)
                .eliteCoachCommissionRate(0)
                .traineeFreePrice(0L)
                .traineeProPrice(99000L)
                .traineePremiumPrice(199000L)
                .coachStarterPrice(0L)
                .coachProPrice(200000L)
                .coachElitePrice(400000L)
                .build();
    }

    private Long getMonthlyPrice(UserRole userRole, SubscriptionPlanCode planCode, PlatformSettings settings) {
        if (userRole == UserRole.TRAINEES) {
            return switch (planCode) {
                case FREE -> settings.getTraineeFreePrice();
                case PRO -> settings.getTraineeProPrice();
                case PREMIUM -> settings.getTraineePremiumPrice();
            };
        }

        if (userRole == UserRole.COACHES) {
            return switch (planCode) {
                case FREE -> settings.getCoachStarterPrice();
                case PRO -> settings.getCoachProPrice();
                case PREMIUM -> settings.getCoachElitePrice();
            };
        }

        throw new BadRequestException("This account does not support subscription plans");
    }

    private Long calculateBillingPrice(Long monthlyPrice, SubscriptionBillingCycle billingCycle) {
        if (monthlyPrice == null || monthlyPrice <= 0) {
            return 0L;
        }

        if (billingCycle == SubscriptionBillingCycle.YEARLY) {
            return monthlyPrice * (12 - YEARLY_DISCOUNT_MONTHS);
        }

        return monthlyPrice;
    }

    private LocalDateTime calculateExpiryDate(SubscriptionPlanCode planCode, SubscriptionBillingCycle billingCycle) {
        if (planCode == SubscriptionPlanCode.FREE) {
            return null;
        }

        return billingCycle == SubscriptionBillingCycle.YEARLY
                ? LocalDateTime.now().plusYears(1)
                : LocalDateTime.now().plusMonths(1);
    }

    private SubscriptionBillingCycle resolveBillingCycle(SubscriptionBillingCycle billingCycle) {
        return billingCycle == null ? SubscriptionBillingCycle.MONTHLY : billingCycle;
    }

    private String getDisplayName(UserRole userRole, SubscriptionPlanCode planCode) {
        if (userRole == UserRole.TRAINEES) {
            return switch (planCode) {
                case FREE -> "Gói Thường";
                case PRO -> "Gói Pro";
                case PREMIUM -> "Gói Premium";
            };
        }

        if (userRole == UserRole.COACHES) {
            return switch (planCode) {
                case FREE -> "Gói Thường";
                case PRO -> "Gói Pro";
                case PREMIUM -> "Gói Premium";
            };
        }

        throw new BadRequestException("This account does not support subscription plans");
    }

    private String buildCurrentPlanNote(UserRole userRole, SubscriptionPlanCode planCode) {
        if (userRole == UserRole.TRAINEES) {
            return switch (planCode) {
                case FREE -> "Bạn đang dùng gói Thường. Nâng cấp lên Pro để mở khoá AI feedback và video phân tích kỹ thuật!";
                case PRO -> "Bạn đang dùng gói Pro. Tiếp tục duy trì để sử dụng AI feedback và lộ trình luyện tập cá nhân hóa.";
                case PREMIUM -> "Bạn đang dùng gói Premium với đầy đủ quyền lợi học tập nâng cao.";
            };
        }

        return switch (planCode) {
            case FREE -> "Bạn đang dùng gói Thường. Nâng cấp để hồ sơ nổi bật hơn và nhận thêm học viên.";
            case PRO -> "Bạn đang dùng gói Pro. Nâng cấp Premium để mở dashboard doanh thu và ưu tiên hiển thị cao hơn.";
            case PREMIUM -> "Bạn đang dùng gói Premium với đầy đủ công cụ phát triển thương hiệu cá nhân.";
        };
    }

    private String buildActionLabel(
            UserRole userRole,
            SubscriptionPlanCode currentPlanCode,
            SubscriptionBillingCycle currentBillingCycle,
            SubscriptionPlanCode targetPlanCode,
            SubscriptionBillingCycle targetBillingCycle
    ) {
        if (currentPlanCode == targetPlanCode && currentBillingCycle == targetBillingCycle) {
            return "✓ Gói hiện tại";
        }

        if (currentPlanCode == targetPlanCode) {
            return targetBillingCycle == SubscriptionBillingCycle.YEARLY ? "Chuyển sang năm" : "Chuyển sang tháng";
        }

        if (getPlanRank(targetPlanCode) < getPlanRank(currentPlanCode)) {
            return "Hạ cấp";
        }

        return switch (targetPlanCode) {
            case FREE -> "Chọn gói Thường";
            case PRO -> "Nâng cấp Pro";
            case PREMIUM -> userRole == UserRole.COACHES ? "Nâng lên Premium" : "Nâng cấp Premium";
        };
    }

    private String buildActionType(
            SubscriptionPlanCode currentPlanCode,
            SubscriptionBillingCycle currentBillingCycle,
            SubscriptionPlanCode targetPlanCode,
            SubscriptionBillingCycle targetBillingCycle
    ) {
        if (currentPlanCode == targetPlanCode && currentBillingCycle == targetBillingCycle) {
            return "CURRENT";
        }

        if (currentPlanCode == targetPlanCode) {
            return "CHANGE_CYCLE";
        }

        return getPlanRank(targetPlanCode) < getPlanRank(currentPlanCode) ? "DOWNGRADE" : "UPGRADE";
    }

    private int getPlanRank(SubscriptionPlanCode planCode) {
        return switch (planCode) {
            case FREE -> 0;
            case PRO -> 1;
            case PREMIUM -> 2;
        };
    }

    private String buildChangeMessage(UserRole userRole, SubscriptionPlanCode planCode) {
        if (userRole == UserRole.TRAINEES && planCode == SubscriptionPlanCode.PRO) {
            return "Đã cập nhật gói học viên sang Pro";
        }
        if (userRole == UserRole.TRAINEES && planCode == SubscriptionPlanCode.FREE) {
            return "Đã chuyển về gói Thường cho học viên";
        }
        if (userRole == UserRole.COACHES && planCode == SubscriptionPlanCode.FREE) {
            return "Đã chuyển HLV về gói Thường";
        }
        if (userRole == UserRole.COACHES && planCode == SubscriptionPlanCode.PRO) {
            return "Đã cập nhật gói HLV sang Pro";
        }
        return "Đã cập nhật gói HLV sang Premium";
    }

    private String formatPrice(Long amount) {
        if (amount == null || amount <= 0) {
            return "Miễn phí";
        }

        NumberFormat numberFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
        numberFormat.setGroupingUsed(true);
        return numberFormat.format(amount) + "đ";
    }

    private String buildBillingLabel(Long billingPrice, SubscriptionBillingCycle billingCycle) {
        if (billingPrice == null || billingPrice <= 0) {
            return "";
        }
        return billingCycle == SubscriptionBillingCycle.YEARLY ? "/năm" : "/tháng";
    }

    private Integer calculateTraineeStreakDays(Long userId, LocalDateTime fallbackCreatedAt) {
        Optional<TraineeProfile> traineeProfileOptional = traineeProfileRepository.findByUserId(userId);
        LocalDate startDate = traineeProfileOptional
                .map(TraineeProfile::getJoinedDate)
                .orElseGet(() -> fallbackCreatedAt != null ? fallbackCreatedAt.toLocalDate() : LocalDate.now());

        long days = ChronoUnit.DAYS.between(startDate, LocalDate.now()) + 1;
        return (int) Math.max(days, 1);
    }

    private SubscriptionFeatureResponse feature(String text, boolean included) {
        return SubscriptionFeatureResponse.builder()
                .text(text)
                .included(included)
                .build();
    }
}
