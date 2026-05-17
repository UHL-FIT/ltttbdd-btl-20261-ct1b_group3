package com.example.learnflash.giaoDien.manHinhChinh

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.learnflash.duLieu.local.thucThe.TuVung
import com.example.learnflash.giaoDien.thanhPhanChung.HopThoaiCanhBaoLoi

// Thành phần giao diện tổng quan của màn hình chính
@Composable
fun ManHinhChinhUI(
    viewModel: ManHinhChinhViewModel,
    chuyenHuongChiTiet: (Int) -> Unit
) {
    // Thu thập State an toàn từ luồng dữ liệu của ViewModel
    val danhSachTu by viewModel.danhSachTuVung.collectAsState()
    val thongKe by viewModel.thongKeTuVung.collectAsState()
    val tuVungXoa by viewModel.tuVungCanXoa.collectAsState()

    // Khung giao diện Scaffold chứa nút thêm nổi (FAB) và danh sách
    Scaffold(
        floatingActionButton = {
            // Điều hướng sang màn hình Chi Tiết với ID = 0 (Đồng nghĩa với Thêm mới)
            FloatingActionButton(onClick = { chuyenHuongChiTiet(0) }) {
                Icon(Icons.Default.Add, contentDescription = "Thêm từ vựng")
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {

            // Khu vực hiển thị thông tin thống kê số lượng
            Text(
                text = "Tổng số từ: ${thongKe.first} | Đã thuộc: ${thongKe.second}",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp)
            )

            // Danh sách cuộn LazyColumn hiển thị các thẻ từ vựng tái sử dụng
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(danhSachTu) { tuVung ->
                    ItemTuVung(
                        tuVung = tuVung,
                        onSua = { chuyenHuongChiTiet(tuVung.id) },
                        onXoa = { viewModel.yeuCauXoaTuVung(tuVung) }
                    )
                }
            }
        }

        // Bắt sự kiện hiển thị hộp thoại cảnh báo xóa
        if (tuVungXoa != null) {
            HopThoaiCanhBaoLoi(
                tieuDe = "Xác nhận xóa",
                noiDung = "Bạn có chắc chắn muốn xóa từ '${tuVungXoa?.tuKhoa}'?",
                onXacNhan = { viewModel.xacNhanXoaTuVung() }
            )
        }
    }
}

// Thành phần giao diện con (Composable) hiển thị một dòng thông tin thẻ từ vựng
@Composable
fun ItemTuVung(tuVung: TuVung, onSua: () -> Unit, onXoa: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onSua() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = tuVung.tuKhoa, style = MaterialTheme.typography.titleLarge)
                Text(text = tuVung.nghiaTiengViet, style = MaterialTheme.typography.bodyLarge)
            }
            // Nút bấm kích hoạt sự kiện yêu cầu xóa từ vựng lên ViewModel
            IconButton(onClick = onXoa) {
                Icon(Icons.Default.Delete, contentDescription = "Xóa")
            }
        }
    }
}
