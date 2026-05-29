P0 — Nên làm trước để FE không bị kẹt

1. Thiếu API lấy profile hiện tại của coach

Hiện backend có:

POST /api/coaches/profile
PUT /api/coaches/{id}
GET /api/coaches/coachDetail/{id}

Nhưng chưa có API kiểu:

GET /api/coaches/me

FE rất cần API này để biết tài khoản coach hiện tại đã có profile chưa, lấy coachId, avatar, category, price, bio.

Nên thêm:

GET /api/coaches/me
PUT /api/coaches/me 2. Thiếu API lấy profile hiện tại của trainee

Backend có:

POST /api/trainees/profile
PUT /api/trainees/{id}
DELETE /api/trainees/{id}

Nhưng thiếu:

GET /api/trainees/me
PUT /api/trainees/me

Quan trọng hơn: khi học viên đăng ký xong, backend chưa chắc đã tự tạo TraineeProfile. Điều này có thể làm API danh sách học viên của HLV bị rỗng.

Nên thêm hoặc sửa:

GET /api/trainees/me
PUT /api/trainees/me

Và nên tự tạo TraineeProfile mặc định khi user đăng ký role TRAINEES.

3. Thiếu API coach xác nhận / từ chối booking

UI có trạng thái:

pending
upcoming
completed
cancelled

Backend hiện có:

POST /api/bookings
GET /api/bookings/my
PUT /api/bookings/{id}/complete
PUT /api/bookings/{id}/cancel

Nhưng thiếu API để HLV duyệt lịch:

PUT /api/bookings/{id}/confirm
PUT /api/bookings/{id}/reject

Hiện tại học viên đặt lịch xong status là PENDING, nhưng coach chưa có API chuẩn để chuyển sang CONFIRMED / UPCOMING.

Nên thêm:

PUT /api/bookings/{id}/confirm
PUT /api/bookings/{id}/reject
PUT /api/bookings/{id}/cancel-by-coach 4. Thiếu API kiểm tra slot còn trống

FE hiện đang lấy lịch của coach rồi tự so sánh, nhưng không biết slot đó đã bị học viên khác đặt chưa. Backend chỉ chặn lúc tạo booking.

Nên thêm:

GET /api/coaches/{coachId}/available-slots?date=2026-05-28
GET /api/coaches/{coachId}/schedule-with-availability

Response nên có:

{
"id": 1,
"startDate": "2026-05-28",
"endDate": "2026-05-28",
"dayOfWeek": "THURSDAY",
"startTime": "09:00:00",
"endTime": "10:00:00",
"available": true
}

Hiện CoachScheduleResponse của bạn thiếu id, startDate, endDate, available.

5. Thiếu update/delete lịch rảnh của coach

Backend mới có tạo lịch:

POST /api/coaches/create/Schedule

Nhưng chưa có:

PUT /api/coaches/schedules/{scheduleId}
DELETE /api/coaches/schedules/{scheduleId}

Màn CoachSchedule có nhu cầu quản lý lịch, xoá lịch, chỉnh lịch. Nên bổ sung.

3. API thiếu cho màn Admin

Admin UI của bạn đang mock nhiều. Backend hiện có admin setting và wallet overview một phần, nhưng chưa đủ.

Thiếu API quản lý user

Nên có:

GET /api/v1/admin/users
GET /api/v1/admin/users/{id}
PUT /api/v1/admin/users/{id}/status
DELETE /api/v1/admin/users/{id}

Query nên hỗ trợ:

GET /api/v1/admin/users?role=COACHES&status=ACTIVE&keyword=abc&page=0&size=20

Hiện backend chỉ có:

GET /api/v1/users/{id}
PUT /api/v1/users/{userId}/status
PUT /api/v1/users/activate/{userId}

Nhưng chưa có API list/search/filter users cho admin.

Thiếu API admin dashboard overview

Màn AdminOverview cần số liệu như:

tổng user
tổng HLV
tổng học viên
doanh thu hôm nay
doanh thu tháng
số giao dịch
số booking
hoa hồng nền tảng
biểu đồ doanh thu
giao dịch gần đây
cảnh báo hệ thống

Nên thêm:

GET /api/v1/admin/dashboard/overview
GET /api/v1/admin/dashboard/revenue-chart?range=month
GET /api/v1/admin/dashboard/recent-transactions
GET /api/v1/admin/dashboard/alerts
Thiếu API admin giao dịch học phí

Backend có wallet transactions của user hiện tại, nhưng chưa có API admin xem toàn bộ giao dịch học phí/booking.

Nên thêm:

GET /api/v1/admin/transactions
GET /api/v1/admin/transactions/{id}

Filter:

GET /api/v1/admin/transactions?status=SUCCESS&coachPlan=PRO&from=2026-05-01&to=2026-05-28&page=0&size=20

Response nên có:

{
"id": "TXN-001",
"learnerName": "Nguyễn Văn A",
"coachName": "Trần Văn B",
"amount": 400000,
"commission": 48000,
"coachPayout": 352000,
"status": "SUCCESS",
"createdAt": "2026-05-28T10:00:00"
}
Thiếu API admin subscription users

Backend có subscription cho user hiện tại:

GET /api/v1/subscriptions/me
GET /api/v1/subscriptions/packages
POST /api/v1/subscriptions/purchase

Nhưng AdminSubscriptions UI cần list toàn bộ user đang dùng gói.

Nên thêm:

GET /api/v1/admin/subscriptions
GET /api/v1/admin/subscriptions/stats
PUT /api/v1/admin/subscriptions/{userId}

Filter:

GET /api/v1/admin/subscriptions?role=COACHES&plan=PRO&status=ACTIVE
Thiếu API báo cáo tài chính admin

