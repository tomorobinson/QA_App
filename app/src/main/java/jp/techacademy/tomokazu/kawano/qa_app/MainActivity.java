package jp.techacademy.tomokazu.kawano.qa_app;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private Toolbar mToolbar;
    private int mGenre = 0;

    private DatabaseReference mDatabaseReference;
    private DatabaseReference mGenreRef, mContentsRef, mFavouriteRef;
    private ListView mListView;
    private ArrayList<Question> mQuestionArrayList, mContentsArrayList, mFilteredQuestionArrayList;
    private ArrayList<String> mFavouriteArrayList;
    private QuestionsListAdapter mAdapter;
    private Question question, questionAll;
    private MenuItem mFavouriteMenuItem;
    private FirebaseUser user;
    private String mUserId;
    private boolean favouriteSelected = false;
    private FloatingActionButton fab;

    private ChildEventListener mEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            HashMap map = (HashMap) dataSnapshot.getValue();
            String title = (String) map.get("title");
            String body = (String) map.get("body");
            String name = (String) map.get("name");
            String uid = (String) map.get("uid");
            String imageString = (String) map.get("image");
            byte[] bytes;
            if (imageString != null) {
                bytes = Base64.decode(imageString, Base64.DEFAULT);
            } else {
                bytes = new byte[0];
            }

            ArrayList<Answer> answerArrayList = new ArrayList<Answer>();
            HashMap answerMap = (HashMap) map.get("answers");
            if (answerMap != null) {
                for (Object key : answerMap.keySet()) {
                    HashMap temp = (HashMap) answerMap.get((String) key);
                    String answerBody = (String) temp.get("body");
                    String answerName = (String) temp.get("name");
                    String answerUid = (String) temp.get("uid");
                    Answer answer = new Answer(answerBody, answerName, answerUid, (String) key);
                    answerArrayList.add(answer);
                }
            }

            question = new Question(title, body, name, uid, dataSnapshot.getKey(), mGenre, bytes, answerArrayList);
            mQuestionArrayList.add(question);
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            HashMap map = (HashMap) dataSnapshot.getValue();

            // 変更があったQuestionを探す
            for (Question question : mQuestionArrayList) {
                if (dataSnapshot.getKey().equals(question.getQuestionUid())) {
                    // このアプリで変更がある可能性があるのは回答(Answer)のみ
                    question.getAnswers().clear();
                    HashMap answerMap = (HashMap) map.get("answers");
                    if (answerMap != null) {
                        for (Object key : answerMap.keySet()) {
                            HashMap temp = (HashMap) answerMap.get((String) key);
                            String answerBody = (String) temp.get("body");
                            String answerName = (String) temp.get("name");
                            String answerUid = (String) temp.get("uid");
                            Answer answer = new Answer(answerBody, answerName, answerUid, (String) key);
                            question.getAnswers().add(answer);
                        }
                    }

                    mAdapter.notifyDataSetChanged();
                }
            }
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

    private ChildEventListener mContentsEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                String genreid = dataSnapshot.getKey();
                int genre = Integer.parseInt(genreid);
                String title = (String) ds.child("title").getValue();
                String body = (String) ds.child("body").getValue();
                String name = (String) ds.child("name").getValue();
                String uid = (String) ds.child("uid").getValue();
                String imageString = (String) ds.child("image").getValue();
                byte[] bytes;
                if (imageString != null) {
                    bytes = Base64.decode(imageString, Base64.DEFAULT);
                } else {
                    bytes = new byte[0];
                }

                ArrayList<Answer> answerArrayList = new ArrayList<Answer>();
                HashMap answerMap = (HashMap) ds.child("answers").getValue();
                if (answerMap != null) {
                    for (Object key : answerMap.keySet()) {
                        HashMap temp = (HashMap) answerMap.get((String) key);
                        String answerBody = (String) temp.get("body");
                        String answerName = (String) temp.get("name");
                        String answerUid = (String) temp.get("uid");
                        Answer answer = new Answer(answerBody, answerName, answerUid, (String) key);
                        answerArrayList.add(answer);
                    }
                }
                questionAll = new Question(title, body, name, uid, ds.getKey(), genre, bytes, answerArrayList);
                mContentsArrayList.add(questionAll);
            }

            // 質問のフィルタリング
            mFilteredQuestionArrayList.clear();
            filterQuestions();
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

    private ChildEventListener mFavouriteEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            HashMap map = (HashMap) dataSnapshot.getValue();

            // 全てのお気に入りを格納するリストを作成
            String questionId = dataSnapshot.getKey();
            mFavouriteArrayList.add(questionId);

            // 質問のフィルタリング
            mFilteredQuestionArrayList.clear();
            filterQuestions();
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

    private void filterQuestions() {
        for (String questionId : mFavouriteArrayList) {
            for (Question q : mContentsArrayList) {
                if (questionId.equals(q.getQuestionUid())) {
                    mFilteredQuestionArrayList.add(q);
                }
            }
        }
        mAdapter.setQuestionArrayList(mFilteredQuestionArrayList);
        mAdapter.notifyDataSetChanged();
        mListView.setAdapter(mAdapter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // メンバ変数
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // ジャンルを選択していない場合（mGenre == 0）はエラーを表示するだけ
                if (mGenre == 0) {
                    Snackbar.make(view, "ジャンルを選択して下さい", Snackbar.LENGTH_LONG).show();
                    return;
                }

                if (user == null) {
                    // ログインしていなければログイン画面に遷移させる
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);

                } else {
                    // ジャンルを渡して質問作成画面を起動する
                    Intent intent = new Intent(getApplicationContext(), QuestionSendActivity.class);
                    intent.putExtra("genre", mGenre);
                    startActivity(intent);
                }
            }
        });

        // ナビゲーションドロワーの設定
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, mToolbar, R.string.app_name, R.string.app_name);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // お気に入りメニューの設定、当該メニューの表示・非表示の判定
        Menu menu = navigationView.getMenu();
        mFavouriteMenuItem = menu.findItem(R.id.nav_favourite);
        checkLogonStatus();

        // Firebase
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        // ListViewの準備
        mListView = (ListView) findViewById(R.id.listView);
        mAdapter = new QuestionsListAdapter(this);
        mQuestionArrayList = new ArrayList<Question>();
        mAdapter.notifyDataSetChanged();

        // お気に入りリストの準備
        mFavouriteArrayList = new ArrayList<String>();

        // コンテンツリストの準備
        mContentsArrayList = new ArrayList<Question>();

        // お気に入り登録済み質問リストの準備
        mFilteredQuestionArrayList = new ArrayList<Question>();

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Questionのインスタンスを渡して質問詳細画面を起動する
                Intent intent = new Intent(getApplicationContext(), QuestionDetailActivity.class);

                if (mFilteredQuestionArrayList.size() != 0){
                    intent.putExtra("question", mFilteredQuestionArrayList.get(position));
                    intent.putExtra("genre", mFilteredQuestionArrayList.get(position).getGenre());
                } else {
                    intent.putExtra("question", mQuestionArrayList.get(position));
                    intent.putExtra("genre", mGenre);
                }
                intent.putExtra("userid", mUserId);
                startActivity(intent);
            }
        });
    }

    private void checkLogonStatus() {
        // ログイン済みのユーザーを取得する
        user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            mUserId = user.getUid();
        }

        if (mFavouriteMenuItem != null) {
            if (user == null) {
                // ログインしていない場合、メニュー非表示
                mFavouriteMenuItem.setVisible(false);
            } else {
                // ログインしている場合、メニュー表示
                mFavouriteMenuItem.setVisible(true);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // 1:趣味を既定の選択とする
        if (mGenre == 0) {
            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            onNavigationItemSelected(navigationView.getMenu().getItem(0));
        }

        checkLogonStatus();

        if (favouriteSelected == true){
            mFilteredQuestionArrayList.clear();
            filterQuestions();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_hobby) {
            mToolbar.setTitle("趣味");
            mGenre = 1;
            favouriteSelected = false;
            fab.show();
        } else if (id == R.id.nav_life) {
            mToolbar.setTitle("生活");
            mGenre = 2;
            favouriteSelected = false;
            fab.show();
        } else if (id == R.id.nav_health) {
            mToolbar.setTitle("健康");
            mGenre = 3;
            favouriteSelected = false;
            fab.show();
        } else if (id == R.id.nav_compter) {
            mToolbar.setTitle("コンピューター");
            mGenre = 4;
            favouriteSelected = false;
            fab.show();
        } else if (id == R.id.nav_favourite) {
            mToolbar.setTitle("お気に入り");
            favouriteSelected = true;
            fab.hide();

            // お気に入りリストをクリア
            mFavouriteArrayList.clear();

            // お気に入りリストにリスナーを登録する
            if (mFavouriteRef != null) {
                mFavouriteRef.removeEventListener(mFavouriteEventListener);
            }
            mFavouriteRef = mDatabaseReference.child(Const.FavouritePATH).child(mUserId);
            mFavouriteRef.addChildEventListener(mFavouriteEventListener);

            // 質問リスト及びお気に入り登録済み質問リストをクリア
            mContentsArrayList.clear();

            if (mContentsRef != null) {
                mContentsRef.removeEventListener(mContentsEventListener);
            }
            mContentsRef = mDatabaseReference.child(Const.ContentsPATH);
            mContentsRef.addChildEventListener(mContentsEventListener);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        // 質問のリストをクリアしてから再度Adapterにセットし、AdapterをListViewにセットし直す
        mQuestionArrayList.clear();
        mAdapter.setQuestionArrayList(mQuestionArrayList);
        mListView.setAdapter(mAdapter);

        // 選択したジャンルにリスナーを登録する
        if (mGenreRef != null) {
            mGenreRef.removeEventListener(mEventListener);
        }
        mGenreRef = mDatabaseReference.child(Const.ContentsPATH).child(String.valueOf(mGenre));
        mGenreRef.addChildEventListener(mEventListener);

        return true;
    }
}
