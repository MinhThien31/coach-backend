package com.minhthien.web.coach.service;

import com.minhthien.web.coach.dto.request.WebsiteFeedbackRequest;
import com.minhthien.web.coach.entity.User;
import com.minhthien.web.coach.entity.WebsiteFeedback;
import com.minhthien.web.coach.enums.UserRole;
import com.minhthien.web.coach.exception.BadRequestException;
import com.minhthien.web.coach.exception.DuplicateResourceException;
import com.minhthien.web.coach.repository.UserRepository;
import com.minhthien.web.coach.repository.WebsiteFeedbackRepository;
import com.minhthien.web.coach.service.impl.WebsiteFeedbackServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WebsiteFeedbackServiceImplTest {

    @Mock
    private WebsiteFeedbackRepository websiteFeedbackRepository;

    @Mock
    private UserRepository userRepository;

    @Test
    void saveMineCreatesFeedbackOnFirstSubmission() {
        User user = User.builder()
                .id(7L)
                .username("tester")
                .email("tester@example.com")
                .fullName("Test User")
                .role(UserRole.TRAINEES)
                .build();
        WebsiteFeedbackRequest request = new WebsiteFeedbackRequest();
        request.setRating(5);
        request.setComment("  Great platform  ");

        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(websiteFeedbackRepository.findByUserId(7L)).thenReturn(Optional.empty());
        when(websiteFeedbackRepository.save(any(WebsiteFeedback.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        WebsiteFeedbackServiceImpl service =
                new WebsiteFeedbackServiceImpl(websiteFeedbackRepository, userRepository);
        service.saveMine(7L, request);

        ArgumentCaptor<WebsiteFeedback> captor = ArgumentCaptor.forClass(WebsiteFeedback.class);
        verify(websiteFeedbackRepository).save(captor.capture());
        assertThat(captor.getValue().getRating()).isEqualTo(5);
        assertThat(captor.getValue().getComment()).isEqualTo("Great platform");
        assertThat(captor.getValue().getUser()).isEqualTo(user);
    }

    @Test
    void saveMineRejectsSecondSubmissionForSameUser() {
        User user = User.builder()
                .id(7L)
                .username("tester")
                .email("tester@example.com")
                .fullName("Test User")
                .role(UserRole.TRAINEES)
                .build();
        WebsiteFeedback existing = WebsiteFeedback.builder()
                .id(11L)
                .user(user)
                .rating(3)
                .comment("Old")
                .build();
        WebsiteFeedbackRequest request = new WebsiteFeedbackRequest();
        request.setRating(5);

        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(websiteFeedbackRepository.findByUserId(7L)).thenReturn(Optional.of(existing));

        WebsiteFeedbackServiceImpl service =
                new WebsiteFeedbackServiceImpl(websiteFeedbackRepository, userRepository);

        assertThatThrownBy(() -> service.saveMine(7L, request))
                .isInstanceOf(DuplicateResourceException.class);
        verify(websiteFeedbackRepository, never()).save(any(WebsiteFeedback.class));
    }

    @Test
    void saveMineRejectsUnsupportedAdminRole() {
        User user = User.builder()
                .id(9L)
                .username("admin")
                .email("admin@example.com")
                .fullName("Admin User")
                .role(UserRole.ADMIN)
                .build();
        WebsiteFeedbackRequest request = new WebsiteFeedbackRequest();
        request.setRating(4);

        when(userRepository.findById(9L)).thenReturn(Optional.of(user));

        WebsiteFeedbackServiceImpl service =
                new WebsiteFeedbackServiceImpl(websiteFeedbackRepository, userRepository);

        assertThatThrownBy(() -> service.saveMine(9L, request))
                .isInstanceOf(BadRequestException.class);
        verify(websiteFeedbackRepository, never()).save(any(WebsiteFeedback.class));
    }
}
