package jp.techacademy.tomokazu.kawano.qa_app;

import java.io.Serializable;

public class Favourite implements Serializable {
    private int mGenre;
    private String mQuestionKey;

    public int getGenre() {
        return mGenre;
    }

    public String getQuestionKey() {
        return mQuestionKey;
    }

    // コンストラクタ
    public Favourite(String questionKey, int genre) {
        mQuestionKey = questionKey;
        mGenre = genre;
    }
}
