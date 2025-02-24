package com.example.ybb.config

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.ybb.dao.UserDao
import com.example.ybb.entity.User

@Database(entities = [User::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // 获取数据库实例并应用迁移
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ybb_database"
                )
                    .addMigrations(*getMigrations(context)) // 添加迁移
                    .build()
                INSTANCE = instance
                instance
            }
        }

        // 加载迁移脚本
        private fun getMigrations(context: Context): Array<Migration> {
            val migrations = mutableListOf<Migration>()
            val migrationLoader = MigrationLoader(context)

            // 加载迁移脚本
            val migrationScripts = migrationLoader.loadMigrations()

            // 遍历迁移脚本并添加到迁移列表中
            migrationScripts.forEach { (versionKey, script) ->
                val versions = versionKey.split("_")
                if (versions.size == 2) {
                    val fromVersion = versions[0].toInt()
                    val toVersion = versions[1].toInt()

                    migrations.add(object : Migration(fromVersion, toVersion) {
                        override fun migrate(db: SupportSQLiteDatabase) {
                            db.execSQL(script)
                        }
                    })
                }
            }

            return migrations.toTypedArray()
        }
    }
}
