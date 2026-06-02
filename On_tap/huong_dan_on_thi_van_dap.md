# CẨM NANG ÔN THI VẤN ĐÁP & HƯỚNG DẪN HIỂU CODE
## Dự án: Phần mềm Quản lý Cho thuê Trang phục (ChoThueTP)
*Nền tảng: C# WinForms, .NET Framework 4.7.2, SQL Server (ADO.NET)*

---

## PHẦN 1: BẢN ĐỒ KIẾN TRÚC DỰ ÁN (3-TIER ARCHITECTURE)

Dự án này áp dụng kiến trúc **3 lớp (3-Tier)** nghiêm ngặt để phân tách rõ ràng trách nhiệm của từng phần code.

```
+------------------------------------------+
|       GUI (Presentation Layer)           |
|  - Dangnhap.cs, TrangPhuc.cs, ...        |
+-------------------+----------------------+
                    |
                    | 1. Gọi nghiệp vụ & truyền DTO
                    v
+-------------------+----------------------+
|        BLL (Business Logic Layer)        |
|  - TrangPhucBLL.cs, PhieuThueBLL.cs, ... |
+-------------------+----------------------+
                    |
                    | 2. Gọi truy xuất database
                    v
+-------------------+----------------------+
|          DAL (Data Access Layer)         |
|  - TrangPhucDAL.cs, PhieuThueDAL.cs, ... |
+-------------------+----------------------+
                    |
                    | 3. Thực thi truy vấn SQL
                    v
+-------------------+----------------------+
|            SQL Server Database           |
+------------------------------------------+
```
*(Lưu ý: Lớp **DTO** chứa các class như `TrangPhucDTO`, `PhieuThueDTO`... đóng vai trò làm khuôn mẫu dữ liệu để truyền qua lại giữa cả 3 lớp trên).*

### 1. Vai trò của từng lớp (Phải học thuộc để trả lời thầy cô)
*   **GUI (Presentation Layer)**: Chứa các Form giao diện (`TrangPhuc.cs`, `PhieuThue.cs`...). Chỉ nhận dữ liệu từ người dùng, gọi BLL xử lý và hiển thị thông báo (`MessageBox`). **Tuyệt đối không có một dòng lệnh SQL nào ở GUI.**
*   **BLL (Business Logic Layer)**: Đóng vai trò "bộ não" kiểm soát luật chơi. Ví dụ: kiểm tra xem đơn giá thuê có âm không, số lượng tồn kho có đủ cho thuê không. Nếu sai quy tắc, BLL sẽ ném ra lỗi (`throw new Exception`).
*   **DAL (Data Access Layer)**: Lớp duy nhất tương tác trực tiếp với SQL Server thông qua ADO.NET (thêm, sửa, xóa, truy vấn dữ liệu).
*   **DTO (Data Transfer Object)**: Các lớp chứa thuộc tính mô tả các thực thể (như `TrangPhucDTO`, `PhieuThueDTO`...). Chúng đóng vai trò là "khuôn mẫu" để đóng gói dữ liệu và truyền qua truyền lại giữa 3 tầng trên.

---

## PHẦN 2: THUẬT NGỮ ADO.NET CƠ BẢN (ĐỊNH NGHĨA KỸ THUẬT CHÍNH XÁC)

Dưới đây là các thuật ngữ ADO.NET cốt lõi được sử dụng để giao tiếp cơ sở dữ liệu trong dự án của bạn:

