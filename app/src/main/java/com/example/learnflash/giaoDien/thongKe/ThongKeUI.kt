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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// Thành phần giao diện cấu thành toàn bộ màn hình Thống kê
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThongKeUI(viewModel: ThongKeViewModel) {
    // Đọc StateFlow từ ViewModel và chuyển thành State để Compose theo dõi
    val thongKe by viewModel.duLieuThongKe.collectAsState()
    val thongKeTheoDanhMuc by viewModel.thongKeTheoDanhMuc.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Thống kê học tập") }) }
    ) { paddingValues ->
        // Dùng LazyColumn thay Column+verticalScroll để hiệu quả hơn khi có 60 danh mục
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Nhóm thống kê tổng quan số lượng từ vựng
            item {
                Text(
                    text = "Tổng quan từ vựng",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            item { TheThongKe(tieuDe = "Tổng số từ vựng", giaTri = "${thongKe.tongSoTu} từ") }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TheThongKe("Đã thuộc", "${thongKe.soTuDaThuoc} từ", Modifier.weight(1f))
                    TheThongKe("Chưa thuộc", "${thongKe.soTuChuaThuoc} từ", Modifier.weight(1f))
                }
            }

            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Nhóm thống kê chỉ số SRS
            item {
                Text(
                    text = "Chỉ số SRS",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            item {
                TheThongKe(
                    tieuDe = "Trung bình cấp độ",
                    giaTri = String.format("%.2f", thongKe.trungBinhCapDoSrs)
                )
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TheThongKe("Cao nhất (Max)", "Cấp ${thongKe.caoNhatCapDoSrs}", Modifier.weight(1f))
                    TheThongKe("Thấp nhất (Min)", "Cấp ${thongKe.thapNhatCapDoSrs}", Modifier.weight(1f))
                }
            }

            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Nhóm thống kê hoạt động hôm nay
            item {
                Text(
                    text = "Hoạt động hôm nay",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TheThongKe("Cần ôn hôm nay", "${thongKe.soTuOnTapHomNay} từ", Modifier.weight(1f))
                    TheThongKe("Đã ôn hôm nay", "${thongKe.soLuotDaOnHomNay} lượt", Modifier.weight(1f))
                }
            }

            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Tiêu đề nhóm tiến độ theo danh mục
            item {
                Text(
                    text = "Tiến độ theo danh mục",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // Danh sách tiến độ từng danh mục dạng inline items trong LazyColumn
            if (thongKeTheoDanhMuc.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Chưa có dữ liệu",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                // Mỗi danh mục là một item trong LazyColumn — hiệu quả hơn vì có thể 60 mục
                items(thongKeTheoDanhMuc) { thongKeDM ->
                    ThanhTienTrinhDanhMuc(thongKeDM)
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

// Thành phần hiển thị tiến trình học của một danh mục với LinearProgressIndicator
@Composable
fun ThanhTienTrinhDanhMuc(thongKe: ThongKeMotDanhMuc) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tên danh mục — tối đa 1 dòng
                Text(
                    text = thongKe.danhMuc.ten,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                // Số từ đã thuộc / tổng số — góc phải
                Text(
                    text = "${thongKe.soDaThuoc}/${thongKe.tongSo}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            // Thanh tiến trình LinearProgressIndicator hiển thị tỉ lệ đã thuộc
            LinearProgressIndicator(
                progress = { thongKe.tiLeDaThuoc },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = when {
                    thongKe.tiLeDaThuoc >= 0.8f -> MaterialTheme.colorScheme.primary
                    thongKe.tiLeDaThuoc >= 0.5f -> MaterialTheme.colorScheme.secondary
                    else -> MaterialTheme.colorScheme.error
                }
            )
            Spacer(modifier = Modifier.height(4.dp))
            // Tỉ lệ phần trăm hiển thị dưới thanh
            Text(
                text = "${(thongKe.tiLeDaThuoc * 100).toInt()}% đã thuộc",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Thành phần thẻ thống kê tái sử dụng
@Composable
fun TheThongKe(
    tieuDe: String,
    giaTri: String,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = tieuDe,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = giaTri,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}