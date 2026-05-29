# Kế Hoạch Triển Khai Các API Thiếu

## Summary

Tạo file kế hoạch mới tại `.github/docs/api-implementation-plan.md`, dựa trên `.github/docs/apithieui.md` và tình trạng hiện tại của backend Spring Boot. Plan sẽ chia implementation thành các phase rõ ràng, ưu tiên những API đang chặn FE trước, sau đó đến admin, coach dashboard, video, progress, notification và chat.

Lưu ý repo hiện tại đã có một số phần mà tài liệu nói thiếu: `PUT /api/bookings/{id}/confirm` đã tồn tại, `CoachScheduleResponse` đã có `id/startDate/endDate/status`. Vì vậy khi implement cần kiểm tra và hoàn thiện logic thay vì tạo trùng endpoint.

## Phase 1: P0 Unblock FE

- Thêm `GET /api/coaches/me` và `PUT /api/coaches/me`, dùng user hiện tại từ JWT, trả về `CoachResponse` hoặc `CoachDetailResponse` phù hợp để FE lấy `coachId`, avatar, category, price, bio.
- Thêm `GET /api/trainees/me` và `PUT /api/trainees/me`, dùng user hiện tại từ JWT, trả về `TraineeResponse`.
- Khi đăng ký hoặc social login với role `TRAINEES`, tự tạo `TraineeProfile` mặc định nếu chưa có.
- Hoàn thiện booking lifecycle:
  - Giữ `PUT /api/bookings/{id}/confirm` hiện có, kiểm tra lại quyền coach/admin và status transition.
  - Thêm `PUT /api/bookings/{id}/reject`.
  - Thêm `PUT /api/bookings/{id}/cancel-by-coach`.
- Hoàn thiện availability/schedule:
  - Thêm `GET /api/coaches/{coachId}/available-slots?date=yyyy-MM-dd`.
  - Thêm hoặc chuẩn hóa `GET /api/coaches/{coachId}/schedule-with-availability`.
  - Thêm `PUT /api/coaches/schedules/{scheduleId}`.
  - Thêm `DELETE /api/coaches/schedules/{scheduleId}`.
- Chuẩn response schedule có `id`, `startDate`, `endDate`, `dayOfWeek`, `startTime`, `endTime`, `available`, `status`, `bookingId`, `bookingStatus`.

## Phase 2: Admin APIs

- Thêm nhóm admin user management:
  - `GET /api/v1/admin/users`
  - `GET /api/v1/admin/users/{id}`
  - `PUT /api/v1/admin/users/{id}/status`
  - `DELETE /api/v1/admin/users/{id}`
  - Hỗ trợ filter `role`, `status`, `keyword`, `page`, `size`.
- Thêm admin dashboard:
  - `GET /api/v1/admin/dashboard/overview`
  - `GET /api/v1/admin/dashboard/revenue-chart?range=month`
  - `GET /api/v1/admin/dashboard/recent-transactions`
  - `GET /api/v1/admin/dashboard/alerts`
- Thêm admin transactions:
  - `GET /api/v1/admin/transactions`
  - `GET /api/v1/admin/transactions/{id}`
  - Filter `status`, `coachPlan`, `from`, `to`, `page`, `size`.
- Thêm admin subscriptions:
  - `GET /api/v1/admin/subscriptions`
  - `GET /api/v1/admin/subscriptions/stats`
  - `PUT /api/v1/admin/subscriptions/{userId}`
- Thêm admin finance:
  - `GET /api/v1/admin/finance/overview`
  - `GET /api/v1/admin/finance/monthly-revenue`
  - `GET /api/v1/admin/finance/commission-by-plan`
  - `GET /api/v1/admin/finance/revenue-by-source`
  - `GET /api/v1/admin/finance/top-coaches`

## Phase 3: Coach Dashboard APIs

- Thêm income dashboard cho coach:
  - `GET /api/v1/coach/income/overview`
  - `GET /api/v1/coach/income/transactions`
  - `GET /api/v1/coach/income/monthly-chart`
  - `GET /api/v1/coach/income/top-students`
  - `GET /api/v1/coach/income/payouts`