| Thuật ngữ | Khái niệm kỹ thuật | Vai trò thực tế trong dự án |
| :--- | :--- | :--- |
| **SqlConnection** | Đối tượng quản lý kết nối vật lý từ ứng dụng đến cơ sở dữ liệu SQL Server. | Thực hiện đóng/mở kết nối đến cơ sở dữ liệu khi thực thi các tác vụ SQL thông qua khối lệnh `using`. |
| **SqlCommand** | Đối tượng chứa mã lệnh SQL (truy vấn dữ liệu hoặc thay đổi cấu trúc/dữ liệu) để thực thi trên máy chủ SQL Server. | Chứa nội dung truy vấn SQL và quản lý danh sách các tham số đầu vào (Parameters) để thực thi. |
| **SqlDataReader** | Đối tượng hỗ trợ đọc dữ liệu theo cơ chế chỉ tiến (Forward-Only) và chỉ đọc (Read-Only) trực tiếp từ luồng kết nối đang mở. | Sử dụng vòng lặp `Read()` để duyệt nhanh các dòng kết quả trả về từ cơ sở dữ liệu. |
| **SqlDataAdapter** | Đối tượng trung gian thực hiện ánh xạ và trao đổi dữ liệu giữa cơ sở dữ liệu và DataSet/DataTable (Disconnected Model). | Sử dụng phương thức `Fill` để nạp dữ liệu vào DataSet và phương thức `Update` để lưu ngược các thay đổi xuống SQL Server. |
| **DataSet** | Đối tượng đại diện cho một cơ sở dữ liệu ảo nằm trong bộ nhớ RAM, có thể chứa nhiều bảng dữ liệu và mối quan hệ liên kết giữa chúng. | Dùng làm bộ lưu trữ tạm thời trong bộ nhớ của lớp `MyDatabase` cho các bảng như Trang phục, Loại trang phục, Người dùng. |
| **DataTable** | Đối tượng đại diện cho một bảng dữ liệu đơn lẻ lưu trữ trong bộ nhớ RAM, bao gồm danh sách các cột (`DataColumn`) và các hàng (`DataRow`). | Chứa kết quả dữ liệu trả về từ các truy vấn để gán vào thuộc tính `DataSource` của các DataGridView hiển thị giao diện. |
| **DataRow** | Đối tượng đại diện cho một bản ghi dữ liệu (một dòng) cụ thể nằm trong DataTable. | Dùng để truy xuất giá trị của một bản ghi hoặc tạo mới một bản ghi để thêm vào DataTable. |
| **SqlCommandBuilder** | Đối tượng tự động sinh các lệnh SQL (INSERT, UPDATE, DELETE) tương ứng từ câu lệnh SELECT gốc cấu hình cho SqlDataAdapter. | Giúp tự động hóa thao tác cập nhật dữ liệu của các bảng danh mục mà không cần lập trình viên viết tay các câu lệnh SQL sửa đổi. |
| **SqlTransaction** | Đối tượng quản lý một giao dịch cơ sở dữ liệu, nhóm nhiều thao tác SQL lại với nhau để thực thi như một đơn vị công việc duy nhất. | Đảm bảo tính toàn vẹn của dữ liệu khi lập phiếu thuê/phiếu trả (nếu một bước ghi lỗi, toàn bộ giao dịch sẽ được Rollback). |

---

## PHẦN 3: GIẢI THÍCH CÁC KHỐI CODE LẠ & CƠ CHẾ KỸ THUẬT PHỨC TẠP

### 1. Giao dịch an toàn (SQL Transaction)
**Chỗ xuất hiện trong bài**: Lập phiếu thuê (`ThemPhieuThue` ở `PhieuThueDAL.cs`) và trả đồ (`ThemPhieuTra` ở `PhieuTraDAL.cs`).

*   **Tại sao phải dùng?** 
    Khi lập một Phiếu thuê, hệ thống phải thực hiện 3 hành động: 
    1. Thêm khách hàng mới (nếu chưa có).
    2. Tạo 1 dòng Phiếu thuê mới.
    3. Tạo nhiều dòng chi tiết đồ thuê và trừ số lượng trong kho.
    
    Nếu bước 1 và 2 thành công nhưng bước 3 bị lỗi (ví dụ: mất điện, lỗi cơ sở dữ liệu), hệ thống sẽ bị **rác dữ liệu** (phiếu thuê tồn tại nhưng không có đồ thuê, kho không được trừ). **Transaction** giúp gộp tất cả các bước này thành 1 khối duy nhất: **"Hoặc tất cả cùng thành công, hoặc tất cả cùng thất bại"**.
*   **Khối code thực tế**:
    ```csharp
    using (SqlTransaction giaodich = ketnoi.BeginTransaction())
    {
        try {
            // Thực hiện bước 1, 2, 3...
            giaodich.Commit(); // ==> ĐỒNG Ý LƯU toàn bộ thay đổi xuống DB khi mọi thứ không lỗi
            return true;
        }
        catch {
            giaodich.Rollback(); // ==> HỦY BỎ toàn bộ các bước đã làm nếu có bất kỳ lỗi nào xảy ra
            throw;
        }
    }
    ```

