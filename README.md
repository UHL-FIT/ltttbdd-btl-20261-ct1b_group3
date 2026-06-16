[![Review Assignment Due Date](https://classroom.github.com/assets/deadline-readme-button-22041afd0340ce965d47ae6ef1cefeee28c7c493a6346c4f15d667ab976d596c.svg)](https://classroom.github.com/a/H8V-fdC0)
[![Open in Visual Studio Code](https://classroom.github.com/assets/open-in-vscode-2e0aaae1b6195c2367325f4f02e2d04e9abb55f0b24a779b69b11b9e10269abc.svg)](https://classroom.github.com/online_ide?assignment_repo_id=24142793&assignment_repo_type=AssignmentRepo)
# 📇 LearnFlash – Học Từ Vựng Flashcard – Nhóm 3

Ứng dụng di động giúp người dùng học và ghi nhớ từ vựng tiếng Anh một cách có hệ thống thông qua phương pháp **Flashcard** kết hợp **Thuật toán lặp ngắt quãng (Spaced Repetition System - SRS)**. Cơ sở dữ liệu sử dụng **Room Database** để lưu trữ ngoại tuyến, kết nối đám mây **Firebase Firestore** để đồng bộ và giao diện xây dựng hoàn toàn bằng **Jetpack Compose**.

> **Môn học**: Lập trình trên thiết bị di động · **Nhóm**: 03 · **Trường**: Đại học Hạ Long (UHL)

---

## ✨ Tính năng nổi bật

| # | Tính năng | Mô tả |
|---|-----------|-------|
| 1 | **Quản lý Từ vựng** | Thêm, Sửa, Xóa từ vựng với đầy đủ thông tin: Từ khóa, nghĩa, phiên âm, loại từ, danh mục |
| 2 | **Tra cứu & Dịch tự động** | Sử dụng Kotlin Coroutines gọi song song Free Dictionary API & MyMemory API để tự động điền phiên âm, loại từ và nghĩa |
| 3 | **Học & Ôn tập (SRS)** | Giao diện Flashcard lật thẻ 3D sinh động, hỗ trợ đánh giá ghi nhớ và tự động tính lịch ôn tập tiếp theo |
| 4 | **Quản lý Danh mục** | Phân nhóm từ vựng theo các chủ đề khoa học; lọc danh sách từ vựng linh hoạt theo từng danh mục |
| 5 | **Đồng bộ đám mây** | Cơ chế offline-first, tự động tải và cập nhật gần 2000 từ vựng mặc định từ Firestore khi khởi chạy lần đầu |
| 6 | **Báo cáo Thống kê** | Trực quan hóa tiến độ học tập: tổng số từ đang học, tỷ lệ đã thuộc/chưa thuộc và phân bố cấp độ SRS |
| 7 | **Dark UI & Cài đặt** | Hỗ trợ cấu hình giao diện tối/sáng qua Jetpack DataStore; xuất/nhập dữ liệu định dạng JSON để sao lưu |
| 8 | **Logcat Vòng đời Activity** | Ghi log toàn bộ các callback vòng đời MainActivity giúp theo dõi, kiểm soát và tối ưu tài nguyên |

---

## 📁 Cấu trúc Dự án

```
New folder/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/example/learnflash/
│   │       │   ├── dieuhuong/            # Quản lý luồng chuyển màn hình (Navigation)
│   │       │   ├── duLieu/
│   │       │   │   ├── khoDuLieu/        # Repository Pattern (danh mục & từ vựng)
│   │       │   │   ├── local/            # SQLite Room DB & Jetpack DataStore
│   │       │   │   └── remote/           # Firebase Firestore & Retrofit API Clients
│   │       │   ├── giaoDien/             # Màn hình UI (Jetpack Compose & Material 3)
│   │       │   ├── tieuChuanGiaoDien/    # Theme, Màu sắc, Kiểu chữ (Design System)
│   │       │   └── MainActivity.kt       # Điểm khởi chạy & Ghi log vòng đời Activity
│   │       └── AndroidManifest.xml       # Khai báo cấu hình ứng dụng và quyền
│   ├── build.gradle.kts                  # Cấu hình build & dependencies cấp app
│   └── google-services.json              # File cấu hình kết nối dự án Firebase
├── Bao_cao/                              # Tài liệu báo cáo, slide & HDSD PDF
├── build.gradle.kts                      # Cấu hình Gradle cấp dự án
├── settings.gradle.kts                   # Định nghĩa các module dự án
└── local.properties                      # Đường dẫn SDK cục bộ
```

---

## 🗄️ Cấu trúc Database

Dữ liệu được lưu trong Room Database cục bộ với 3 bảng chính:

### Bảng Từ Vựng (`tu_vung`)

| Cột | Kiểu | Bắt buộc | Mô tả |
|-----|------|----------|-------|
| `id` | INTEGER | ✅ | Khoá chính tự tăng |
| `tuKhoa` | TEXT | ✅ | Từ vựng tiếng Anh |
| `nghiaTiengViet` | TEXT | ✅ | Nghĩa tiếng Việt tương ứng |
| `phienAm` | TEXT | | Phiên âm chuẩn quốc tế (IPA) |
| `loaiTu` | TEXT | | Loại từ (noun, verb, adjective,...) |
| `danhMucId` | TEXT | ✅ | Mã danh mục liên kết (Khóa ngoại) |
| `capDoSrs` | INTEGER | ✅ | Cấp độ lặp ngắt quãng SRS hiện tại (0 -> 5) |
| `ngayOnTapTiepTheo` | INTEGER | ✅ | Mốc thời gian ngày ôn tập tiếp theo |
| `daThuoc` | INTEGER | ✅ | Trạng thái đã thuộc hay chưa (1 = True, 0 = False) |

### Bảng Danh Mục (`danh_muc`)

| Cột | Kiểu | Bắt buộc | Mô tả |
|-----|------|----------|-------|
| `id` | TEXT | ✅ | Khoá chính (Firestore Document ID) |
| `ten` | TEXT | ✅ | Tên chủ đề từ vựng |
| `moTa` | TEXT | | Mô tả ngắn gọn về chủ đề |
| `laMacDinh` | INTEGER | ✅ | Cờ đánh dấu danh mục gốc (1 = True, 0 = False) |

### Bảng Lịch Sử Ôn Tập (`lich_su_on_tap`)

| Cột | Kiểu | Bắt buộc | Mô tả |
|-----|------|----------|-------|
| `id` | INTEGER | ✅ | Khoá chính tự tăng |
| `ngayOnTap` | INTEGER | ✅ | Mốc thời gian thực hiện phiên học |
| `soTuDaHoc` | INTEGER | ✅ | Tổng số từ vựng đã ôn tập trong phiên |
| `soTuDung` | INTEGER | ✅ | Số từ vựng trả lời đúng (Đã thuộc) |
| `soTuSai` | INTEGER | ✅ | Số từ vựng trả lời sai (Chưa thuộc) |

> **Lưu ý:** Database SQLite cục bộ chạy theo cơ chế *offline-first*. Khi khởi chạy ứng dụng lần đầu tiên, dữ liệu danh mục và từ vựng mẫu sẽ được tự động tải từ Cloud Firestore trực tuyến và lưu vào Room DB.

---

## 🚀 Hướng dẫn chạy ứng dụng

> Yêu cầu: Đã cài đặt Android Studio Koala+, SDK Android tương thích (API 24+) và thiết bị thật hoặc máy ảo đã bật USB Debugging.

**Bước 1 – Mở dự án trong Android Studio**
*   Mở Android Studio, chọn **Open** và tìm đến thư mục gốc của dự án.

**Bước 2 – Đồng bộ Gradle**
*   Nhấn nút **Sync Project with Gradle Files** và đợi Android Studio tải các dependencies được khai báo tại [app/build.gradle.kts](app/build.gradle.kts). Đảm bảo cấu hình Gradle JDK sử dụng **Java 11**.

**Bước 3 – Cấu hình Google Services**
*   Đảm bảo tệp tin [google-services.json](app/google-services.json) được đặt chính xác trong thư mục `app/` để kích hoạt kết nối tới Firebase Firestore.

**Bước 4 – Biên dịch và Chạy (Run)**
*   Kết nối thiết bị Android của bạn hoặc khởi động máy ảo Emulator.
*   Nhấn nút **Run 'app'** (`Shift + F10`) để tiến hành biên dịch ứng dụng và cài đặt trực tiếp.

---

## 👥 Tác giả / Contributors

| Tên | Vai trò |
|-----|---------|
| Lê Minh Hoàng | Trưởng nhóm (MSSV: 23DH201067) |
| Phạm Tiến Đạt | Thành viên (MSSV: 23DH201058) |
| Dương Xuân Dũng | Thành viên (MSSV: 23DH201055) |
| Nguyễn Quỳnh Anh | Thành viên (MSSV: 23DH201051) |
| **ThS. Vũ Duy Sơn** | Giảng viên hướng dẫn · vuduyson@daihochalong.edu.vn |

---