- Thêm coach analytics:
  - `GET /api/v1/coach/analytics/overview`
  - `GET /api/v1/coach/analytics/bookings`
  - `GET /api/v1/coach/analytics/revenue`
  - `GET /api/v1/coach/analytics/students-progress`
  - `GET /api/v1/coach/analytics/videos`
  - `GET /api/v1/coach/analytics/profile-views`
- Thêm quản lý học viên nâng cao:
  - `GET /api/v1/coach/students`
  - `GET /api/v1/coach/students/{traineeId}`
  - `GET /api/v1/coach/students/{traineeId}/sessions`
  - `GET /api/v1/coach/students/{traineeId}/progress`
  - CRUD task theo trainee.
  - CRUD note theo trainee.
- Nếu chưa có entity phù hợp, tạo entity tối thiểu cho `CoachStudentTask` và `CoachStudentNote`.

## Phase 4: Video 360 / Video Studio

- Chuẩn hóa API video public/v1:
  - `GET /api/v1/videos`
  - `GET /api/v1/videos/{id}`
  - `POST /api/v1/videos/{id}/like`
  - `DELETE /api/v1/videos/{id}/like`
  - `POST /api/v1/videos/{id}/save`
  - `DELETE /api/v1/videos/{id}/save`
  - `GET /api/v1/videos/saved`
- Thêm API coach video management:
  - `PUT /api/v1/coach/videos/{id}`
  - `DELETE /api/v1/coach/videos/{id}`
  - `GET /api/v1/coach/videos/dashboard`
  - `GET /api/v1/coach/videos/{id}/analytics`
- Thêm API submissions:
  - `GET /api/v1/coach/submissions`
  - `GET /api/v1/coach/submissions/pending`
- Mở rộng `CoachVideo` với `description`, `thumbnailUrl`, `duration`, `difficulty`, `visibility`, `likes`, `isPremium`, `categoryId`.

## Phase 5: Progress, Notification, Chat

- Thêm trainee progress tracking:
  - `GET /api/v1/trainees/progress/overview`
  - `GET/POST /api/v1/trainees/progress/body-metrics`
  - `GET/POST /api/v1/trainees/progress/exercises`
  - `GET /api/v1/trainees/progress/achievements`
  - `GET /api/v1/trainees/progress/streak`
  - `GET /api/v1/trainees/progress/heatmap`
- Thêm notification:
  - `GET /api/v1/notifications`
  - `PUT /api/v1/notifications/{id}/read`
  - `PUT /api/v1/notifications/read-all`
  - `DELETE /api/v1/notifications/{id}`
- Bắn notification cho booking mới, confirm/reject/cancel booking, video được review, wallet transaction, subscription hết hạn và admin alert.
- Hoàn thiện chat phụ:
  - `GET /api/v1/chat/unread-count`
  - `PUT /api/v1/chat/conversations/{conversationId}/read`
  - `DELETE /api/v1/chat/conversations/{conversationId}`

## Test Plan

- Chạy `mvn test` sau mỗi phase.
- Thêm service tests cho booking status transition, schedule availability, auto-create trainee profile, wallet/admin aggregates.
- Thêm controller tests cho role-based access: trainee, coach, admin, unauthorized.
- Test filter/pagination cho admin users, admin transactions, admin subscriptions.
- Test video like/save idempotency và quyền sửa/xóa video của coach.
- Test notification read/read-all/delete theo đúng user hiện tại.

## Assumptions

- File plan mới sẽ đặt tại `.github/docs/api-implementation-plan.md`.
- Giữ style hiện tại: Spring Boot controller/service/repository, `ApiResponse`, DTO request/response riêng.
- Không thêm Flyway/Liquibase trong phase này vì project đang dùng `spring.jpa.hibernate.ddl-auto=update`.
- Tất cả endpoint admin cần giới hạn role `ADMIN`; coach endpoint cần giới hạn owner coach hoặc admin khi phù hợp.
- Các API trong `.github/docs/apithieui.md` đều được đưa vào plan, kể cả Progress và Chat phụ.
