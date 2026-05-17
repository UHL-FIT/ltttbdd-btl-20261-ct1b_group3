package com.example.learnflash.duLieu.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.learnflash.duLieu.local.dao.LichSuOnTapDao
import com.example.learnflash.duLieu.local.dao.TuVungDao
import com.example.learnflash.duLieu.local.thucThe.LichSuOnTap
import com.example.learnflash.duLieu.local.thucThe.TuVung

// Khai báo cơ sở dữ liệu Room, bao gồm mảng các thực thể (Entities) và phiên bản version
@Database(entities = [TuVung::class, LichSuOnTap::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    // Cung cấp phương thức để lấy đối tượng truy vấn TuVungDao
    abstract fun tuVungDao(): TuVungDao

    // Cung cấp phương thức để lấy đối tượng truy vấn LichSuOnTapDao
    abstract fun lichSuOnTapDao(): LichSuOnTapDao

    companion object {
        // Đảm bảo chỉ có một thể hiện (Instance) duy nhất của Database được tạo ra (Singleton Pattern)
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Hàm khởi tạo và cung cấp Database
        fun layDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                // Xây dựng Database thông qua Room.databaseBuilder
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "learnflash_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
