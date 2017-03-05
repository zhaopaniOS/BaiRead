package sunday.app.bairead.UI;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import sunday.app.bairead.DataBase.BookInfo;
import sunday.app.bairead.R;
import sunday.app.bairead.Tool.NetworkTool;
import sunday.app.bairead.View.BookcaseView;
import sunday.app.bairead.presenter.BookcasePresenter;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener, BookcasePresenter.IBookcasePresenterListener {

    SearchFragment searchFragment;

    private NetworkTool networkTool = new NetworkTool(this);
    private BookcasePresenter bookcasePresenter;
    private ListView mListView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private BookListAdapter booklistAdapter = new BookListAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        //TextView textView = (TextView) toolbar.findViewById(R.id.toolbar_title);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        setupView();
        bookcasePresenter = new BookcasePresenter(this, this);
        bookcasePresenter.init();
        registerReceiver();

    }

    private void setupView() {
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setColorSchemeColors(0xFFFF0000);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                bookcasePresenter.checkNewChapter(booklistAdapter.getBookInfoList());
            }
        });
        mListView = (ListView) findViewById(R.id.xlist_view);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BookInfo bookInfo = (BookInfo) booklistAdapter.getItem(position);
                BookcasePresenter.readBook(getBaseContext(), bookInfo);
            }
        });
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final int fPosition = position;
                showConfirmDialog("是否从书架中删除","确定","取消",new DialogListener() {
                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onConfirmed() {
                        BookInfo bookInfo = (BookInfo) booklistAdapter.getItem(fPosition);
                        booklistAdapter.getBookInfoList().remove(bookInfo);
                        booklistAdapter.notifyDataSetChanged();

                        bookcasePresenter.deleteBook(bookInfo);
                    }

                });
                return true;
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (getFragmentManager().getBackStackEntryCount() > 0) {
            searchFragment.back();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_search) {
            //转到搜索界面fragment
            searchFragment = new SearchFragment();
            searchFragment.show(this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unRegisterReceiver();
    }

    public void registerReceiver() {
        networkTool.addReceiver();
    }

    public void unRegisterReceiver() {
        networkTool.removeReceiver();
    }

    @Override
    public void loadBookStart() {
        showProgressDialog("");
    }

    @Override
    public void loadBookFinish(ArrayList<BookInfo> bookList) {
        hideProgressDialog();
        booklistAdapter = new BookListAdapter();
        booklistAdapter.setBookInfoList(bookList);
        mListView.setAdapter(booklistAdapter);
    }

    @Override
    public void onCheckNewChapter(BookInfo bookInfo) {
        ViewHolder viewHolder = indexOf(bookInfo.bookDetail.getId());
        if (viewHolder != null) {
            viewHolder.setValue(bookInfo);
        }

    }

    @Override
    public void onCheckFinish() {
        swipeRefreshLayout.setRefreshing(false);
    }

    public ViewHolder indexOf(long bookId) {
        int count = mListView.getChildCount();
        for (int i = 0; i < count; i++) {
            View view = mListView.getChildAt(i);
            if (view instanceof BookcaseView) {
                ViewHolder viewHolder = (ViewHolder) view.getTag();
                if (viewHolder.getBookId() == bookId) {
                    return viewHolder;
                }
            }
        }
        return null;
    }

    class ViewHolder {
        TextView nameTView;
        TextView chapterLatestTView;
        TextView chapterIndexTView;
        TextView updateImageTView;
        TextView updateTimeTView;
        private long bookId;

        ViewHolder(ViewGroup parent) {
            nameTView = (TextView) parent.findViewById(R.id.xlist_item_name);
            chapterLatestTView = (TextView) parent.findViewById(R.id.xlist_item_chapter_latest);
            chapterIndexTView = (TextView) parent.findViewById(R.id.xlist_item_chapter_index);
            updateImageTView = (TextView) parent.findViewById(R.id.xlist_item_chapter_update);
            updateTimeTView = (TextView) parent.findViewById(R.id.xlist_item_update_time);
        }

        public void setValue(BookInfo bookInfo) {
            String name = bookInfo.bookDetail.getName();
            String chapterLatest = bookInfo.bookDetail.getChapterLatest();
            int chapterIndex = bookInfo.bookChapter.getChapterIndex() + 1;
            int chapterCount = bookInfo.bookChapter.getChapterCount();
            String chapterText = String.valueOf(chapterIndex) + "/" + String.valueOf(chapterCount);
            nameTView.setText(name);
            chapterLatestTView.setText(chapterLatest);
            chapterIndexTView.setText(chapterText);
            updateTimeTView.setText(bookInfo.bookDetail.getUpdateTime());
            bookId = bookInfo.bookDetail.getId();
        }

        public long getBookId() {
            return bookId;
        }
    }

    public class BookListAdapter extends BaseAdapter {

        private ArrayList<BookInfo> bookInfos;

        public ArrayList<BookInfo> getBookInfoList() {
            return bookInfos;
        }

        public void setBookInfoList(ArrayList<BookInfo> list) {
            bookInfos = list;
        }

        @Override
        public int getCount() {
            return bookInfos.size();
        }

        @Override
        public Object getItem(int position) {
            return bookInfos.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(MainActivity.this).inflate(R.layout.xlist_item, null);
                ViewHolder viewHolder = new ViewHolder((ViewGroup) convertView);
                convertView.setTag(viewHolder);
            }
            ViewHolder viewHolder = (ViewHolder) convertView.getTag();
            viewHolder.setValue(bookInfos.get(position));
            return convertView;
        }
    }

}