### 2. Khối lệnh `using` trong ADO.NET
**Chỗ xuất hiện**: Trong mọi hàm kết nối SQL ở tầng DAL (như `using (SqlConnection conn = ...)`).
*   **Công dụng**: Tự động giải phóng bộ nhớ và **đóng kết nối** tới SQL Server ngay sau khi khối ngoặc nhọn `{ }` kết thúc, bất kể code bên trong chạy thành công hay bị lỗi sập.
*   **Tại sao quan trọng?** Nếu không dùng `using`, kết nối tới SQL Server sẽ bị treo mở. Sau một thời gian ngắn, SQL Server sẽ bị đầy kết nối (Max Pool Size reached) và phần mềm sẽ bị treo không thể truy cập được nữa.

### 3. Đọc và lưu hình ảnh an toàn (Safe Image Stream)
**Chỗ xuất hiện**: Module trang phục (TrangPhuc.cs).
*   **Vấn đề**: Thông thường dùng `Image.FromFile()` sẽ làm Windows **khóa (lock) chặt** file ảnh đó trên ổ cứng. Khi người dùng muốn đổi ảnh khác hoặc xóa ảnh, Windows sẽ báo lỗi *"File is in use by another process"*.
*   **Khối code giải quyết (Dùng Stream)**:
    ```csharp
    using (FileStream fs = new FileStream(duongdananh, FileMode.Open, FileAccess.Read))
    {
        using (Image temp = Image.FromStream(fs))
        {
            ptb_hinhanh.Image = new Bitmap(temp); // Tạo một bản sao độc lập trong RAM để hiển thị
        } // fs tự động đóng và giải phóng file ảnh vật lý trên ổ cứng ngay tại đây
    }
    ```

### 4. Cơ chế lưu ảnh trì hoãn (Deferred Image Copying)
*   **Vấn đề**: Nếu người dùng nhấn chọn ảnh liên tục để thử xem trước (Preview) trên màn hình mà copy ảnh vào thư mục dự án ngay lập tức, thư mục sẽ bị tràn ngập ảnh rác do người dùng chọn thử mà không lưu.
*   **Cách giải quyết**: 
    1. Khi người dùng bấm nút Chọn ảnh, hệ thống chỉ lưu đường dẫn tạm thời vào biến `tempSourceImagePath` và nạp ảnh lên màn hình.
    2. Chỉ khi người dùng thực sự bấm nút **Thêm** hoặc **Cập nhật** thành công xuống cơ sở dữ liệu, hàm `ThucHienSaoChepAnh()` mới được gọi để copy file ảnh thực tế từ vị trí tạm vào thư mục dự án.

---

## PHẦN 4: BỘ CÂU HỎI VẤN ĐÁP THƯỜNG GẶP (Q&A)

### Nhóm 1: Câu hỏi về Kiến trúc & Thiết kế

#### ❓ Câu 1: Tại sao bạn lại chọn kiến trúc 3 lớp (3-Tier)? Gộp hết code SQL viết vào code behind của Form (GUI) có được không?
*   **Trả lời**: 
    *   **Lý do chia 3 lớp**: Giúp dự án dễ bảo trì, dễ nâng cấp và dễ làm việc nhóm. Nếu sau này muốn đổi giao diện (từ WinForms sang Web/Mobile), chỉ cần sửa lại tầng GUI, toàn bộ logic nghiệp vụ (BLL) và kết nối database (DAL) được giữ nguyên.
    *   **Nếu gộp chung**: Code behind sẽ cực kỳ dài dòng, rối rắm. Khi xảy ra lỗi sẽ rất khó debug và không thể tái sử dụng lại code truy vấn database ở các màn hình khác nhau.

#### ❓ Câu 2: Làm sao để truyền dữ liệu từ GUI xuống DAL mà không vi phạm quy tắc gọi chéo lớp?
*   **Trả lời**: Sử dụng các đối tượng **DTO** (Data Transfer Object). Ví dụ: Khi người dùng điền thông tin trang phục ở GUI, GUI sẽ đóng gói toàn bộ thông tin đó vào một đối tượng `TrangPhucDTO` rồi truyền nó làm tham số cho hàm của BLL, BLL tiếp tục truyền cục DTO đó xuống DAL để thực thi câu lệnh SQL.

