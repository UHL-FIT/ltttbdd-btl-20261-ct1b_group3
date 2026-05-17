package com.example.learnflash.dieuHuong

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.learnflash.duLieu.khoDuLieu.KhoDuLieuTuVung
import com.example.learnflash.giaoDien.chiTietTuVung.ChiTietUI
import com.example.learnflash.giaoDien.chiTietTuVung.ChiTietViewModel
import com.example.learnflash.giaoDien.gioiThieu.GioiThieuUI
import com.example.learnflash.giaoDien.gioiThieu.GioiThieuViewModel
import com.example.learnflash.giaoDien.manHinhChinh.ManHinhChinhUI
import com.example.learnflash.giaoDien.manHinhChinh.ManHinhChinhViewModel
import com.example.learnflash.giaoDien.onTapTheFlash.OnTapUI
import com.example.learnflash.giaoDien.onTapTheFlash.OnTapViewModel

// Hàm Composable định nghĩa cấu trúc khung chuyển hướng màn hình
@Composable
fun DieuHuongApp(khoDuLieu: KhoDuLieuTuVung) {
    // Khởi tạo bộ điều khiển trạng thái tuyến đường (NavController)
    val navController = rememberNavController()

    // Thu thập trạng thái BackStack để xác định Item nào trên thanh BottomBar đang được chọn
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val tuyenHienTai = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            // Thành phần giao diện thanh điều hướng ngang bên dưới (Bottom Navigation Bar)
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Trang chủ") },
                    label = { Text("Trang chủ") },
                    selected = tuyenHienTai == "manHinhChinh",
                    onClick = {
                        navController.navigate("manHinhChinh") { popUpTo(0) }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.PlayArrow, contentDescription = "Ôn tập") },
                    label = { Text("Ôn tập") },
                    selected = tuyenHienTai == "onTap",
                    onClick = {
                        navController.navigate("onTap") { launchSingleTop = true }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Info, contentDescription = "Giới thiệu") },
                    label = { Text("Giới thiệu") },
                    selected = tuyenHienTai == "gioiThieu",
                    onClick = {
                        navController.navigate("gioiThieu") { launchSingleTop = true }
                    }
                )
            }
        }
    ) { paddingValues ->
        // Thành phần quản lý hệ thống chuyển trang (NavHost)
        NavHost(
            navController = navController,
            startDestination = "manHinhChinh",
            modifier = Modifier.padding(paddingValues)
        ) {
            // Khai báo tuyến đường Màn Hình Chính
            composable("manHinhChinh") {
                // Khởi tạo ViewModel an toàn bằng Factory để giữ State khi Recomposition
                val viewModel: ManHinhChinhViewModel = viewModel { ManHinhChinhViewModel(khoDuLieu) }
                ManHinhChinhUI(
                    viewModel = viewModel,
                    chuyenHuongChiTiet = { id -> navController.navigate("chiTiet/$id") }
                )
            }

            // Khai báo tuyến đường Màn Hình Chi Tiết truyền tham số động (ID)
            composable(
                route = "chiTiet/{id}",
                arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) { backStackEntry ->
                val idTuVung = backStackEntry.arguments?.getInt("id") ?: 0
                val viewModel: ChiTietViewModel = viewModel { ChiTietViewModel(khoDuLieu, idTuVung) }
                ChiTietUI(
                    viewModel = viewModel,
                    quayLai = { navController.popBackStack() }
                )
            }

            // Khai báo tuyến đường Màn Hình Ôn Tập Thẻ
            composable("onTap") {
                val viewModel: OnTapViewModel = viewModel { OnTapViewModel(khoDuLieu) }
                OnTapUI(
                    viewModel = viewModel,
                    quayLai = { navController.popBackStack() }
                )
            }

            // Khai báo tuyến đường Màn Hình Giới Thiệu
            composable("gioiThieu") {
                val viewModel: GioiThieuViewModel = viewModel { GioiThieuViewModel(khoDuLieu) }
                GioiThieuUI(viewModel = viewModel)
            }
        }
    }
}
