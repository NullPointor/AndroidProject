package com.example.words;

import android.content.Context;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;

/**
 * 用来做数据的处理
 * 可以在数据库或者在云端的数据库取数据
 */
class WordRepository {
    private LiveData<List<Word>>allWordsLive;
    private WordDao wordDao;

    WordRepository(Context context) {
        WordDatabase wordDatabase = WordDatabase.getDatabase(context.getApplicationContext());//单例设计模式
        wordDao = wordDatabase.getWordDao();
        allWordsLive = wordDao.getAllWordsLive();
    }

    LiveData<List<Word>> getAllWordsLive() {
        return allWordsLive;
    }
    //模糊匹配，需要加上通配符号
    LiveData<List<Word>> findWordWithPatten(String patten){
        return wordDao.findWordWithPatten("%" + patten + "%");
    }

    //给外界提供接口
    void insertWords(Word...words){
        new InsertAsyncTask(wordDao).execute(words);
    }

    void updateWords(Word...words){
        new UpdateAsyncTask(wordDao).execute(words);
    }

    void deleteAllWords(){
        new DeleteAllAsyncTask(wordDao).execute();
    }

    void deleteWords(Word...words){
        new DeleteAsyncTask(wordDao).execute(words);
    }



    /**
     * 使用这些AsyncTask是为了不在主线程中对数据进行操作，避免加载时间过程，影响主线程的执行
     * 但其实还是调用之前在主线程中操作的那几个方法
     *
     *泛型里面三个内容
     * 第一个：实体类
     * 第二个：操作的进度
     * 第三个：操作的结果
     *
     * 因为这是对数据的处理方法，所以这几个方法从MainActivity中换到了WordViewModel中，然后让到了现在的WordRepository中
     */
    static class InsertAsyncTask extends AsyncTask<Word,Void,Void> {
        private WordDao wordDao;

        InsertAsyncTask(WordDao wordDao) {
            this.wordDao = wordDao;
        }

        //在后台上要做什么操作
        @Override
        protected Void doInBackground(Word... words) {
            wordDao.insertWords(words);
            return null;
        }

        //任务完成后会呼叫
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }

        //当进度更新的时候呼叫
        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        //在后台任务执行之前呼叫
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
    }

    static class UpdateAsyncTask extends AsyncTask<Word,Void,Void>{
        private WordDao wordDao;

        UpdateAsyncTask(WordDao wordDao) {
            this.wordDao = wordDao;
        }

        //在后台上要做什么操作
        @Override
        protected Void doInBackground(Word... words) {
            wordDao.updateWords(words);
            return null;
        }
    }

    static class DeleteAsyncTask extends AsyncTask<Word,Void,Void>{
        private WordDao wordDao;

        DeleteAsyncTask(WordDao wordDao) {
            this.wordDao = wordDao;
        }

        //在后台上要做什么操作
        @Override
        protected Void doInBackground(Word... words) {
            wordDao.deleteWords(words);
            return null;
        }
    }

    static class DeleteAllAsyncTask extends AsyncTask<Void,Void,Void>{
        private WordDao wordDao;

        DeleteAllAsyncTask(WordDao wordDao) {
            this.wordDao = wordDao;
        }

        //在后台上要做什么操作
        @Override
        protected Void doInBackground(Void... voids) {
            wordDao.deleteAllWords();
            return null;
        }
    }
}
