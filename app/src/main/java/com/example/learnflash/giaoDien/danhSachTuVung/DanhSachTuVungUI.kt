package com.example.learnflash.giaoDien.danhSachTuVung

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.learnflash.duLieu.local.thucThe.TuVung

// Thành phần giao diện màn hình danh sách từ vựng theo danh mục
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DanhSachTuVungUI(
    viewModel: DanhSachTuVungViewModel,
    chuyenHuongChiTiet: (Int, String) -> Unit,
    chuyenHuongOnTap: (String) -> Unit,
    quayLai: () -> Unit
) {
    // Thu thập State từ ViewModel
    val danhSachTu by viewModel.danhSachTuVung.collectAsState()
    val tuVungCanXoa by viewModel.tuVungCanXoa.collectAsState()
    val tuKhoaTimKiem by viewModel.tuKhoaTimKiem.collectAsState()

    // Hộp thoại xác nhận xóa từ vựng — đặt NGOÀI Scaffold để không bị lồng lambda
    if (tuVungCanXoa != null) {
        AlertDialog(
            onDismissRequest = { viewModel.huyXoaTuVung() },
            title = { Text("Xác nhận xóa") },
            text = { Text("Xóa từ \"${tuVungCanXoa?.tuKhoa}\"? Thao tác không thể hoàn tác.") },
            confirmButton = {
                Button(onClick = { viewModel.xacNhanXoaTuVung() }) {
                    Text("Xóa")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.huyXoaTuVung() }) {
                    Text("Hủy")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        // Tiêu đề chính là tên danh mục
                        Text(
                            text = viewModel.tenDanhMuc,
                            style = MaterialTheme.typography.titleLarge
                        )
                        // Phụ đề hiển thị số từ trong danh mục
                        Text(
                            text = "${danhSachTu.size} từ vựng",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    // Nút quay lại màn hình danh mục
                    IconButton(onClick = quayLai) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = {
                    // Nút kích hoạt phiên ôn tập cho danh mục hiện tại
                    IconButton(onClick = { chuyenHuongOnTap(viewModel.danhMucId) }) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Ôn tập danh mục này")
                    }
                }
            )
        },
        floatingActionButton = {
            // Nút thêm từ mới — truyền danhMucId để ChiTietUI tự điền danh mục
            FloatingActionButton(
                onClick = { chuyenHuongChiTiet(0, viewModel.danhMucId) }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Thêm từ")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Ô nhập tìm kiếm — lọc real-time không cần gọi DB
            OutlinedTextField(
                value = tuKhoaTimKiem,
                onValueChange = { viewModel.capNhatTimKiem(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                placeholder = { Text("Tìm từ khóa hoặc nghĩa tiếng Việt...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Tìm kiếm")
                },
                singleLine = true
            )

            if (danhSachTu.isEmpty()) {
                // Hiển thị gợi ý khi danh mục chưa có từ vựng nào
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Chưa có từ vựng nào",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Nhấn + để thêm từ vựng mới",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Danh sách cuộn LazyColumn hiển thị từ vựng trong danh mục
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(danhSachTu) { tuVung ->
                        ItemTuVungTrongDanhMuc(
                            tuVung = tuVung,
                            onSua = { chuyenHuongChiTiet(tuVung.id, viewModel.danhMucId) },
                            onXoa = { viewModel.yeuCauXoaTuVung(tuVung) }
                        )
                    }
                }
            }
        }
    }
}

// Thành phần Composable hiển thị một thẻ từ vựng trong LazyColumn
// *** Đặt NGOÀI hàm DanhSachTuVungUI — không được khai báo local bên trong Composable ***
@Composable
fun ItemTuVungTrongDanhMuc(tuVung: TuVung, onSua: () -> Unit, onXoa: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .clickable { onSua() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Từ khóa tiếng Anh — nổi bật
                Text(
                    text = tuVung.tuKhoa,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                // Nghĩa tiếng Việt
                Text(
                    text = tuVung.nghiaTiengViet,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                // Phiên âm nếu có
                if (tuVung.phienAm.isNotEmpty()) {
                    Text(
                        text = "/${tuVung.phienAm}/",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
            // Chỉ báo cấp độ SRS — hiển thị màu theo mức độ ghi nhớ
            Text(
                text = "Lv.${tuVung.capDoSrs}",
                style = MaterialTheme.typography.labelSmall,
                color = when {
                    tuVung.daThuoc -> MaterialTheme.colorScheme.primary
                    tuVung.capDoSrs >= 3 -> MaterialTheme.colorScheme.secondary
                    else -> MaterialTheme.colorScheme.outline
                },
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            // Nút xóa từ vựng
            IconButton(onClick = onXoa) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Xóa",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}