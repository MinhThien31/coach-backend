package com.minhthien.web.coach.controller;

import com.minhthien.web.coach.dto.request.CoachWorkspaceRequests;
import com.minhthien.web.coach.dto.response.ApiResponse;
import com.minhthien.web.coach.dto.response.CoachWorkspaceResponses;
import com.minhthien.web.coach.dto.response.WalletTransactionResponse;
import com.minhthien.web.coach.entity.User;
import com.minhthien.web.coach.service.CoachWorkspaceService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/coach")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class CoachWorkspaceController {

    private final CoachWorkspaceService coachWorkspaceService;

    @GetMapping("/income/overview")
    public ApiResponse<CoachWorkspaceResponses.IncomeOverviewResponse> getIncomeOverview(@AuthenticationPrincipal User currentUser) {
        return ApiResponse.success(coachWorkspaceService.getIncomeOverview(currentUser.getId()));
    }

    @GetMapping("/income/transactions")
    public ApiResponse<List<WalletTransactionResponse>> getIncomeTransactions(@AuthenticationPrincipal User currentUser) {
        return ApiResponse.success(coachWorkspaceService.getIncomeTransactions(currentUser.getId()));
    }

    @GetMapping("/income/monthly-chart")
    public ApiResponse<List<CoachWorkspaceResponses.ChartPointResponse>> getMonthlyChart(@AuthenticationPrincipal User currentUser) {
        return ApiResponse.success(coachWorkspaceService.getMonthlyChart(currentUser.getId()));
    }

    @GetMapping("/income/top-students")
    public ApiResponse<List<CoachWorkspaceResponses.TopStudentResponse>> getTopStudents(@AuthenticationPrincipal User currentUser) {
        return ApiResponse.success(coachWorkspaceService.getTopStudents(currentUser.getId()));
    }

    @GetMapping("/income/payouts")
    public ApiResponse<List<WalletTransactionResponse>> getPayouts(@AuthenticationPrincipal User currentUser) {
        return ApiResponse.success(coachWorkspaceService.getPayouts(currentUser.getId()));
    }

    @GetMapping("/analytics/overview")
    public ApiResponse<CoachWorkspaceResponses.AnalyticsOverviewResponse> getAnalyticsOverview(@AuthenticationPrincipal User currentUser) {
        return ApiResponse.success(coachWorkspaceService.getAnalyticsOverview(currentUser.getId()));
    }

    @GetMapping("/analytics/bookings")
    public ApiResponse<List<CoachWorkspaceResponses.ChartPointResponse>> getBookingAnalytics(@AuthenticationPrincipal User currentUser) {
        return ApiResponse.success(coachWorkspaceService.getBookingAnalytics(currentUser.getId()));
    }

    @GetMapping("/analytics/revenue")
    public ApiResponse<List<CoachWorkspaceResponses.ChartPointResponse>> getRevenueAnalytics(@AuthenticationPrincipal User currentUser) {
        return ApiResponse.success(coachWorkspaceService.getRevenueAnalytics(currentUser.getId()));
    }

    @GetMapping("/analytics/students-progress")
    public ApiResponse<List<CoachWorkspaceResponses.StudentProgressResponse>> getStudentsProgress(@AuthenticationPrincipal User currentUser) {
        return ApiResponse.success(coachWorkspaceService.getStudentsProgress(currentUser.getId()));
    }

    @GetMapping("/analytics/videos")
    public ApiResponse<List<CoachWorkspaceResponses.ChartPointResponse>> getVideoAnalytics(@AuthenticationPrincipal User currentUser) {
        return ApiResponse.success(coachWorkspaceService.getVideoAnalytics(currentUser.getId()));
    }

    @GetMapping("/analytics/profile-views")
    public ApiResponse<List<CoachWorkspaceResponses.ChartPointResponse>> getProfileViews(@AuthenticationPrincipal User currentUser) {
        return ApiResponse.success(coachWorkspaceService.getProfileViews(currentUser.getId()));
    }

    @GetMapping("/students")
    public ApiResponse<List<CoachWorkspaceResponses.StudentSummaryResponse>> getStudents(@AuthenticationPrincipal User currentUser) {
        return ApiResponse.success(coachWorkspaceService.getStudents(currentUser.getId()));
    }

    @GetMapping("/students/{traineeId}")
    public ApiResponse<CoachWorkspaceResponses.StudentDetailResponse> getStudent(@AuthenticationPrincipal User currentUser, @PathVariable Long traineeId) {
        return ApiResponse.success(coachWorkspaceService.getStudent(currentUser.getId(), traineeId));
    }

    @GetMapping("/students/{traineeId}/sessions")
    public ApiResponse<List<CoachWorkspaceResponses.SessionResponse>> getStudentSessions(@AuthenticationPrincipal User currentUser, @PathVariable Long traineeId) {
        return ApiResponse.success(coachWorkspaceService.getStudentSessions(currentUser.getId(), traineeId));
    }

    @GetMapping("/students/{traineeId}/progress")
    public ApiResponse<CoachWorkspaceResponses.StudentProgressResponse> getStudentProgress(@AuthenticationPrincipal User currentUser, @PathVariable Long traineeId) {
        return ApiResponse.success(coachWorkspaceService.getStudentProgress(currentUser.getId(), traineeId));
    }

    @GetMapping("/students/{traineeId}/tasks")
    public ApiResponse<List<CoachWorkspaceResponses.TaskResponse>> getTasks(@AuthenticationPrincipal User currentUser, @PathVariable Long traineeId) {
        return ApiResponse.success(coachWorkspaceService.getStudentTasks(currentUser.getId(), traineeId));
    }

    @PostMapping("/students/{traineeId}/tasks")
    public ApiResponse<CoachWorkspaceResponses.TaskResponse> createTask(@AuthenticationPrincipal User currentUser, @PathVariable Long traineeId, @RequestBody CoachWorkspaceRequests.StudentTaskRequest request) {
        return ApiResponse.success(coachWorkspaceService.createStudentTask(currentUser.getId(), traineeId, request));
    }

    @PutMapping("/students/{traineeId}/tasks/{taskId}")
    public ApiResponse<CoachWorkspaceResponses.TaskResponse> updateTask(@AuthenticationPrincipal User currentUser, @PathVariable Long traineeId, @PathVariable Long taskId, @RequestBody CoachWorkspaceRequests.StudentTaskRequest request) {
        return ApiResponse.success(coachWorkspaceService.updateStudentTask(currentUser.getId(), traineeId, taskId, request));
    }

    @DeleteMapping("/students/{traineeId}/tasks/{taskId}")
    public ApiResponse<Void> deleteTask(@AuthenticationPrincipal User currentUser, @PathVariable Long traineeId, @PathVariable Long taskId) {
        coachWorkspaceService.deleteStudentTask(currentUser.getId(), traineeId, taskId);
        return ApiResponse.success("Task deleted successfully", null);
    }

    @GetMapping("/students/{traineeId}/notes")
    public ApiResponse<List<CoachWorkspaceResponses.NoteResponse>> getNotes(@AuthenticationPrincipal User currentUser, @PathVariable Long traineeId) {
        return ApiResponse.success(coachWorkspaceService.getStudentNotes(currentUser.getId(), traineeId));
    }

    @PostMapping("/students/{traineeId}/notes")
    public ApiResponse<CoachWorkspaceResponses.NoteResponse> createNote(@AuthenticationPrincipal User currentUser, @PathVariable Long traineeId, @RequestBody CoachWorkspaceRequests.StudentNoteRequest request) {
        return ApiResponse.success(coachWorkspaceService.createStudentNote(currentUser.getId(), traineeId, request));
    }

    @PutMapping("/students/{traineeId}/notes/{noteId}")
    public ApiResponse<CoachWorkspaceResponses.NoteResponse> updateNote(@AuthenticationPrincipal User currentUser, @PathVariable Long traineeId, @PathVariable Long noteId, @RequestBody CoachWorkspaceRequests.StudentNoteRequest request) {
        return ApiResponse.success(coachWorkspaceService.updateStudentNote(currentUser.getId(), traineeId, noteId, request));
    }

    @DeleteMapping("/students/{traineeId}/notes/{noteId}")
    public ApiResponse<Void> deleteNote(@AuthenticationPrincipal User currentUser, @PathVariable Long traineeId, @PathVariable Long noteId) {
        coachWorkspaceService.deleteStudentNote(currentUser.getId(), traineeId, noteId);
        return ApiResponse.success("Note deleted successfully", null);
    }
}
