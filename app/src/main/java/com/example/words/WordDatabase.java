package com.example.words;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

//Singleton
//没次实体类entity属性发生改变后都要改变数据库版本
@Database(entities = {Word.class},version = 5,exportSchema = false)
public abstract class WordDatabase extends RoomDatabase {
    //单例设计模式，全局使用一个数据库，同时避免多次去new 一个数据库出来
    private static WordDatabase INSTANCE;
    //当有多个客户端同时申请数据库的时候，不会发生“碰撞”
    static synchronized WordDatabase getDatabase(Context context){
        if (INSTANCE == null){
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),//得到一个应用程序根节点的Context(全局唯一)
                    WordDatabase.class,"word_database")
//            .allowMainThreadQueries()//这一句是运行对数据的操作可以在主线程中执行，当加入了人AsyncTask后，就不用使用这个方法了
//                    .fallbackToDestructiveMigration()//破坏性的迁移，会删掉用户全部数据，新建一个表
                    .addMigrations(MIGRATION_4_5)//版本迁移，需要添加一个参数
                    .build();
        }
        return INSTANCE;
    }
    public abstract WordDao getWordDao();

    static final Migration MIGRATION_2_3 = new Migration(2,3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {//偏底层，需要手写SQL语句
            database.execSQL("ALTER TABLE word ADD COLUMN bar_data INTEGER NOT NULL DEFAULT 1");
        }
    };

    /**现在要删除这两个字段，需要经过四个步骤
     * 1. 创建一个新表，建好你想要的一些字段
     * 2. 把你想要的字段复制过去
     * 3. 删除旧的表
     * 4. 再把新的数据库改一个名字
     */
    static final Migration MIGRATION_3_4 = new Migration(3,4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {//偏底层，需要手写SQL语句
            database.execSQL("CREATE TABLE word_temp (id INTEGER PRIMARY KEY NOT NULL,english_word TEXT," +
                    "chinese_meaning TEXT)");
            database.execSQL("INSERT INTO word_temp (id,english_word,chinese_meaning)" +
                    "SELECT id,english_word,chinese_meaning from word");
            database.execSQL("DROP TABLE word");
            database.execSQL("ALTER TABLE word_temp RENAME TO word");
        }
    };

    private static final Migration MIGRATION_4_5 = new Migration(4,5) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {//偏底层，需要手写SQL语句
            database.execSQL("ALTER TABLE word ADD COLUMN chineseInvisible INTEGER NOT NULL DEFAULT 0");
        }
    };
}