#### ❓ Câu 3: Thuộc tính `[STAThread]` trong file `Program.cs` có công dụng gì?
*   **Trả lời**: Viết tắt của **Single-Threaded Apartment**. Nó bắt buộc Windows Forms chạy giao diện người dùng trên một luồng chính duy nhất. Đồng thời, nó kích hoạt các dịch vụ tương tác của hệ điều hành Windows như: hộp thoại chọn file `OpenFileDialog` (dùng để chọn ảnh), tính năng Sao chép/Dán (Clipboard), kéo thả file. Nếu không có thuộc tính này, ứng dụng sẽ lỗi sập khi mở hộp thoại chọn file.

#### ❓ Câu 4: Lớp tĩnh `Session` ở tầng DTO có vai trò gì trong ứng dụng?
*   **Trả lời**: Lớp tĩnh `Session` lưu trữ thông tin của tài khoản người dùng đang đăng nhập vào hệ thống (như tên đăng nhập, tên hiển thị, quyền hạn). Vì là lớp tĩnh (`static`), dữ liệu này được lưu giữ xuyên suốt phiên làm việc của phần mềm và tất cả các Form đều có thể truy xuất để phân quyền sử dụng (Ví dụ: chỉ tài khoản có quyền Admin mới mở được màn hình Quản lý người dùng).

#### ❓ Câu 5: Lớp tiện ích `UIHelper` dùng để làm gì? Kể tên một số phương thức chính bạn tự xây dựng trong đó?
*   **Trả lời**: Lớp `UIHelper` chứa các phương thức tĩnh dùng chung để chuẩn hóa giao diện WinForms, giảm thiểu mã nguồn trùng lặp. Một số hàm chính gồm:
    *   `CauHinhDataGridView`: Cấu hình tự động căn lề, tự động co giãn cột, đổi màu hàng xen kẽ và ẩn cột khóa chính.
    *   `FillComboBox`: Nạp dữ liệu tự động từ DataTable vào ComboBox, thiết lập `DisplayMember` và `ValueMember`.
    *   `GiaiPhongAnh`: Thu hồi vùng nhớ Bitmap trên PictureBox trước khi nạp ảnh mới để tránh rò rỉ bộ nhớ.

#### ❓ Câu 6: Tại sao lại cần kiểm tra (validate) dữ liệu đầu vào (như bỏ trống thông tin, giá âm) ở cả tầng GUI lẫn tầng BLL? Việc này có bị dư thừa không?
*   **Trả lời**: Việc kiểm tra này không dư thừa vì có hai mục đích khác nhau:
    *   **Validate tại GUI**: Phản hồi nhanh cho người dùng ngay tại giao diện mà không cần gọi xuống hệ thống dưới, cải thiện trải nghiệm sử dụng (User Experience).
    *   **Validate tại BLL**: Bảo vệ tính toàn vẹn nghiệp vụ của phần mềm ở tầng dưới. Điều này phòng trường hợp có nhiều Form khác nhau cùng gọi chung phương thức nghiệp vụ, hoặc khi kiểm tra dữ liệu hệ thống tự động, đảm bảo dữ liệu ghi xuống database luôn sạch và đúng luật.

#### ❓ Câu 7: Form `Trangchu` và Form `Dashboard` có thể gộp lại với nhau không? Tại sao lại không gộp?
*   **Trả lời**: 
    **Không nên gộp hai Form này làm một**, vì vi phạm nguyên tắc thiết kế giao diện WinForms hiện đại:
    *   **Nguyên tắc đơn nhiệm (Single Responsibility)**: 
        *   Form `Trangchu` đóng vai trò là **khung sườn chính (Shell Container)** của hệ thống, chỉ làm nhiệm vụ quản lý thanh trình đơn điều hướng (Sidebar), thanh tiêu đề và điều phối việc hiển thị các Form con khác vào khu vực trung tâm (`PanelMain`).
        *   Form `Dashboard` đóng vai trò là một **màn hình nội dung (Content Form)**, có chức năng riêng biệt là truy vấn tổng hợp và hiển thị các số liệu thống kê (doanh thu, quá hạn, biểu đồ...).
    *   **Khả năng linh hoạt & bảo trì**: Khi tách riêng, `Dashboard` có thể dễ dàng được nhúng vào hoặc gỡ ra khỏi khung `Trangchu` giống như các Form con khác (`TrangPhuc`, `PhieuThue`, `PhieuTra`). Nếu gộp chung, Form `Trangchu` sẽ chứa quá nhiều controls và code xử lý chồng chéo, làm giảm hiệu năng khởi chạy ứng dụng và cực kỳ khó khăn khi muốn nâng cấp hoặc thay đổi bố cục giao diện.

