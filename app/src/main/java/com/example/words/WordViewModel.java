package com.example.words;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

/**
 * ViewModel用于管理界面数据
 * 不应该存在对数据的操作方法
 * AndroidViewModel可以有一个Context参数，方便获取数据，或则使用SharedPreference类
 */
public class WordViewModel extends AndroidViewModel {
    private WordRepository wordRepository;
    public WordViewModel(@NonNull Application application) {
        super(application);
        wordRepository = new WordRepository(application);
    }

    //作为一个中转站，去得到数据
    LiveData<List<Word>> getAllWordLive() {
        return wordRepository.getAllWordsLive();
    }

    LiveData<List<Word>> findWordWithPatten(String patten){
        return wordRepository.findWordWithPatten(patten);
    }

    //给外界提供接口
    //虽然将这些方法移到了Model中，但在这里还是要保留这些方法，因为MainActivity中还要调用这些方达
    //所以这里将作为一个中转站，
    void insertWords(Word...words){
//        new InsertAsyncTask(wordDao).execute(words);
        wordRepository.insertWords(words);
    }

    void updateWords(Word...words){
//        new UpdateAsyncTask(wordDao).execute(words);
        wordRepository.updateWords(words);
    }

    void deleteAllWords(){
//        new DeleteAllAsyncTask(wordDao).execute();
        wordRepository.deleteAllWords();
    }

    void deleteWords(Word...words){
//        new DeleteAsyncTask(wordDao).execute(words);
        wordRepository.deleteWords(words);
    }


}
