package com.example.learnflash.giaoDien.thongKe

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.learnflash.duLieu.local.thucThe.LichSuOnTap
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Thành phần giao diện cấu thành toàn bộ màn hình Thống kê học tập
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThongKeUI(viewModel: ThongKeViewModel) {
    // Đọc StateFlow từ ViewModel và chuyển thành State để Compose theo dõi
    val thongKe by viewModel.duLieuThongKe.collectAsState()
    val thongKeTheoDanhMuc by viewModel.thongKeTheoDanhMuc.collectAsState()
    val lichSuGanDay by viewModel.lichSuGanDay.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Thống kê học tập",
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // 1. Nhóm hiển thị Tiến trình mục tiêu học tập hàng ngày
            item {
                TheTienDoMucTieuNgay(
                    daOn = thongKe.tongSoTuDaOnHomNay,
                    mucTieu = thongKe.mucTieuHocNgay
                )
            }

            // 2. Nhóm thống kê tổng quan số lượng từ vựng
            item {
                Text(
                    text = "Tổng quan kho từ vựng",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Hàng đầu tiên: Tổng số từ
                    TheThongKeChiTiet(
                        tieuDe = "Tổng số từ vựng",
                        giaTri = "${thongKe.tongSoTu} từ",
                        chuThich = "Tổng lượng từ đang có trong từ điển",
                        icon = Icons.Default.School,
                        mauIcon = MaterialTheme.colorScheme.primary
                    )
                    // Hàng hai cột: Đã thuộc và Chưa thuộc
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TheThongKePhu(
                            tieuDe = "Đã thuộc",
                            giaTri = "${thongKe.soTuDaThuoc} từ",
                            mauSắc = Color(0xFF2E7D32),
                            modifier = Modifier.weight(1f)
                        )
                        TheThongKePhu(
                            tieuDe = "Chưa thuộc",
                            giaTri = "${thongKe.soTuChuaThuoc} từ",
                            mauSắc = Color(0xFFC62828),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 3. Nhóm phân bố cấp độ ghi nhớ SRS
            item {
                Text(
                    text = "Phân bố cấp độ ghi nhớ (SRS)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            item {
                ThePhanBoSrs(
                    tongSo = thongKe.tongSoTu,
                    cap0 = thongKe.soTuSrsCap0,
                    cap1 = thongKe.soTuSrsCap1,
                    cap2 = thongKe.soTuSrsCap2,
                    cap3 = thongKe.soTuSrsCap3,
                    cap4 = thongKe.soTuSrsCap4,
                    cap5 = thongKe.soTuDaThuoc
                )
            }

            // 4. Nhóm lịch sử ôn tập gần đây
            item {
                Text(
                    text = "Hoạt động ôn tập gần đây",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            if (lichSuGanDay.isEmpty()) {
                item {
                    TheHopRong(thongDiep = "Bạn chưa thực hiện phiên ôn tập nào")
                }
            } else {
                items(lichSuGanDay) { lichSu ->
                    TheLichSuHoc(lichSu = lichSu)
                }
            }

            // 5. Nhóm tiến độ theo từng danh mục chủ đề
            item {
                Text(
                    text = "Tiến độ theo danh mục",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            if (thongKeTheoDanhMuc.isEmpty()) {
                item {
                    TheHopRong(thongDiep = "Chưa có dữ liệu danh mục")
                }
            } else {
                items(thongKeTheoDanhMuc) { thongKeDM ->
                    ThanhTienTrinhDanhMuc(thongKeDM)
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

// Thành phần thẻ hiển thị tiến độ mục tiêu ôn tập hàng ngày nổi bật
@Composable
fun TheTienDoMucTieuNgay(daOn: Int, mucTieu: Int) {
    // Tính tỉ lệ phần trăm hoàn thành mục tiêu ngày
    val tiLe = if (mucTieu <= 0) 0f else daOn.toFloat() / mucTieu
    val phanTram = (tiLe * 100).toInt()
    
    // Tạo câu thông điệp động khích lệ tinh thần người học
    val thongDiep = when {
        daOn == 0 -> "Bạn chưa bắt đầu ôn tập hôm nay. Hãy học ngay nào!"
        tiLe < 0.5f -> "Khởi đầu tốt! Hãy ôn thêm một chút nữa nhé!"
        tiLe < 1f -> "Tuyệt vời, bạn sắp hoàn thành mục tiêu ngày rồi!"
        else -> "Chúc mừng! Bạn đã hoàn thành xuất sắc mục tiêu hôm nay! 🎉"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Khung chứa nội dung mô tả mục tiêu chiếm diện tích tối đa còn lại
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "MỤC TIÊU HÔM NAY",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Đã ôn tập: $daOn / $mucTieu từ",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                // Hiển thị phần trăm nổi bật ở góc phải không xuống dòng
                Text(
                    text = "$phanTram%",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    softWrap = false
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Thanh tiến trình LinearProgressIndicator góc bo tròn
            LinearProgressIndicator(
                progress = { tiLe.coerceAtMost(1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            
            Spacer(modifier = Modifier.height(10.dp))
            
            // Dòng thông điệp động khích lệ
            Text(
                text = thongDiep,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// Thành phần thẻ hiển thị thông số thống kê chi tiết kèm Icon
@Composable
fun TheThongKeChiTiet(
    tieuDe: String,
    giaTri: String,
    chuThich: String,
    icon: ImageVector,
    mauIcon: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = mauIcon,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            // Khung chứa tiêu đề và chú thích chiếm diện tích tối đa còn lại
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tieuDe,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = chuThich,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            // Văn bản hiển thị giá trị thống kê cụ thể bên phải không xuống dòng
            Text(
                text = giaTri,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = mauIcon,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

// Thành phần thẻ thống kê phụ gọn gàng
@Composable
fun TheThongKePhu(
    tieuDe: String,
    giaTri: String,
    mauSắc: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = tieuDe,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = giaTri,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = mauSắc
            )
        }
    }
}

// Thành phần hiển thị biểu đồ thanh phân bố các cấp độ SRS
@Composable
fun ThePhanBoSrs(
    tongSo: Int,
    cap0: Int,
    cap1: Int,
    cap2: Int,
    cap3: Int,
    cap4: Int,
    cap5: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Danh sách các cấp độ để lặp qua hiển thị thanh tiến độ
            val danhSachCap = listOf(
                Triple("Cấp 0 (Từ mới)", cap0, Color(0xFF78909C)),
                Triple("Cấp 1 (Mới học)", cap1, Color(0xFFFFB74D)),
                Triple("Cấp 2 (Bắt đầu nhớ)", cap2, Color(0xFFFF8A65)),
                Triple("Cấp 3 (Nhớ khá)", cap3, Color(0xFF4DD0E1)),
                Triple("Cấp 4 (Nhớ tốt)", cap4, Color(0xFF81C784)),
                Triple("Cấp 5+ (Đã thuộc)", cap5, Color(0xFF4CAF50))
            )

            danhSachCap.forEach { (nhan, soLuong, mauSacThanh) ->
                val tiLeSrs = if (tongSo == 0) 0f else soLuong.toFloat() / tongSo
                
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = nhan,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "$soLuong từ (${(tiLeSrs * 100).toInt()}%)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = mauSacThanh
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { tiLeSrs },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = mauSacThanh,
                        trackColor = MaterialTheme.colorScheme.surface
                    )
                }
            }
        }
    }
}

// Thành phần hiển thị bản ghi lịch sử ôn tập gần đây dạng dòng phẳng gọn gàng
@Composable
fun TheLichSuHoc(lichSu: LichSuOnTap) {
    // Định dạng ngày giờ hiển thị ngắn gọn từ mili-giây
    val dinhDangNgay = SimpleDateFormat("HH:mm - dd/MM", Locale.getDefault())
    val chuoiThoiGian = dinhDangNgay.format(Date(lichSu.ngayOnTap))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = chuoiThoiGian,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Text(
                text = "Đã ôn: ${lichSu.soTuDaHoc} từ",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Đúng: ${lichSu.soTuDung}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF2E7D32),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Sai: ${lichSu.soTuSai}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFC62828),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// Thành phần hiển thị tiến trình học của một danh mục chủ đề
@Composable
fun ThanhTienTrinhDanhMuc(thongKe: ThongKeMotDanhMuc) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = thongKe.danhMuc.ten,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "Thuộc: ${thongKe.soDaThuoc} / ${thongKe.tongSo} từ",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            
            // Thanh tiến trình LinearProgressIndicator thể hiện tỉ lệ từ đã thuộc
            LinearProgressIndicator(
                progress = { thongKe.tiLeDaThuoc },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = when {
                    thongKe.tiLeDaThuoc >= 0.8f -> Color(0xFF2E7D32) // Màu xanh lá khi thuộc tốt
                    thongKe.tiLeDaThuoc >= 0.5f -> Color(0xFFF57C00) // Màu cam khi thuộc trung bình
                    else -> Color(0xFFC62828) // Màu đỏ khi thuộc ít
                },
                trackColor = MaterialTheme.colorScheme.surface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tỉ lệ hoàn thành: ${(thongKe.tiLeDaThuoc * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Thành phần hộp hiển thị thông báo rỗng khi danh sách không có phần tử nào
@Composable
fun TheHopRong(thongDiep: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = thongDiep,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}