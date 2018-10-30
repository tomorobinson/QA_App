package jp.techacademy.tomokazu.kawano.qa_app;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class QuestionDetailActivity extends AppCompatActivity {

    private static final boolean FAVOURITED = true;
    private static final boolean UNFAVOURITED = false;
    private ListView mListView;
    private Question mQuestion;
    private QuestionDetailListAdapter mAdapter;
    private int mGenre;
    private Boolean mFavouriteFlag = UNFAVOURITED;

    private DatabaseReference mDatabaseRef;
    private DatabaseReference mAnswerRef;
    private DatabaseReference mFavouriteRef;
    private ImageButton mFavouriteButton;
    private String mQuestionKey;
    private String genre;
    private String mUserId;

    private ChildEventListener mEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            HashMap map = (HashMap) dataSnapshot.getValue();
            String answerUid = dataSnapshot.getKey();

            for (Answer answer : mQuestion.getAnswers()) {
                // 同じAnswerUidのものが存在しているときは何もしない
                if (answerUid.equals(answer.getAnswerUid())) {
                    return;
                }
            }

            String body = (String) map.get("body");
            String name = (String) map.get("name");
            String uid = (String) map.get("uid");

            Answer answer = new Answer(body, name, uid, answerUid);

            mQuestion.getAnswers().add(answer);
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_detail);

        // 渡ってきたQuestionのオブジェクトを保持する
        Bundle extras = getIntent().getExtras();
        mQuestion = (Question) extras.get("question");
        mUserId = (String) extras.get("userid");
        setTitle(mQuestion.getTitle());

        // ListViewの準備
        mListView = (ListView) findViewById(R.id.listView);
        mAdapter = new QuestionDetailListAdapter(this, mQuestion);
        mListView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

        // 質問追加用ボタン
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // ログイン済みのユーザーを取得する
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if (user == null) {
                    // ログインしていなければログイン画面に遷移させる
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                } else {
                    // Questionを渡して回答作成画面を起動する
                    Intent intent = new Intent(getApplicationContext(), AnswerSendActivity.class);
                    intent.putExtra("question", mQuestion);
                    startActivity(intent);
                }
            }
        });

        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        mAnswerRef = mDatabaseRef.child(Const.ContentsPATH).child(String.valueOf(mQuestion.getGenre())).child(mQuestion.getQuestionUid()).child(Const.AnswersPATH);
        mAnswerRef.addChildEventListener(mEventListener);

        // お気に入り追加用ボタン
        mFavouriteButton = (ImageButton) findViewById(R.id.favourite);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            // ログインしていなければ、ボタン非表示
            mFavouriteButton.setVisibility(View.INVISIBLE);
        } else {
            // ログインしていれば、ボタン表示してリスナー設定
            mFavouriteButton.setVisibility(View.VISIBLE);
            // 渡ってきたジャンルを保持
            mGenre = extras.getInt("genre");
            // 現在ログインしているユーザーを保持
            mUserId = user.getUid();
            // ボタンにリスナーを設定
            mFavouriteButton.setOnClickListener(favouriteClickListener);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        checkFavouriteExist();
    }

    private View.OnClickListener favouriteClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

//            checkFavouriteExist();

            if (mFavouriteFlag == FAVOURITED) {
                // お気に入り登録済みのため、お気に入りから削除、お気に入り解除ボタンへ変更
                mFavouriteRef.removeValue();
                mFavouriteFlag = UNFAVOURITED;
                mFavouriteButton.setImageResource(R.drawable.unfavourite);
            } else if (mFavouriteFlag == UNFAVOURITED) {
                // お気に入り未登録の場合、お気に入りへ登録し、お気に入り登録済みボタンへ変更
                Map<String, String> data = new HashMap<String, String>();
                data.put("genre", genre);

                DatabaseReference mFavouriteRef = mDatabaseRef.child(Const.FavouritePATH).child(mUserId).child(mQuestionKey);
                mFavouriteRef.setValue(data);

                mFavouriteFlag = FAVOURITED;
                mFavouriteButton.setImageResource(R.drawable.favourite);
            }
        }
    };

    private void checkFavouriteExist() {
        genre = String.valueOf(mGenre); // mGenreをIntからStringへ変換
        mQuestionKey = mQuestion.getQuestionUid();
        mFavouriteRef = mDatabaseRef.child(Const.FavouritePATH).child(mUserId).child(mQuestionKey);
        mFavouriteRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // レコードを取得できた場合、お気に入り登録済み
                if (dataSnapshot.getValue() != null) {
                    mFavouriteFlag = FAVOURITED;
                    mFavouriteButton.setImageResource(R.drawable.favourite);
                } else {
                    mFavouriteFlag = UNFAVOURITED;
                    mFavouriteButton.setImageResource(R.drawable.unfavourite);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
