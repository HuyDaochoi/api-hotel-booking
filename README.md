# 🏨 Hotel Booking API - Spring Boot Backend

Chào mừng đến với dự án **Hotel Booking API**. Đây là một hệ thống backend mạnh mẽ được xây dựng bằng **Java 17** và **Spring Boot 3**, cung cấp đầy đủ các API (RESTful) để quản lý một hệ thống đặt phòng khách sạn chuyên nghiệp, từ việc quản lý người dùng, phòng ốc, tiện ích cho đến xử lý đặt phòng, thanh toán, hoàn tiền và báo cáo doanh thu.

---

## 🛠 Công Nghệ Sử Dụng (Tech Stack)

- **Ngôn ngữ:** Java 17
- **Framework:** Spring Boot 3 (Web, Data JPA, Security, Validation)
- **Cơ sở dữ liệu:** MySQL 8+
- **Bảo mật:** Spring Security & JSON Web Token (JWT) cho Authentication & Authorization.
- **Tài liệu API:** Swagger UI / OpenAPI 3
- **Quản lý dependencies:** Maven
- **Khác:** Lombok (Giảm boilerplate code), ModelMapper.

---

## 🚀 Các Tính Năng Cốt Lõi (Core Features)

1. **🔒 Quản lý Người dùng & Phân quyền (Auth & Security)**
   - Đăng ký, Đăng nhập và lấy Access Token / Refresh Token (JWT).
   - Phân quyền theo Role-Based Access Control (RBAC): `ADMIN` và `CUSTOMER`.
   - API đổi mật khẩu, cập nhật profile dành riêng cho người dùng.
   - Admin có quyền tạo, sửa, xóa, và xem danh sách người dùng.

2. **🛏️ Quản lý Phòng & Loại Phòng (Room Management)**
   - CRUD Loại phòng (Room Types) và Phòng vật lý (Rooms).
   - Quản lý Tiện ích (Amenities) và gán tiện ích vào từng loại phòng.
   - **Tìm kiếm nâng cao (Search):** Tìm phòng trống theo khoảng thời gian (Check-in/Check-out), số lượng khách và lọc theo các Tiện ích (AND logic).

3. **📅 Quản lý Đặt Phòng (Booking System)**
   - Tạo mới Booking (có kiểm tra lịch trống và tránh trùng lặp ngày).
   - Xử lý các luồng trạng thái chuẩn: `PENDING`, `CONFIRMED`, `CHECKED_IN`, `CHECKED_OUT`, `CANCELLED`, `NO_SHOW`.
   - Admin duyệt booking, xác nhận trạng thái.

4. **💳 Thanh Toán & Hoàn Tiền (Payment & Refund)**
   - Tích hợp logic thanh toán (tiền mặt/chuyển khoản).
   - Quản lý chính sách hủy phòng (Cancellation Policy) và tự động tính toán phí phạt / số tiền hoàn lại (Refund) dựa trên thời gian hủy.
   - Quản lý quy tắc giá động (Pricing Rules) theo mùa hoặc sự kiện.

5. **📸 Lưu Trữ Hình Ảnh (Image Storage)**
   - Upload file ảnh (Multipart) lưu trực tiếp trên Server (Local Storage).
   - Truy xuất ảnh qua đường dẫn tĩnh (Static resources mapping).
   - Gắn ảnh đại diện và thư viện ảnh cho từng Loại Phòng.

6. **📊 Báo Cáo & Thống Kê (Reporting)**
   - Tính toán doanh thu theo thời gian thực.
   - Các API xuất báo cáo dành riêng cho quyền quản trị (Admin).

---

## ⚙️ Hướng Dẫn Cài Đặt (Setup Instructions)

### 1. Yêu cầu hệ thống (Prerequisites)
- [JDK 17](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html) trở lên.
- [Maven 3.8+](https://maven.apache.org/) (Dự án đã có sẵn Maven Wrapper `mvnw`).
- MySQL Server (đang chạy ở port `3306`).

### 2. Cấu hình Database
Mở phần mềm quản lý MySQL (VD: MySQL Workbench, DBeaver, XAMPP) và tạo một database trống hoặc để Spring Boot tự tạo. 
Mặc định hệ thống sử dụng cấu hình trong file `src/main/resources/application-dev.yaml`:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/Hotel_Booking?createDatabaseIfNotExist=true...
    username: root
    password: # Điền password MySQL của bạn vào đây nếu có
```
*Lưu ý: Mặc định `ddl-auto: update` nên các bảng sẽ được tạo tự động khi chạy app.*

### 3. Khởi chạy ứng dụng (Run)
Mở terminal ở thư mục gốc của dự án và gõ lệnh:
```bash
# Trên Windows
./mvnw.cmd spring-boot:run

# Trên Linux/Mac
./mvnw spring-boot:run
```
Ứng dụng sẽ khởi chạy và lắng nghe ở cổng mặc định là **`8080`**.

---

## 📚 Tài Liệu API (API Documentation - Swagger)

Khi ứng dụng đã chạy thành công, bạn có thể xem và test toàn bộ các API thông qua giao diện UI trực quan của Swagger.

- **Đường dẫn Swagger UI:** `http://localhost:8080/swagger-ui.html`

> 💡 **Mẹo Test API:** 
> 1. Đầu tiên hãy dùng API `POST /api/auth/register` để tạo 1 tài khoản, sau đó vào Database đổi `role` thành `ADMIN` nếu cần.
> 2. Dùng API `POST /api/auth/login` để lấy chuỗi `accessToken`.
> 3. Click vào nút **"Authorize"** trên Swagger UI và dán Token vào (nhớ thêm chữ `Bearer ` ở trước, ví dụ: `Bearer eyJhbG...`). Sau đó bạn có thể test các API bị khóa.

---

## 📁 Cấu Trúc Thư Mục (Project Structure)
Dự án được thiết kế theo mô hình chuẩn của Spring Boot (Controller - Service - Repository):
- `controllers/`: Định nghĩa các API endpoints và tiếp nhận Request từ Client.
- `services/`: Chứa toàn bộ Business Logic (Xử lý đặt phòng, tính tiền, hoàn tiền...).
- `repository/`: Giao tiếp với Database qua Spring Data JPA.
- `schemas/`: Chứa các Entities (Domain) ánh xạ với các bảng trong MySQL và các lớp Enums.
- `dto/`: Data Transfer Objects (Request/Response) để giao tiếp dữ liệu an toàn.
- `configs/`: Cấu hình Security, CORS, Swagger, WebMvc.
- `common/`: Các file tiện ích (Utils), Global Exception Handler và khung Response trả về chuẩn mực.

---
*Developed by HuyDaoChoi - apiHotelBooking*
