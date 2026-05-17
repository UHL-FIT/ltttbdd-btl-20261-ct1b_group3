# TÀI LIỆU QUY ƯỚC VÀ YÊU CẦU DỰ ÁN: LEARNFLASH

## I. THÔNG TIN CHUNG
- Tên đề tài: learnflash - Học từ vựng.
- Nền tảng phát triển: Android Studio.
- Ngôn ngữ & Build System: Kotlin DSL.
- SDK tối thiểu: API 24 (Android 7.0 Nougat).
- Package Name: com.example.learnflash.
- Kiến trúc áp dụng: MVVM (Model - View - ViewModel) kết hợp Repository Pattern.

## II. MỤC TIÊU & CHỨC NĂNG HỆ THỐNG
1. Nguồn dữ liệu đầu vào: Từ vựng do người dùng tự khởi tạo (local) hoặc lấy từ Free Dictionary API (remote).
2. Thuật toán cốt lõi: Học theo phương pháp lặp ngắt quãng (Spaced Repetition).
3. Nghiệp vụ lưu trữ & Đồng bộ:
   - Lưu trữ offline: Room Database (dữ liệu bộ từ vựng và lịch sử ôn tập).
   - Lưu trữ cấu hình: Jetpack DataStore (cài đặt ứng dụng của người dùng).
   - Kết nối mạng: Retrofit xử lý REST API để tra nghĩa từ trực tuyến. 
   - Khả năng hoạt động: Đảm bảo ứng dụng hoạt động được khi không có kết nối mạng (Offline-first).
4. Xử lý UI & Luồng dữ liệu:
   - UI xây dựng bằng Jetpack Compose (Composable functions), áp dụng Material Design 3 thống nhất.
   - Quản lý logic lật thẻ bằng State.
   - Luồng dữ liệu một chiều (UDF): Dữ liệu chảy từ ViewModel (StateFlow) xuống UI. UI truyền sự kiện (Event) lên ViewModel.
   - Quản lý trạng thái xử lý tác vụ: Hiển thị trạng thái Loading/Error rõ ràng. Có màn hình chờ/tiến trình đối với tác vụ xử lý trên 3 giây.
5. Chức năng bắt buộc:
   - Thao tác dữ liệu: Thêm, Sửa, Xóa (CRUD) từ vựng.
   - Màn hình bổ sung: Màn hình Giới thiệu ứng dụng (About screen). Có nút mở file PDF hướng dẫn sử dụng.
   - Xuất/Nhập dữ liệu: Hỗ trợ import/export dữ liệu dạng CSV hoặc JSON.
   - Thống kê (Logic Kotlin): Tổng số bản ghi, giá trị trung bình, Min/Max, phân loại số từ đã thuộc/chưa thuộc, ôn tập hàng ngày.
   - Kiểm tra hợp lệ (Validation): Xác thực dữ liệu đầu vào, thông báo lỗi qua SnackBar, AlertDialog hoặc TextField error.

## III. YÊU CẦU KỸ THUẬT & ĐIỀU HƯỚNG UI
1. Màn hình & Điều hướng: Có nhiều hơn 3 màn hình, điều hướng bằng Navigation Compose, nút Back hệ thống phải hoạt động hợp lý.
2. Danh sách: Toàn bộ danh sách lặp phải sử dụng LazyColumn hoặc LazyGrid.
3. Vòng đời (Lifecycle): Cần override và ghi log (Logcat) ít nhất 4 callback vòng đời của Activity phục vụ báo cáo minh họa.

