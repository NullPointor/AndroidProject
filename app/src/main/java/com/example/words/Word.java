package com.example.words;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Word {
    @PrimaryKey(autoGenerate = true)
    private int id;

    //如果不设置ColumnInfo，系统默认你的变量名为ColumnInfo
    @ColumnInfo(name = "english_word")
    private String word;
    @ColumnInfo(name = "chinese_meaning")
    private String ChineseMeaning;
    @ColumnInfo(name = "chinese_invisible")
    private boolean chineseInvisible;

    //必须要写setter和getter，不然会报错
    boolean getChineseInvisible() {
        return chineseInvisible;
    }

    void setChineseInvisible(boolean chineseInvisible) {
        this.chineseInvisible = chineseInvisible;
    }

    /*@ColumnInfo(name = "foo_data")
    private boolean foo;
    @ColumnInfo(name = "bar_data")
    private boolean bar;

    public boolean isFoo() {
        return foo;
    }

    public void setFoo(boolean foo) {
        this.foo = foo;
    }

    public boolean isBar() {
        return bar;
    }

    public void setBar(boolean bar) {
        this.bar = bar;
    }*/

    public Word(){

    }

    public Word(String word, String chineseMeaning) {
        this.word = word;
        ChineseMeaning = chineseMeaning;
    }



    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    String getChineseMeaning() {
        return ChineseMeaning;
    }

    void setChineseMeaning(String chineseMeaning) {
        ChineseMeaning = chineseMeaning;
    }
}
