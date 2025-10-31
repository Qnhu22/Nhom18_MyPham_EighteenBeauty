# Xây dựng website bán mỹ phẩm OneShop

![Java](https://img.shields.io/badge/Java-17-red?logo=openjdk) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.x-brightgreen?logo=springboot) ![SQL Server](https://img.shields.io/badge/SQL%20Server-Database-blue?logo=microsoftsqlserver)

## 📑 Mục lục
- [📌 Giới thiệu](#-giới-thiệu)
- [🏗️ Kiến trúc và Công nghệ](#-kiến-trúc-và-công-nghệ)
- [✨ Chức năng chính](#-chức-năng-chính)
- [🚀 Cách sử dụng](#-cách-sử-dụng)
- [🔐 Tài khoản & Phân quyền](#-tài-khoản-và-phân-quyền-trong-hệ-thống)
- [📞 Liên hệ](#-liên-hệ)


## 📌 Giới thiệu
  Trong bối cảnh thương mại điện tử phát triển mạnh, **OneShop** là một website bán mỹ phẩm được xây dựng để mang tới trải nghiệm mua sắm trực tuyến thuận tiện, nhanh chóng và an toàn cho người dùng cá nhân, đồng thời hỗ trợ đầy đủ các vai trò quản trị và bán hàng. Hệ thống được phát triển trên nền tảng **Java Web (Spring Boot)**, tối ưu về hiệu năng, bảo mật và khả năng mở rộng.

![Trang chủ](images/homepage.jpg)
> Hình ảnh trang chủ shop

## 🏗️ Kiến trúc và Công nghệ

**Kiến trúc:** MVC (Spring Boot) + REST API cho các chức năng nền.

| Khu vực | Công nghệ | Vai trò chính | Badge |
|---|---|---|---|
| Backend | Spring Boot | Xử lý nghiệp vụ, API, cấu hình hệ thống | ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.x-brightgreen) |
| Backend | Spring Security + JWT | Bảo mật, phân quyền, xác thực người dùng | ![Security](https://img.shields.io/badge/Security-JWT-blue) |
| Backend | JPA (Hibernate) | ORM, truy vấn dữ liệu | ![JPA](https://img.shields.io/badge/JPA-Hibernate-orange) |
| Frontend | Thymeleaf | Render giao diện động | ![Thymeleaf](https://img.shields.io/badge/Thymeleaf-Templates-success) |
| Frontend | Bootstrap | Giao diện responsive | ![Bootstrap](https://img.shields.io/badge/Bootstrap-5-purple) |
| UI Layout | Sitemesh Decorator | Template layout cho toàn hệ thống | ![Sitemesh](https://img.shields.io/badge/Sitemesh-Decorator-lightgrey) |
| Database | SQL Server | Lưu trữ dữ liệu hệ thống | ![SQL](https://img.shields.io/badge/SQL-Server-blue) |
| SMTP | Gmail API | Gửi Email OTP & thông báo đơn hàng | ![Email](https://img.shields.io/badge/Email-OTP-red) |

## ✨ Chức năng chính

- **Quản lý tài khoản:** Đăng ký, đăng nhập, quên mật khẩu (OTP qua Email), kích hoạt tài khoản, mã hóa mật khẩu bằng Spring Security.
- **Phân quyền:** Guest, User (khách hàng), Admin, Manager (người quản lý cửa hàng), Shipper.
- **Quản lý sản phẩm & danh mục:** Thêm, sửa, xóa, quản lý hình ảnh, phân loại theo thương hiệu.
- **Giỏ hàng & đặt hàng:** Thêm vào giỏ, thanh toán, quản lý trạng thái đơn.
- **Quản lý đơn hàng:** Tạo / cập nhật trạng thái, phân công Shipper, quản lý hoàn trả / refund.
- **Quản lý khuyến mãi:** Tạo mã giảm giá, chương trình khuyến mãi, áp dụng theo đơn hàng, hoặc sản phẩm.
- **Đánh giá & phản hồi:** Người dùng có thể đánh giá sản phẩm; Manager có thể quản lý đánh giá.
- **Thống kê & báo cáo:** Doanh thu, đơn hàng theo thời gian, hiệu suất giao hàng.
- **Thông báo & Email tự động:** Gửi OTP, thông báo trạng thái đơn, mail xác nhận đặt hàng.

## 🚀 Cách sử dụng

**Khởi động hệ thống:**
1. Cấu hình file `application.properties` với thông tin database, SMTP, JWT secret, và port.
2. Chạy ứng dụng bằng IDE (IntelliJ/STS) hoặc dùng `mvn spring-boot:run`.
3. Insert dữ liệu database từ file SQL vào SQL Server.
4. Truy cập: `http://localhost:8083`

**Hướng dẫn theo vai trò**

- **Guest (Khách chưa đăng nhập):**
  - Truy cập trang chủ shop.
  - Xem và tìm kiếm sản phẩm theo tên.
  - Đăng ký / đăng nhập để đặt hàng.

- **User (Khách hàng):**
  - Đăng ký hoặc đăng nhập (OTP kích hoạt qua Email).
  - Chọn sản phẩm → Thêm vào giỏ hàng → Thanh toán.
  - Xem lịch sử đơn hàng, theo dõi trạng thái, đánh giá sản phẩm.
  - Quản lý thông tin cá nhân.

- **Admin (Quản trị viên):**
  - Truy cập trang quản trị toàn hệ thống.
  - Quản lý người dùng: thêm/sửa/xóa và phân quyền người dùng trong hệ thống
  - Quản lý nhà vận chuyển: thêm/sửa/xóa nhà vận chuyển.
  - Quản lý danh mục: thêm/sửa/xóa danh mục sản phẩm.
  - Quản lý các chương trình khuyến mãi.

- **Manager (Người quản lý cửa hàng):**
  - Đăng nhập vào trang quản lý của Manager.
  - Quản lý sản phẩm cửa hàng: thêm/sửa/xóa sản phẩm, quản lý tồn kho.
  - Quản lý đơn hàng cửa hàng: xử lý đơn, thay đổi trạng thái, phân công giao hàng.
  - Quản lý mã khuyến mãi: thêm/sửa/xóa khuyến mãi.
  - Xem thống kê doanh thu tổng quan, quản lý đánh giá khách hàng.

- **Shipper:**
  -  Đăng nhập vào giao diện Shipper.
  -  Xem các đơn hàng được giao, nhận đơn và cập nhật trạng thái giao.
  -  Xem thống kê hiệu suất giao hàng theo thời gian.

## 🔐 Tài khoản và phân quyền trong hệ thống

Hệ thống hỗ trợ nhiều vai trò, mỗi vai trò có quyền hạn khác nhau nhằm đảm bảo phân tách nghiệp vụ rõ ràng.

| Vai trò | Quyền hạn chính | Gán khi đăng ký |
|---|---|:---:|
| **Admin** | Quản trị toàn hệ thống, phân quyền và quản lý tài khoản | ❌ |
| **Manager** | Quản lý sản phẩm, đơn hàng, khuyến mãi, cửa hàng | ❌ |
| **Shipper** | Cập nhật trạng thái giao hàng, xác nhận giao/hoàn đơn | ❌ |
| **User** | Mua hàng, đặt đơn và quản lý tài khoản cá nhân | ✅ |

> Khi đăng ký tài khoản mới, hệ thống **tự động gán vai trò User**.  
> Việc tạo/quản lý các tài khoản Admin, Manager, Shipper sẽ do **Admin** thực hiện trong trang quản trị.
> Có thể thay đổi theo cấu hình thực tế.

## 📞 Liên hệ

### 👥 Nhóm thực hiện

| STT | Họ và Tên | MSSV |
|---:|---|---|
| 1 | Dương Quỳnh Như | 23110281 |
| 2 | Võ Minh Xuân Kiều | 23110245 |
| 3 | Nguyễn Thị Quyến | 23110297 |
| 4 | Trần Thị Như Quỳnh | 23110299 |

### 👨‍🏫 Giảng viên hướng dẫn
- ThS. Nguyễn Hữu Trung

### 📌 GitHub Repository
- https://github.com/Qnhu22/Nhom18_MyPham_EighteenBeauty.git