## IV. QUY TẮC ĐẶT TÊN & CODING CONVENTIONS (BẮT BUỘC)
1. Quy tắc đặt tên file, biến, hàm: Sử dụng TIẾNG VIỆT KHÔNG DẤU, đặt tên tường minh theo công dụng chức năng của thành phần kỹ thuật.
2. Quy tắc chú thích (Comment):
   - Mỗi khối giao diện, chức năng hoặc đoạn xử lý logic bắt buộc phải có chú thích mô tả công dụng trước khi viết code.
   - Định dạng bắt buộc: Dòng trên là chú thích tiếng Việt sử dụng ký hiệu `//`. Dòng dưới là code.
   - Tuyệt đối không viết chú thích bên cạnh, cuối dòng code, hoặc viết code xong mới chú thích phía dưới.
3. Quy tắc giải thích kỹ thuật:
   - Sử dụng thuật ngữ chuyên môn chính xác (StateFlow, Observable, Callback, Instance, Repository,...).
   - Giải thích ngắn gọn cơ chế hoạt động thực tế, không dùng các ví dụ nhân hóa (không dùng các từ như "bộ não", "đầu bếp", "ông quản gia", "người gác cổng",...).

   ## V. CÂY THƯ MỤC DỰ KIẾN :
   com.example.learnflash/
│
├── duLieu/                                 // Lớp Dữ liệu (Data Layer)
│   ├── local/                              // Lưu trữ nội bộ ứng dụng
│   │   ├── database/                       // Cấu hình Room Database
│   │   ├── dao/                            // Các Interface Data Access Object (DAO)
│   │   ├── thucThe/                        // Các Entity (Bảng dữ liệu Room) như TuVung, LichSuOnTap
│   │   └── dataStore/                      // Cấu hình Jetpack DataStore lưu cài đặt
│   │
│   ├── remote/                             // Kết nối dữ liệu bên ngoài qua mạng
│   │   ├── api/                            // Định nghĩa Interface Retrofit cho Free Dictionary API
│   │   └── moHinhReMote/                   // Các Data Class hứng dữ liệu JSON từ API trả về
│   │
│   └── khoDuLieu/                          // Lớp Repository làm trung gian điều phối dữ liệu
│       └── KhoDuLieuTuVung.kt              // Repository quản lý việc lấy dữ liệu từ Local hay Remote
│
├── giaoDien/                               // Lớp Hiển thị & Xử lý UI (UI Layer)
│   ├── manHinhChinh/                       // Màn hình chính/Danh sách từ vựng
│   │   ├── ManHinhChinhUI.kt               // Các Composable Function giao diện
│   │   └── ManHinhChinhViewModel.kt        // ViewModel quản lý State và Logic của màn hình chính
│   │
│   ├── chiTietTuVung/                      // Màn hình xem chi tiết / Thêm / Sửa / Xóa (CRUD)
│   │   ├── ChiTietUI.kt
│   │   └── ChiTietViewModel.kt
│   │
│   ├── onTapTheFlash/                      // Màn hình học từ vựng Spaced Repetition (Lật thẻ)
│   │   ├── OnTapUI.kt
│   │   └── OnTapViewModel.kt
│   │
│   ├── gioiThieu/                          // Màn hình giới thiệu ứng dụng (About screen)
│   │   └── GioiThieuUI.kt
│   │
│   └── thanhPhanChung/                     // Các thành phần UI dùng chung cho toàn bộ app
│       └── CacThanhPhanGiaoDien.kt         // Các nút, hộp thoại lỗi (AlertDialog), SnackBar dùng chung
│
├── dieuHuong/                              // Quản lý chuyển màn hình
│   └── DieuHuongCumManhinh.kt              // Cấu hình NavHost, các Route điều hướng (Navigation Compose)
│
├── tieuChuanGiaoDien/                      // Cấu hình giao diện Material Design 3
│   ├── MauSac.kt                           // Định nghĩa bảng màu (Color.kt)
│   ├── KieuChu.kt                          // Định nghĩa font chữ (Type.kt)
│   └── GiaoDienUngDung.kt                  // Định nghĩa cấu hình Theme chung (Theme.kt)
│
└── MainActivity.kt                         // Cửa sổ chính, nơi override các Lifecycle callback để ghi log