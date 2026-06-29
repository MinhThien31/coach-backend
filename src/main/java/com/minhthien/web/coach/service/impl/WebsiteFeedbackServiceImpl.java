package com.minhthien.web.coach.service.impl;

import com.minhthien.web.coach.dto.request.WebsiteFeedbackRequest;
import com.minhthien.web.coach.dto.response.WebsiteFeedbackResponse;
import com.minhthien.web.coach.entity.User;
import com.minhthien.web.coach.entity.WebsiteFeedback;
import com.minhthien.web.coach.enums.UserRole;
import com.minhthien.web.coach.exception.BadRequestException;
import com.minhthien.web.coach.exception.DuplicateResourceException;
import com.minhthien.web.coach.exception.ResourceNotFoundException;
import com.minhthien.web.coach.repository.UserRepository;
import com.minhthien.web.coach.repository.WebsiteFeedbackRepository;
import com.minhthien.web.coach.service.WebsiteFeedbackService;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class WebsiteFeedbackServiceImpl implements WebsiteFeedbackService {

    private final WebsiteFeedbackRepository websiteFeedbackRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public WebsiteFeedbackResponse getMine(Long userId) {
        return websiteFeedbackRepository.findByUserId(userId)
                .map(this::map)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
                    return WebsiteFeedbackResponse.builder()
                            .userId(user.getId())
                            .username(user.getUsername())
                            .fullName(user.getFullName())
                            .email(user.getEmail())
                            .role(user.getRole())
                            .build();
                });
    }

    @Override
    @Transactional
    public WebsiteFeedbackResponse saveMine(Long userId, WebsiteFeedbackRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        if (user.getRole() != UserRole.TRAINEES && user.getRole() != UserRole.COACHES) {
            throw new BadRequestException("Chỉ học viên và huấn luyện viên mới có thể gửi đánh giá website");
        }
        if (websiteFeedbackRepository.findByUserId(userId).isPresent()) {
            throw new DuplicateResourceException("Bạn đã gửi đánh giá rồi, không thể đánh giá lần thứ hai.");
        }

        WebsiteFeedback feedback = WebsiteFeedback.builder().user(user).build();
        feedback.setRating(request.getRating());
        feedback.setComment(normalizeComment(request.getComment()));
        try {
            return map(websiteFeedbackRepository.save(feedback));
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateResourceException("Bạn đã gửi đánh giá rồi, không thể đánh giá lần thứ hai.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WebsiteFeedbackResponse> getAll(
            String keyword,
            Integer rating,
            UserRole role,
            LocalDate from,
            LocalDate to,
            int page,
            int size
    ) {
        Specification<WebsiteFeedback> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            Join<WebsiteFeedback, User> user = root.join("user");

            if (rating != null) {
                predicates.add(cb.equal(root.get("rating"), rating));
            }
            if (role != null) {
                predicates.add(cb.equal(user.get("role"), role));
            }
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("updatedAt"), from.atStartOfDay()));
            }
            if (to != null) {
                predicates.add(cb.lessThan(root.get("updatedAt"), to.plusDays(1).atStartOfDay()));
            }
            if (StringUtils.hasText(keyword)) {
                String pattern = "%" + keyword.trim().toLowerCase(Locale.ROOT) + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(user.get("username")), pattern),
                        cb.like(cb.lower(user.get("email")), pattern),
                        cb.like(cb.lower(user.get("fullName")), pattern),
                        cb.like(cb.lower(root.get("comment")), pattern)
                ));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };

        PageRequest pageRequest = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), 100),
                Sort.by(Sort.Direction.DESC, "updatedAt")
        );
        return websiteFeedbackRepository.findAll(specification, pageRequest).map(this::map);
    }

    private WebsiteFeedbackResponse map(WebsiteFeedback feedback) {
        User user = feedback.getUser();
        return WebsiteFeedbackResponse.builder()
                .id(feedback.getId())
                .userId(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .rating(feedback.getRating())
                .comment(feedback.getComment())
                .createdAt(feedback.getCreatedAt())
                .updatedAt(feedback.getUpdatedAt())
                .build();
    }

    private String normalizeComment(String comment) {
        return StringUtils.hasText(comment) ? comment.trim() : null;
    }
}