---

### Nhóm 2: Câu hỏi về Cơ sở dữ liệu & ADO.NET

#### ❓ Câu 8: Dự án này sử dụng mô hình kết nối Connected hay Disconnected trong ADO.NET? Nó hoạt động thế nào?
*   **Trả lời**: Dự án sử dụng kết hợp cả hai:
    *   **Disconnected Model (Mô hình không duy trì kết nối)**: Sử dụng thông qua lớp `MyDatabase` chứa `DataSet` và `SqlDataAdapter` cho các bảng danh mục thông thường (Loại trang phục, Trang phục...). Dữ liệu được nạp vào RAM máy trạm, thao tác thêm/sửa/xóa thực hiện trên `DataRow` của `DataTable`, sau đó gọi `DataAdapter.Update()` để đồng bộ một lần xuống Database SQL Server.
    *   **Connected Model (Mô hình kết nối trực tiếp)**: Sử dụng thông qua `SqlConnection`, `SqlCommand`, `SqlDataReader` cho các tác vụ cần tốc độ nhanh hoặc các giao dịch phức tạp cần Transaction (như lập phiếu thuê/trả). Kết nối được mở ra, thực thi lệnh SQL ngay lập tức, rồi đóng lại bằng khối `using`.

#### ❓ Câu 9: Khi xóa một trang phục đang có trong CSDL, làm thế nào để tránh lỗi ràng buộc khóa ngoại (Foreign Key Violation)?
*   **Trả lời**: 
    Trước khi xóa Trang phục (bảng `TRANGPHUC`), ở tầng DAL, ta phải kiểm tra xem mã trang phục đó đã từng phát sinh trong chi tiết phiếu thuê (bảng `CHITIETPHIEUTHUE`) chưa.
    *   Nếu **đã phát sinh**: BLL sẽ chặn lại và ném ra lỗi thông báo: *"Trang phục đã có lịch sử thuê, không được xóa!"*.
    *   Nếu **chưa phát sinh**: Hệ thống tiến hành xóa bình thường.

#### ❓ Câu 10: Tại sao bạn lại truyền các giá trị tham số vào SqlCommand thông qua `Parameters.AddWithValue` thay vì cộng chuỗi trực tiếp (ví dụ: `WHERE MaTP = ` + txt_ma.Text)?
*   **Trả lời**: 
    *   **Bảo mật**: Ngăn chặn hoàn toàn lỗi tấn công **SQL Injection** (hacker cố tình nhập các đoạn mã độc SQL vào ô nhập liệu để phá hủy database).
    *   **Hiệu năng & Cú pháp**: Tránh lỗi biên dịch SQL khi dữ liệu nhập có ký tự đặc biệt (như dấu nháy đơn `'`), giúp SQL Server phân tích và tối ưu hóa kế hoạch thực thi câu lệnh tốt hơn.

#### ❓ Câu 11: Phân biệt sự khác nhau và cách dùng của 3 phương thức: `ExecuteNonQuery`, `ExecuteScalar`, và `ExecuteReader` trong SqlCommand?
*   **Trả lời**: 
    *   `ExecuteNonQuery`: Dùng cho các câu lệnh SQL thay đổi dữ liệu (INSERT, UPDATE, DELETE). Trả về số dòng dữ liệu bị ảnh hưởng.
    *   `ExecuteScalar`: Thực thi và trả về một giá trị duy nhất ở ô đầu tiên của dòng đầu tiên trong kết quả truy vấn (thường dùng cho lệnh đếm `COUNT(*)` hoặc lấy ID tự tăng vừa tạo `SCOPE_IDENTITY()`).
    *   `ExecuteReader`: Trả về một đối tượng `SqlDataReader` dùng để duyệt đọc luồng dữ liệu tiến một chiều với tốc độ cao.

