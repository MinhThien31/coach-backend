package com.minhthien.web.coach.service.impl;

import com.minhthien.web.coach.dto.request.CoachWorkspaceRequests;
import com.minhthien.web.coach.dto.response.CoachWorkspaceResponses;
import com.minhthien.web.coach.dto.response.WalletTransactionResponse;
import com.minhthien.web.coach.entity.Booking;
import com.minhthien.web.coach.entity.CoachProfile;
import com.minhthien.web.coach.entity.CoachStudentNote;
import com.minhthien.web.coach.entity.CoachStudentTask;
import com.minhthien.web.coach.entity.TraineeProfile;
import com.minhthien.web.coach.entity.TraineeSubmission;
import com.minhthien.web.coach.entity.User;
import com.minhthien.web.coach.entity.Wallet;
import com.minhthien.web.coach.entity.WalletTransaction;
import com.minhthien.web.coach.enums.BookingStatus;
import com.minhthien.web.coach.enums.SubmissionStatus;
import com.minhthien.web.coach.enums.VideoType;
import com.minhthien.web.coach.enums.WalletTransactionType;
import com.minhthien.web.coach.enums.WalletWithdrawalStatus;
import com.minhthien.web.coach.exception.ResourceNotFoundException;
import com.minhthien.web.coach.exception.UnauthorizedException;
import com.minhthien.web.coach.repository.BookingRepository;
import com.minhthien.web.coach.repository.CoachRepository;
import com.minhthien.web.coach.repository.CoachStudentNoteRepository;
import com.minhthien.web.coach.repository.CoachStudentTaskRepository;
import com.minhthien.web.coach.repository.CoachVideoRepository;
import com.minhthien.web.coach.repository.ReviewRepository;
import com.minhthien.web.coach.repository.TraineeProfileRepository;
import com.minhthien.web.coach.repository.TraineeSubmissionRepository;
import com.minhthien.web.coach.repository.WalletRepository;
import com.minhthien.web.coach.repository.WalletTransactionRepository;
import com.minhthien.web.coach.service.CoachWorkspaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CoachWorkspaceServiceImpl implements CoachWorkspaceService {

    private final CoachRepository coachRepository;
    private final TraineeProfileRepository traineeProfileRepository;
    private final BookingRepository bookingRepository;
    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final CoachVideoRepository coachVideoRepository;
    private final TraineeSubmissionRepository traineeSubmissionRepository;
    private final ReviewRepository reviewRepository;
    private final CoachStudentTaskRepository taskRepository;
    private final CoachStudentNoteRepository noteRepository;

    @Override
    @Transactional(readOnly = true)
    public CoachWorkspaceResponses.IncomeOverviewResponse getIncomeOverview(Long currentUserId) {
        CoachProfile coach = getCoach(currentUserId);
        List<WalletTransaction> transactions = getCoachWalletTransactions(currentUserId);
        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate weekStart = today.minusDays(6);

        return CoachWorkspaceResponses.IncomeOverviewResponse.builder()
                .monthRevenue(sumTransactions(transactions, WalletTransactionType.BOOKING_COACH_PAYOUT, monthStart, today.plusDays(1)))
                .weekRevenue(sumTransactions(transactions, WalletTransactionType.BOOKING_COACH_PAYOUT, weekStart, today.plusDays(1)))
                .totalRevenue(sumTransactions(transactions, WalletTransactionType.BOOKING_COACH_PAYOUT, null, null))
                .availableBalance(walletRepository.findByUserId(currentUserId).map(Wallet::getBalance).orElse(0L))
                .pendingWithdrawals(sumPendingWithdrawals(transactions))
                .platformCommission(bookingRepository.findByCoachId(coach.getId()).stream().mapToLong(b -> nullToZero(b.getAdminCommissionAmount())).sum())
                .completedBookings(bookingRepository.countByCoachIdAndStatus(coach.getId(), BookingStatus.COMPLETED))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<WalletTransactionResponse> getIncomeTransactions(Long currentUserId) {
        return getCoachWalletTransactions(currentUserId).stream()
                .map(this::mapWalletTransaction)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CoachWorkspaceResponses.ChartPointResponse> getMonthlyChart(Long currentUserId) {
        List<WalletTransaction> transactions = getCoachWalletTransactions(currentUserId);
        YearMonth now = YearMonth.now();
        return java.util.stream.IntStream.rangeClosed(0, 5)
                .mapToObj(i -> now.minusMonths(5 - i))
                .map(month -> {
                    long value = sumTransactions(
                            transactions,
                            WalletTransactionType.BOOKING_COACH_PAYOUT,
                            month.atDay(1),
                            month.plusMonths(1).atDay(1)
                    );
                    return CoachWorkspaceResponses.ChartPointResponse.builder()
                            .period(month.toString())
                            .value(value)
                            .count(countTransactions(transactions, WalletTransactionType.BOOKING_COACH_PAYOUT, month.atDay(1), month.plusMonths(1).atDay(1)))
                            .build();
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CoachWorkspaceResponses.TopStudentResponse> getTopStudents(Long currentUserId) {
        CoachProfile coach = getCoach(currentUserId);
        return bookingRepository.findByCoachId(coach.getId()).stream()
                .collect(Collectors.groupingBy(Booking::getTrainee))
                .entrySet()
                .stream()
                .map(entry -> CoachWorkspaceResponses.TopStudentResponse.builder()
                        .traineeId(resolveTraineeProfileId(entry.getKey().getId()))
                        .traineeName(entry.getKey().getFullName())
                        .sessions(entry.getValue().size())
                        .revenue(entry.getValue().stream().mapToLong(this::bookingAmount).sum())
                        .build())
                .sorted(Comparator.comparingLong(CoachWorkspaceResponses.TopStudentResponse::getRevenue).reversed())
                .limit(10)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<WalletTransactionResponse> getPayouts(Long currentUserId) {
        return getCoachWalletTransactions(currentUserId).stream()
                .filter(t -> t.getType() == WalletTransactionType.WITHDRAWAL || t.getType() == WalletTransactionType.BOOKING_COACH_PAYOUT)
                .map(this::mapWalletTransaction)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CoachWorkspaceResponses.AnalyticsOverviewResponse getAnalyticsOverview(Long currentUserId) {
        CoachProfile coach = getCoach(currentUserId);
        List<Booking> bookings = bookingRepository.findByCoachId(coach.getId());
        long totalViews = Objects.requireNonNullElse(coachVideoRepository.getTotalViewsByCoach(currentUserId), 0L);
        Double rating = reviewRepository.getAverageRating(coach.getId());

        return CoachWorkspaceResponses.AnalyticsOverviewResponse.builder()
                .totalBookings(bookings.size())
                .pendingBookings(countBookings(bookings, BookingStatus.PENDING))
                .confirmedBookings(countBookings(bookings, BookingStatus.CONFIRMED))
                .completedBookings(countBookings(bookings, BookingStatus.COMPLETED))
                .totalStudents(bookings.stream().map(b -> b.getTrainee().getId()).distinct().count())
                .totalRevenue(bookings.stream().mapToLong(this::bookingAmount).sum())
                .totalVideos(coachVideoRepository.countByCoachId(currentUserId))
                .totalVideoViews(totalViews)
                .averageRating(rating == null ? 0.0 : rating)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CoachWorkspaceResponses.ChartPointResponse> getBookingAnalytics(Long currentUserId) {
        CoachProfile coach = getCoach(currentUserId);
        List<Booking> bookings = bookingRepository.findByCoachId(coach.getId());
        YearMonth now = YearMonth.now();
        return java.util.stream.IntStream.rangeClosed(0, 5)
                .mapToObj(i -> now.minusMonths(5 - i))
                .map(month -> CoachWorkspaceResponses.ChartPointResponse.builder()
                        .period(month.toString())
                        .value(bookings.stream().filter(b -> inMonth(b.getStartDate(), month)).count())
                        .count(bookings.stream().filter(b -> inMonth(b.getStartDate(), month)).count())
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CoachWorkspaceResponses.ChartPointResponse> getRevenueAnalytics(Long currentUserId) {
        return getMonthlyChart(currentUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CoachWorkspaceResponses.StudentProgressResponse> getStudentsProgress(Long currentUserId) {
        return getStudents(currentUserId).stream()
                .map(student -> getStudentProgress(currentUserId, student.getTraineeId()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CoachWorkspaceResponses.ChartPointResponse> getVideoAnalytics(Long currentUserId) {
        return coachVideoRepository.findByCoachId(currentUserId).stream()
                .map(video -> CoachWorkspaceResponses.ChartPointResponse.builder()
                        .period(video.getTitle())
                        .value(Objects.requireNonNullElse(video.getViewCount(), 0L))
                        .count(traineeSubmissionRepository.findByCoachVideoId(video.getId()).size())
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CoachWorkspaceResponses.ChartPointResponse> getProfileViews(Long currentUserId) {
        long totalVideoViews = Objects.requireNonNullElse(coachVideoRepository.getTotalViewsByCoach(currentUserId), 0L);
        return List.of(CoachWorkspaceResponses.ChartPointResponse.builder()
                .period("TOTAL")
                .value(totalVideoViews)
                .count(coachVideoRepository.countByCoachId(currentUserId))
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CoachWorkspaceResponses.StudentSummaryResponse> getStudents(Long currentUserId) {
        CoachProfile coach = getCoach(currentUserId);
        return bookingRepository.findByCoachId(coach.getId()).stream()
                .collect(Collectors.groupingBy(Booking::getTrainee))
                .entrySet()
                .stream()
                .map(entry -> mapStudentSummary(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(CoachWorkspaceResponses.StudentSummaryResponse::getFullName, Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CoachWorkspaceResponses.StudentDetailResponse getStudent(Long currentUserId, Long traineeId) {
        TraineeProfile trainee = getManagedTrainee(currentUserId, traineeId);
        return CoachWorkspaceResponses.StudentDetailResponse.builder()
                .profile(mapStudentSummary(trainee.getUser(), getStudentBookings(currentUserId, traineeId)))
                .recentSessions(getStudentSessions(currentUserId, traineeId))
                .tasks(getStudentTasks(currentUserId, traineeId))
                .notes(getStudentNotes(currentUserId, traineeId))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CoachWorkspaceResponses.SessionResponse> getStudentSessions(Long currentUserId, Long traineeId) {
        return getStudentBookings(currentUserId, traineeId).stream()
                .map(this::mapSession)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CoachWorkspaceResponses.StudentProgressResponse getStudentProgress(Long currentUserId, Long traineeId) {
        TraineeProfile trainee = getManagedTrainee(currentUserId, traineeId);
        List<Booking> bookings = getStudentBookings(currentUserId, traineeId);
        List<TraineeSubmission> submissions = traineeSubmissionRepository.findByTraineeId(trainee.getUser().getId());
        List<TraineeSubmission> reviewed = submissions.stream()
                .filter(s -> s.getStatus() != SubmissionStatus.PENDING)
                .toList();

        return CoachWorkspaceResponses.StudentProgressResponse.builder()
                .traineeId(traineeId)
                .totalSessions(bookings.size())
                .completedSessions(countBookings(bookings, BookingStatus.COMPLETED))
                .pendingSubmissions(submissions.stream().filter(s -> s.getStatus() == SubmissionStatus.PENDING).count())
                .reviewedSubmissions(reviewed.size())
                .averageSubmissionScore(reviewed.stream().map(TraineeSubmission::getTotalScore).filter(Objects::nonNull).mapToDouble(Double::doubleValue).average().orElse(0.0))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CoachWorkspaceResponses.TaskResponse> getStudentTasks(Long currentUserId, Long traineeId) {
        CoachProfile coach = getCoach(currentUserId);
        TraineeProfile trainee = getManagedTrainee(currentUserId, traineeId);
        return taskRepository.findByCoachIdAndTraineeIdOrderByCreatedAtDesc(coach.getId(), trainee.getId())
                .stream()
                .map(this::mapTask)
                .toList();
    }

    @Override
    @Transactional
    public CoachWorkspaceResponses.TaskResponse createStudentTask(Long currentUserId, Long traineeId, CoachWorkspaceRequests.StudentTaskRequest request) {
        CoachProfile coach = getCoach(currentUserId);
        TraineeProfile trainee = getManagedTrainee(currentUserId, traineeId);
        CoachStudentTask task = CoachStudentTask.builder()
                .coach(coach)
                .trainee(trainee)
                .title(request.getTitle())
                .description(request.getDescription())
                .status(request.getStatus() == null ? "OPEN" : request.getStatus())
                .dueDate(request.getDueDate())
                .completed(Boolean.TRUE.equals(request.getCompleted()))
                .build();
        return mapTask(taskRepository.save(task));
    }

    @Override
    @Transactional
    public CoachWorkspaceResponses.TaskResponse updateStudentTask(Long currentUserId, Long traineeId, Long taskId, CoachWorkspaceRequests.StudentTaskRequest request) {
        CoachStudentTask task = getOwnedTask(currentUserId, traineeId, taskId);
        if (request.getTitle() != null) task.setTitle(request.getTitle());
        if (request.getDescription() != null) task.setDescription(request.getDescription());
        if (request.getStatus() != null) task.setStatus(request.getStatus());
        if (request.getDueDate() != null) task.setDueDate(request.getDueDate());
        if (request.getCompleted() != null) task.setCompleted(request.getCompleted());
        return mapTask(taskRepository.save(task));
    }

    @Override
    @Transactional
    public void deleteStudentTask(Long currentUserId, Long traineeId, Long taskId) {
        taskRepository.delete(getOwnedTask(currentUserId, traineeId, taskId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CoachWorkspaceResponses.NoteResponse> getStudentNotes(Long currentUserId, Long traineeId) {
        CoachProfile coach = getCoach(currentUserId);
        TraineeProfile trainee = getManagedTrainee(currentUserId, traineeId);
        return noteRepository.findByCoachIdAndTraineeIdOrderByCreatedAtDesc(coach.getId(), trainee.getId())
                .stream()
                .map(this::mapNote)
                .toList();
    }

    @Override
    @Transactional
    public CoachWorkspaceResponses.NoteResponse createStudentNote(Long currentUserId, Long traineeId, CoachWorkspaceRequests.StudentNoteRequest request) {
        CoachProfile coach = getCoach(currentUserId);
        TraineeProfile trainee = getManagedTrainee(currentUserId, traineeId);
        CoachStudentNote note = CoachStudentNote.builder()
                .coach(coach)
                .trainee(trainee)
                .title(request.getTitle())
                .content(request.getContent())
                .build();
        return mapNote(noteRepository.save(note));
    }

    @Override
    @Transactional
    public CoachWorkspaceResponses.NoteResponse updateStudentNote(Long currentUserId, Long traineeId, Long noteId, CoachWorkspaceRequests.StudentNoteRequest request) {
        CoachStudentNote note = getOwnedNote(currentUserId, traineeId, noteId);
        if (request.getTitle() != null) note.setTitle(request.getTitle());
        if (request.getContent() != null) note.setContent(request.getContent());
        return mapNote(noteRepository.save(note));
    }

    @Override
    @Transactional
    public void deleteStudentNote(Long currentUserId, Long traineeId, Long noteId) {
        noteRepository.delete(getOwnedNote(currentUserId, traineeId, noteId));
    }

    private CoachProfile getCoach(Long currentUserId) {
        return coachRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Coach profile not found"));
    }

    private TraineeProfile getManagedTrainee(Long currentUserId, Long traineeId) {
        CoachProfile coach = getCoach(currentUserId);
        TraineeProfile trainee = traineeProfileRepository.findById(traineeId)
                .orElseThrow(() -> new ResourceNotFoundException("Trainee profile not found"));

        boolean hasBooking = bookingRepository.findByCoachId(coach.getId()).stream()
                .anyMatch(b -> b.getTrainee().getId().equals(trainee.getUser().getId()));
        boolean assigned = trainee.getCoach() != null && trainee.getCoach().getId().equals(coach.getId());
        if (!hasBooking && !assigned) {
            throw new UnauthorizedException("This trainee is not managed by current coach");
        }
        return trainee;
    }

    private List<Booking> getStudentBookings(Long currentUserId, Long traineeId) {
        CoachProfile coach = getCoach(currentUserId);
        TraineeProfile trainee = getManagedTrainee(currentUserId, traineeId);
        return bookingRepository.findByCoachId(coach.getId()).stream()
                .filter(b -> b.getTrainee().getId().equals(trainee.getUser().getId()))
                .sorted(Comparator.comparing(Booking::getStartDate).reversed())
                .toList();
    }

    private List<WalletTransaction> getCoachWalletTransactions(Long currentUserId) {
        return walletRepository.findByUserId(currentUserId)
                .map(wallet -> walletTransactionRepository.findTop50ByWalletIdOrderByCreatedAtDesc(wallet.getId()))
                .orElse(List.of());
    }

    private CoachWorkspaceResponses.StudentSummaryResponse mapStudentSummary(User traineeUser, List<Booking> bookings) {
        TraineeProfile profile = traineeProfileRepository.findByUserId(traineeUser.getId()).orElse(null);
        return CoachWorkspaceResponses.StudentSummaryResponse.builder()
                .traineeId(profile == null ? null : profile.getId())
                .userId(traineeUser.getId())
                .fullName(traineeUser.getFullName())
                .avatar(profile == null ? traineeUser.getAvatarUrl() : profile.getAvatar())
                .goal(profile == null ? null : profile.getGoal())
                .phone(profile == null ? traineeUser.getPhone() : profile.getPhone())
                .sessions(bookings.size())
                .completedSessions(countBookings(bookings, BookingStatus.COMPLETED))
                .lastSessionDate(bookings.stream().map(Booking::getStartDate).max(LocalDate::compareTo).orElse(null))
                .build();
    }

    private CoachWorkspaceResponses.SessionResponse mapSession(Booking booking) {
        return CoachWorkspaceResponses.SessionResponse.builder()
                .bookingId(booking.getId())
                .startDate(booking.getStartDate())
                .endDate(booking.getEndDate())
                .dayOfWeek(booking.getDayOfWeek().name())
                .startTime(booking.getStartTime())
                .endTime(booking.getEndTime())
                .status(booking.getStatus())
                .price(booking.getPrice())
                .build();
    }

    private CoachWorkspaceResponses.TaskResponse mapTask(CoachStudentTask task) {
        return CoachWorkspaceResponses.TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .dueDate(task.getDueDate())
                .completed(task.getCompleted())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }

    private CoachWorkspaceResponses.NoteResponse mapNote(CoachStudentNote note) {
        return CoachWorkspaceResponses.NoteResponse.builder()
                .id(note.getId())
                .title(note.getTitle())
                .content(note.getContent())
                .createdAt(note.getCreatedAt())
                .updatedAt(note.getUpdatedAt())
                .build();
    }

    private CoachStudentTask getOwnedTask(Long currentUserId, Long traineeId, Long taskId) {
        CoachProfile coach = getCoach(currentUserId);
        TraineeProfile trainee = getManagedTrainee(currentUserId, traineeId);
        CoachStudentTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        if (!task.getCoach().getId().equals(coach.getId()) || !task.getTrainee().getId().equals(trainee.getId())) {
            throw new UnauthorizedException("You cannot manage this task");
        }
        return task;
    }

    private CoachStudentNote getOwnedNote(Long currentUserId, Long traineeId, Long noteId) {
        CoachProfile coach = getCoach(currentUserId);
        TraineeProfile trainee = getManagedTrainee(currentUserId, traineeId);
        CoachStudentNote note = noteRepository.findById(noteId)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found"));
        if (!note.getCoach().getId().equals(coach.getId()) || !note.getTrainee().getId().equals(trainee.getId())) {
            throw new UnauthorizedException("You cannot manage this note");
        }
        return note;
    }

    private WalletTransactionResponse mapWalletTransaction(WalletTransaction transaction) {
        return WalletTransactionResponse.builder()
                .id(transaction.getId())
                .type(transaction.getType())
                .amount(transaction.getAmount())
                .balanceBefore(transaction.getBalanceBefore())
                .balanceAfter(transaction.getBalanceAfter())
                .description(transaction.getDescription())
                .referenceType(transaction.getReferenceType())
                .referenceId(transaction.getReferenceId())
                .subscriptionPlanCode(transaction.getSubscriptionPlanCode())
                .subscriptionBillingCycle(transaction.getSubscriptionBillingCycle())
                .withdrawalStatus(transaction.getWithdrawalStatus())
                .adminNote(transaction.getAdminNote())
                .processedByUserId(transaction.getProcessedByUserId())
                .processedByName(transaction.getProcessedByName())
                .processedAt(transaction.getProcessedAt())
                .createdAt(transaction.getCreatedAt())
                .build();
    }

    private long sumTransactions(List<WalletTransaction> transactions, WalletTransactionType type, LocalDate from, LocalDate to) {
        return transactions.stream()
                .filter(t -> t.getType() == type)
                .filter(t -> from == null || !t.getCreatedAt().toLocalDate().isBefore(from))
                .filter(t -> to == null || t.getCreatedAt().toLocalDate().isBefore(to))
                .mapToLong(t -> Math.abs(nullToZero(t.getAmount())))
                .sum();
    }

    private long countTransactions(List<WalletTransaction> transactions, WalletTransactionType type, LocalDate from, LocalDate to) {
        return transactions.stream()
                .filter(t -> t.getType() == type)
                .filter(t -> from == null || !t.getCreatedAt().toLocalDate().isBefore(from))
                .filter(t -> to == null || t.getCreatedAt().toLocalDate().isBefore(to))
                .count();
    }

    private long sumPendingWithdrawals(List<WalletTransaction> transactions) {
        return transactions.stream()
                .filter(t -> t.getType() == WalletTransactionType.WITHDRAWAL)
                .filter(t -> t.getWithdrawalStatus() == WalletWithdrawalStatus.PROCESSING)
                .mapToLong(t -> Math.abs(nullToZero(t.getAmount())))
                .sum();
    }

    private long countBookings(List<Booking> bookings, BookingStatus status) {
        return bookings.stream().filter(b -> b.getStatus() == status).count();
    }

    private boolean inMonth(LocalDate date, YearMonth month) {
        return date != null && YearMonth.from(date).equals(month);
    }

    private long bookingAmount(Booking booking) {
        if (booking.getCoachPayoutAmount() != null) {
            return booking.getCoachPayoutAmount();
        }
        return booking.getPrice() == null ? 0L : Math.round(booking.getPrice());
    }

    private long nullToZero(Long value) {
        return Objects.requireNonNullElse(value, 0L);
    }

    private Long resolveTraineeProfileId(Long traineeUserId) {
        return traineeProfileRepository.findByUserId(traineeUserId).map(TraineeProfile::getId).orElse(null);
    }
}