Màn AdminFinance cần biểu đồ:

doanh thu theo tháng
hoa hồng theo gói
nguồn doanh thu
top coach
doanh thu tuần

Nên thêm:

GET /api/v1/admin/finance/overview
GET /api/v1/admin/finance/monthly-revenue
GET /api/v1/admin/finance/commission-by-plan
GET /api/v1/admin/finance/revenue-by-source
GET /api/v1/admin/finance/top-coaches 4. API thiếu cho Coach Dashboard
Thiếu API income dashboard cho coach

Backend có ví tiền:

GET /api/v1/wallets/me
GET /api/v1/wallets/me/transactions
POST /api/v1/wallets/withdraw

Nhưng màn CoachIncome cần nhiều hơn:

doanh thu tháng
doanh thu theo nguồn
giao dịch học viên
top học viên
payout history
số tiền pending
số tiền available
hoa hồng bị trừ

Nên thêm:

GET /api/v1/coach/income/overview
GET /api/v1/coach/income/transactions
GET /api/v1/coach/income/monthly-chart
GET /api/v1/coach/income/top-students
GET /api/v1/coach/income/payouts
Thiếu API analytics cho coach

Màn CoachAnalytics đang mock hoàn toàn.

Nên thêm:

GET /api/v1/coach/analytics/overview
GET /api/v1/coach/analytics/bookings
GET /api/v1/coach/analytics/revenue
GET /api/v1/coach/analytics/students-progress
GET /api/v1/coach/analytics/videos
GET /api/v1/coach/analytics/profile-views
Thiếu API quản lý học viên nâng cao

Backend có:

GET /api/trainees/my-trainees/search

Nhưng UI CoachStudents cần:

chi tiết học viên
lịch sử buổi học
bài tập được giao
ghi chú của coach
tiến độ học viên
doanh thu theo học viên

Nên thêm:

GET /api/v1/coach/students
GET /api/v1/coach/students/{traineeId}
GET /api/v1/coach/students/{traineeId}/sessions
GET /api/v1/coach/students/{traineeId}/progress
GET /api/v1/coach/students/{traineeId}/tasks
POST /api/v1/coach/students/{traineeId}/tasks
PUT /api/v1/coach/students/{traineeId}/tasks/{taskId}
DELETE /api/v1/coach/students/{traineeId}/tasks/{taskId}
GET /api/v1/coach/students/{traineeId}/notes
POST /api/v1/coach/students/{traineeId}/notes
DELETE /api/v1/coach/students/{traineeId}/notes/{noteId} 5. API thiếu cho Video 360 / Video Studio

Backend hiện có video cơ bản, nhưng chưa đủ cho UI.

Hiện có:

POST /api/coach/videos/upload
GET /api/coach/videos/coach/videos
GET /api/coach/videos/coach/videos/{videoId}
GET /api/coach/videos/coach/videos/search
POST /api/coach/videos/coach/videos/{videoId}/view
POST /api/trainee/submissions/upload
GET /api/trainee/submissions/coach/videos/{videoId}/submissions
PUT /api/trainee/submissions/coach/submissions/{submissionId}/review

Nhưng UI cần thêm:

GET /api/v1/videos
GET /api/v1/videos/{id}
PUT /api/v1/coach/videos/{id}
DELETE /api/v1/coach/videos/{id}
POST /api/v1/videos/{id}/like
DELETE /api/v1/videos/{id}/like
POST /api/v1/videos/{id}/save
DELETE /api/v1/videos/{id}/save
GET /api/v1/videos/saved
GET /api/v1/coach/videos/dashboard
GET /api/v1/coach/videos/{id}/analytics
GET /api/v1/coach/submissions
GET /api/v1/coach/submissions/pending

Ngoài ra entity CoachVideo hiện còn thiếu nhiều field UI cần:

description
thumbnailUrl
duration
difficulty
visibility: public/students/private
likes
isPremium
categoryId khi upload 6. API thiếu cho Progress Tracking

Bạn nói trừ AI phân tích, nhưng màn Tiến độ vẫn có phần không phải AI như cân nặng, body fat, bài tập, achievement, streak.

Backend hiện chưa có nhóm API này.

Nên thêm:

GET /api/v1/trainees/progress/overview
GET /api/v1/trainees/progress/body-metrics
POST /api/v1/trainees/progress/body-metrics
GET /api/v1/trainees/progress/exercises
POST /api/v1/trainees/progress/exercises
GET /api/v1/trainees/progress/achievements
GET /api/v1/trainees/progress/streak
GET /api/v1/trainees/progress/heatmap

Nếu không làm màn Progress thì có thể bỏ qua, nhưng UI hiện có menu này.

7. API thiếu cho notification

UI có chuông thông báo ở dashboard learner/coach/admin, nhưng backend chưa thấy notification API.

Nên thêm:

GET /api/v1/notifications
PUT /api/v1/notifications/{id}/read
PUT /api/v1/notifications/read-all
DELETE /api/v1/notifications/{id}

Thông báo cần dùng cho:

booking mới
coach xác nhận lịch
huỷ lịch
sắp đến giờ học
video được review
giao dịch ví
subscription hết hạn
admin alert 8. Chat API có rồi nhưng thiếu vài API phụ

Chat cơ bản đã có:

GET /api/v1/chat/conversations
POST /api/v1/chat/conversations
GET /api/v1/chat/conversations/{conversationId}/messages
POST /api/v1/chat/conversations/{conversationId}/messages

Nên thêm nếu muốn giống UI hơn:

GET /api/v1/chat/unread-count
PUT /api/v1/chat/conversations/{conversationId}/read
DELETE /api/v1/chat/conversations/{conversationId}

Không bắt buộc, nhưng nên có.
