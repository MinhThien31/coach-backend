package com.minhthien.web.coach.service;

import com.minhthien.web.coach.dto.request.CoachWorkspaceRequests;
import com.minhthien.web.coach.dto.response.CoachWorkspaceResponses;
import com.minhthien.web.coach.dto.response.WalletTransactionResponse;

import java.util.List;

public interface CoachWorkspaceService {
    CoachWorkspaceResponses.IncomeOverviewResponse getIncomeOverview(Long currentUserId);
    List<WalletTransactionResponse> getIncomeTransactions(Long currentUserId);
    List<CoachWorkspaceResponses.ChartPointResponse> getMonthlyChart(Long currentUserId);
    List<CoachWorkspaceResponses.TopStudentResponse> getTopStudents(Long currentUserId);
    List<WalletTransactionResponse> getPayouts(Long currentUserId);

    CoachWorkspaceResponses.AnalyticsOverviewResponse getAnalyticsOverview(Long currentUserId);
    List<CoachWorkspaceResponses.ChartPointResponse> getBookingAnalytics(Long currentUserId);
    List<CoachWorkspaceResponses.ChartPointResponse> getRevenueAnalytics(Long currentUserId);
    List<CoachWorkspaceResponses.StudentProgressResponse> getStudentsProgress(Long currentUserId);
    List<CoachWorkspaceResponses.ChartPointResponse> getVideoAnalytics(Long currentUserId);
    List<CoachWorkspaceResponses.ChartPointResponse> getProfileViews(Long currentUserId);

    List<CoachWorkspaceResponses.StudentSummaryResponse> getStudents(Long currentUserId);
    CoachWorkspaceResponses.StudentDetailResponse getStudent(Long currentUserId, Long traineeId);
    List<CoachWorkspaceResponses.SessionResponse> getStudentSessions(Long currentUserId, Long traineeId);
    CoachWorkspaceResponses.StudentProgressResponse getStudentProgress(Long currentUserId, Long traineeId);

    List<CoachWorkspaceResponses.TaskResponse> getStudentTasks(Long currentUserId, Long traineeId);
    CoachWorkspaceResponses.TaskResponse createStudentTask(Long currentUserId, Long traineeId, CoachWorkspaceRequests.StudentTaskRequest request);
    CoachWorkspaceResponses.TaskResponse updateStudentTask(Long currentUserId, Long traineeId, Long taskId, CoachWorkspaceRequests.StudentTaskRequest request);
    void deleteStudentTask(Long currentUserId, Long traineeId, Long taskId);

    List<CoachWorkspaceResponses.NoteResponse> getStudentNotes(Long currentUserId, Long traineeId);
    CoachWorkspaceResponses.NoteResponse createStudentNote(Long currentUserId, Long traineeId, CoachWorkspaceRequests.StudentNoteRequest request);
    CoachWorkspaceResponses.NoteResponse updateStudentNote(Long currentUserId, Long traineeId, Long noteId, CoachWorkspaceRequests.StudentNoteRequest request);
    void deleteStudentNote(Long currentUserId, Long traineeId, Long noteId);
}
