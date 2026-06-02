package com.example.learnflash.giaoDien.gioiThieu

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.learnflash.giaoDien.thanhPhanChung.ManHinhChoLoading

// Thành phần giao diện cấu thành màn hình Giới thiệu ứng dụng
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GioiThieuUI(viewModel: GioiThieuViewModel) {
    // Trích xuất ngữ cảnh hệ thống (Context) để kích hoạt Intent mở PDF
    val context = LocalContext.current

    // Trạng thái lưu trữ URI được chọn tạm thời để chờ xác nhận chế độ nhập
    var uriNapDuLieuTam by remember { mutableStateOf<Uri?>(null) }

    // Trạng thái lưu trữ định dạng tệp tin được chọn (true là JSON, false là CSV)
    var laDinhDangJson by remember { mutableStateOf(true) }

    // Trạng thái kiểm soát việc hiển thị hộp thoại lựa chọn chế độ nhập dữ liệu
    var hienThiHopThoaiCheDo by remember { mutableStateOf(false) }

    // Trạng thái lưu trữ chế độ nạp dữ liệu được người dùng lựa chọn trên giao diện
    var cheDoDuocChon by remember { mutableStateOf(CheDoNapDuLieu.THEM_MOI) }

    // Bộ kích hoạt hệ thống chọn vị trí lưu và đặt tên cho tệp JSON xuất ra
    val boKichHoatXuatJson = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { diaChiUri ->
        diaChiUri?.let { viewModel.xuatDuLieuJson(context, it) }
    }

    // Bộ kích hoạt hệ thống chọn tệp JSON cần nhập vào ứng dụng
    val boKichHoatNhapJson = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { diaChiUri ->
        diaChiUri?.let {
            uriNapDuLieuTam = it
            laDinhDangJson = true
            hienThiHopThoaiCheDo = true
        }
    }

    // Bộ kích hoạt hệ thống chọn vị trí lưu và đặt tên cho tệp CSV xuất ra
    val boKichHoatXuatCsv = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { diaChiUri ->
        diaChiUri?.let { viewModel.xuatDuLieuCsv(context, it) }
    }

    // Bộ kích hoạt hệ thống chọn tệp CSV cần nhập vào ứng dụng
    val boKichHoatNhapCsv = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { diaChiUri ->
        diaChiUri?.let {
            uriNapDuLieuTam = it
            laDinhDangJson = false
            hienThiHopThoaiCheDo = true
        }
    }

    // Đọc các trạng thái StateFlow từ ViewModel
    val dangXuLy by viewModel.dangXuLy.collectAsState()
    val thongBaoKetQua by viewModel.thongBaoKetQua.collectAsState()

    // Khởi tạo SnackbarHostState để hiển thị thông báo kết quả xuất/nhập
    val snackbarHostState = remember { SnackbarHostState() }

    // Theo dõi StateFlow thông báo và kích hoạt hiển thị SnackBar khi có nội dung mới
    LaunchedEffect(thongBaoKetQua) {
        if (thongBaoKetQua.isNotEmpty()) {
            snackbarHostState.showSnackbar(thongBaoKetQua)
            viewModel.daHienThiThongBao()
        }
    }

    // Khung giao diện Scaffold tích hợp TopAppBar và SnackbarHost
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Giới thiệu") })
        },
        snackbarHost = {
            // Vùng hiển thị thông báo SnackBar phía dưới màn hình
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Tiêu đề và thông tin phiên bản ứng dụng
            Text(
                text = "LearnFlash",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Phiên bản 1.0.0",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Ứng dụng học từ vựng bằng thuật toán lặp ngắt quãng (Spaced Repetition System).",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // Nhóm nút mở tài liệu hướng dẫn PDF
            Text(
                text = "Tài liệu hướng dẫn",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Nút bấm kích hoạt Intent hệ thống mở file PDF từ URL trực tuyến
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        // URL trỏ đến file PDF hướng dẫn sử dụng thực tế — cập nhật khi deploy
                        data = Uri.parse("https://drive.google.com/file/d/huong_dan_learnflash/view")
                    }
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !dangXuLy
            ) {
                Text("Xem Hướng Dẫn Sử Dụng (PDF)")
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // Nhóm nút xuất/nhập dữ liệu định dạng JSON
            Text(
                text = "Quản lý dữ liệu — JSON",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Hàng nút Xuất và Nhập JSON đặt song song
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Nút kích hoạt tác vụ xuất dữ liệu ra file JSON
                Button(
                    onClick = { boKichHoatXuatJson.launch("learnflash_tuvung.json") },
                    modifier = Modifier.weight(1f),
                    enabled = !dangXuLy
                ) {
                    Text("Xuất JSON")
                }
                // Nút kích hoạt tác vụ nhập dữ liệu từ file JSON
                OutlinedButton(
                    onClick = { boKichHoatNhapJson.launch("application/json") },
                    modifier = Modifier.weight(1f),
                    enabled = !dangXuLy
                ) {
                    Text("Nhập JSON")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Nhóm nút xuất/nhập dữ liệu định dạng CSV
            Text(
                text = "Quản lý dữ liệu — CSV",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Hàng nút Xuất và Nhập CSV đặt song song
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Nút kích hoạt tác vụ xuất dữ liệu ra file CSV
                Button(
                    onClick = { boKichHoatXuatCsv.launch("learnflash_tuvung.csv") },
                    modifier = Modifier.weight(1f),
                    enabled = !dangXuLy
                ) {
                    Text("Xuất CSV")
                }
                // Nút kích hoạt tác vụ nhập dữ liệu từ file CSV
                OutlinedButton(
                    onClick = { boKichHoatNhapCsv.launch("*/*") },
                    modifier = Modifier.weight(1f),
                    enabled = !dangXuLy
                ) {
                    Text("Nhập CSV")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Hiển thị hộp thoại lựa chọn chế độ nạp dữ liệu khi có URI hợp lệ và cờ được bật
    if (hienThiHopThoaiCheDo && uriNapDuLieuTam != null) {
        AlertDialog(
            onDismissRequest = {
                // Đặt lại các trạng thái về ban đầu khi đóng hộp thoại
                hienThiHopThoaiCheDo = false
                uriNapDuLieuTam = null
            },
            title = {
                Text(
                    text = "Lựa chọn chế độ nhập dữ liệu",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Vui lòng chọn cách thức xử lý dữ liệu khi nhập vào ứng dụng:",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    // Lựa chọn 1: Chỉ thêm từ mới chưa tồn tại
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { cheDoDuocChon = CheDoNapDuLieu.THEM_MOI }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = cheDoDuocChon == CheDoNapDuLieu.THEM_MOI,
                            onClick = { cheDoDuocChon = CheDoNapDuLieu.THEM_MOI }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Chỉ thêm từ mới",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Bỏ qua các từ đã tồn tại (trùng từ khóa). Giữ nguyên dữ liệu hiện có.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Lựa chọn 2: Cập nhật thông tin từ vựng đã trùng
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { cheDoDuocChon = CheDoNapDuLieu.CAP_NHAT_TRUNG }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = cheDoDuocChon == CheDoNapDuLieu.CAP_NHAT_TRUNG,
                            onClick = { cheDoDuocChon = CheDoNapDuLieu.CAP_NHAT_TRUNG }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Cập nhật & ghi đè từ trùng",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Cập nhật nghĩa, loại từ của từ cũ nếu trùng. Thêm mới từ chưa có.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Lựa chọn 3: Xóa toàn bộ dữ liệu và nạp mới hoàn toàn
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { cheDoDuocChon = CheDoNapDuLieu.THAY_THE_TAT_CA }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = cheDoDuocChon == CheDoNapDuLieu.THAY_THE_TAT_CA,
                            onClick = { cheDoDuocChon = CheDoNapDuLieu.THAY_THE_TAT_CA }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Thay thế toàn bộ",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = "Xóa sạch toàn bộ từ vựng hiện có trong máy và nạp lại từ file.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Tắt hộp thoại hiển thị
                        hienThiHopThoaiCheDo = false
                        // Kiểm tra URI tạm thời và gọi hàm nạp dữ liệu phù hợp
                        uriNapDuLieuTam?.let { uri ->
                            if (laDinhDangJson) {
                                viewModel.nhapDuLieuJson(context, uri, cheDoDuocChon)
                            } else {
                                viewModel.nhapDuLieuCsv(context, uri, cheDoDuocChon)
                            }
                        }
                        // Xóa dữ liệu URI tạm thời sau khi xử lý xong
                        uriNapDuLieuTam = null
                    }
                ) {
                    Text("Xác nhận")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        // Đặt lại các biến trạng thái khi hủy tác vụ
                        hienThiHopThoaiCheDo = false
                        uriNapDuLieuTam = null
                    }
                ) {
                    Text("Hủy")
                }
            }
        )
    }

    // Hiển thị lớp phủ vòng xoay Loading khi đang thực thi tác vụ IO
    if (dangXuLy) {
        ManHinhChoLoading()
    }
}