#### ❓ Câu 12: Trong mô hình ngắt kết nối (Disconnected), khi người dùng chỉnh sửa dữ liệu trên DataGridView, cơ chế nào giúp đồng bộ dữ liệu đó xuống cơ sở dữ liệu vật lý?
*   **Trả lời**: 
    DataGridView được liên kết trực tiếp với một `DataTable` thông qua thuộc tính `DataSource`. Khi người dùng sửa dữ liệu trên lưới, trạng thái của các dòng tương ứng trong `DataTable` sẽ tự động chuyển thành `Modified`. Khi ta gọi lệnh `bophanap.Update(ds, tenbang)` của `SqlDataAdapter`, Adapter sẽ tự động duyệt qua các dòng trong `DataTable`, phát hiện các dòng có trạng thái `Modified` và thực thi câu lệnh UPDATE tự động được tạo bởi `SqlCommandBuilder` xuống SQL Server.

---

### Nhóm 3: Câu hỏi về Logic & Nghiệp vụ xử lý

#### ❓ Câu 13: Mật khẩu người dùng được lưu trữ như thế nào trong cơ sở dữ liệu để đảm bảo bảo mật?
*   **Trả lời**: 
    Mật khẩu không được lưu dưới dạng văn bản thuần túy (Clear Text). Dự án sử dụng lớp `BaoMat.cs` ở tầng DAL để băm mật khẩu bằng thuật toán **SHA-256** thành một mảng các byte (`byte[]`) trước khi ghi vào SQL Server (cột kiểu dữ liệu `VARBINARY`). Khi kiểm tra đăng nhập, hệ thống sẽ băm mật khẩu do người dùng nhập vào rồi so sánh chuỗi byte băm đó với chuỗi byte trong database.

#### ❓ Câu 14: Giải thích cơ chế tính tiền phạt và tổng tiền khi khách hàng trả trang phục?
*   **Trả lời**: 
    Trong Form trả trang phục (`PhieuTra.cs`), hệ thống lấy `Ngày trả thực tế` trừ đi `Ngày hẹn trả` (sử dụng hàm tính toán khoảng cách ngày của C# hoặc hàm `DATEDIFF` của SQL Server):
    *   Nếu `Số ngày quá hạn > 0`, tiền phạt được tính theo công thức: `Tiền phạt = Số ngày quá hạn * Đơn giá phạt quy định`.
    *   Tổng thanh toán thực tế của khách hàng khi trả đồ là: `Tổng thanh toán = Tổng tiền thuê + Tiền phạt - Tiền đặt cọc`.

#### ❓ Câu 15: Tại sao khi chọn ảnh trang phục trên giao diện, bạn không thực hiện sao chép tệp ảnh vào thư mục dự án ngay lập tức?
*   **Trả lời**: 
    Đây là kỹ thuật trì hoãn sao chép tệp tin. Nếu sao chép ngay khi chọn ảnh, trong trường hợp người dùng liên tục thay đổi ảnh để xem thử rồi nhấn nút Hủy không lưu, hệ thống sẽ bị dư thừa rất nhiều ảnh rác. Do đó, hệ thống chỉ lưu đường dẫn tạm trên giao diện, và chỉ thực hiện sao chép file vật lý vào thư mục ứng dụng sau khi câu lệnh lưu thông tin vào database thành công.

#### ❓ Câu 16: Làm thế nào phần mềm kiểm tra và ngăn chặn việc cho thuê vượt quá số lượng tồn kho?
*   **Trả lời**: 
    Trước khi lập phiếu thuê, tầng BLL (`PhieuThueBLL`) sẽ gọi hàm của DAL để lấy số lượng tồn kho thực tế của trang phục đó từ database. BLL tiến hành so sánh: nếu `Số lượng thuê > Số lượng tồn kho`, BLL lập tức dừng xử lý và ném ra ngoại lệ (`throw new Exception("Số lượng trang phục trong kho không đủ!")`). GUI bắt ngoại lệ này và hiển thị cảnh báo cho người dùng.

#### ❓ Câu 17: Khi thực hiện Thêm hoặc Sửa thông tin thành công, tại sao chương trình luôn gọi các hàm `ClearInput()` và `LoadData()`?
*   **Trả lời**: 
    *   `ClearInput()` (hoặc `ClearForm()`): Thực hiện xóa trắng hoặc đưa các controls nhập liệu (TextBox, ComboBox, CheckBox) về trạng thái mặc định, giúp người dùng sẵn sàng thực hiện thao tác nhập tiếp theo.
    *   `LoadData()`: Thực hiện truy vấn lại toàn bộ dữ liệu mới nhất từ SQL Server và gán lại nguồn dữ liệu cho DataGridView, giúp giao diện luôn đồng bộ và hiển thị dữ liệu chính xác nhất.

#### ❓ Câu 18: Làm thế nào phần mềm biết một phiếu thuê đã được trả đồ hay chưa để ngăn việc lập phiếu trả lần hai?
*   **Trả lời**: 
    Trước khi cho phép chọn lập phiếu trả, ở tầng DAL (`PhieuThueDAL`), hệ thống gọi hàm `KiemTraDaTra(maPhieuThue)`. Hàm này thực hiện đếm số lượng dòng trong bảng `PHIEUTRA` có `MaPhieuThue` trùng khớp. Nếu kết quả đếm lớn hơn 0, hệ thống xác định phiếu thuê này đã được thanh lý và tầng GUI sẽ khóa nút hoặc đưa ra cảnh báo chặn không cho phép lập phiếu trả tiếp tục.

---

### Nhóm 4: Câu hỏi về Xuất báo cáo (ClosedXML & iTextSharp)

#### ❓ Câu 19: Làm sao để xuất dữ liệu từ lưới hiển thị (DataGridView) ra file Excel bằng ClosedXML?
*   **Trả lời**: 
    Trong file [XuatBaoCao.cs](file:///e:/LTCSDL/ChoThueTP/GUI/Utils/XuatBaoCao.cs):
    1. Tạo workbook mới: `var wb = new XLWorkbook();`
    2. Tạo bảng tính: `var ws = wb.Worksheets.Add("SheetName");`
    3. Đổ tiêu đề báo cáo, điền ngày lập báo cáo vào các ô chỉ định.
    4. Quét qua các cột của DataGridView để tạo dòng tiêu đề bảng (Header) và định dạng màu nền.
    5. Vòng lặp duyệt qua các dòng của DataGridView để ghi dữ liệu tương ứng vào các ô Excel.
    6. Sử dụng hộp thoại `SaveFileDialog` để cho phép người dùng chọn nơi lưu và lưu workbook: `wb.SaveAs(filename);`

#### ❓ Câu 20: Làm sao để thiết lập viền bảng (Border) cho các ô dữ liệu khi xuất Excel bằng ClosedXML?
*   **Trả lời**: 
    Ta duyệt qua vùng dữ liệu (Range) cần tạo viền trong Excel, sau đó thiết lập thuộc tính kiểu viền:
    ```csharp
    var o = ws.Cell(dong, cot);
    o.Style.Border.OutsideBorder = XLBorderStyleValues.Thin; // Thiết lập viền mỏng bao quanh ô
    ```

#### ❓ Câu 21: Bạn xử lý xuất file PDF như thế nào bằng thư viện iTextSharp?
*   **Trả lời**: 
    1. Khởi tạo đối tượng tài liệu PDF: `Document doc = new Document(PageSize.A4);`
    2. Liên kết tài liệu với luồng ghi file: `PdfWriter.GetInstance(doc, new FileStream(duongdan, FileMode.Create));`
    3. Mở tài liệu: `doc.Open();`
    4. Thêm các đoạn văn bản (Paragraph) làm tiêu đề.
    5. Tạo bảng biểu PDF (`PdfPTable`) với số cột tương ứng với các cột hiển thị trên DataGridView.
    6. Vòng lặp điền tiêu đề cột và nội dung dữ liệu vào các ô của bảng (`PdfPCell`), sau đó thêm bảng vào tài liệu: `doc.Add(pdfTable);`
    7. Đóng tài liệu: `doc.Close();`

#### ❓ Câu 22: Khi xuất file PDF bằng iTextSharp, làm thế nào để hiển thị được tiếng Việt có dấu?
*   **Trả lời**: 
    iTextSharp mặc định không hỗ trợ font tiếng Việt. Ta phải đăng ký font hệ thống (ví dụ font Arial từ Windows):
    ```csharp
    string duongdanfont = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.Fonts), "Arial.ttf");
    BaseFont bf = BaseFont.CreateFont(duongdanfont, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
    Font fontTiengViet = new Font(bf, 12);
    ```
    Khi viết chữ hoặc tạo ô trong PDF, ta truyền đối tượng `fontTiengViet` này vào để văn bản hiển thị đúng tiếng Việt có dấu.
