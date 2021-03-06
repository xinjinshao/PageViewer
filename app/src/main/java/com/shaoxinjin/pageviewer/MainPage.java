package com.shaoxinjin.pageviewer;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.shaoxinjin.pageviewer.db.DbManager;
import com.shaoxinjin.pageviewer.db.DbWebRecord;
import com.shaoxinjin.pageviewer.websites.Star;
import com.shaoxinjin.pageviewer.websites.WebOperation;
import com.shaoxinjin.pageviewer.websites.WebOperationView;
import com.shaoxinjin.pageviewer.websites.aitaotu.Aitaotu;
import com.shaoxinjin.pageviewer.websites.mhxxoo.Mhxxoo;
import com.shaoxinjin.pageviewer.websites.semanhua.Semanhua;
import com.shaoxinjin.pageviewer.websites.wuzhi.Wuzhi;
import com.shaoxinjin.pageviewer.websites.xixi.Xixi;
import com.shaoxinjin.pageviewer.websites.zhuotu.Zhuotu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MainPage extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = Util.PREFIX + MainPage.class.getSimpleName();
    public static final String IMAGE_KEY = "image_key";
    public static final String TEXT_KEY = "text_key";
    public static final String URL_KEY = "url_key";

    public static final String TYPE_KEY = "type_key";
    public static final String CLASS_KEY = "class_key";

    private CoverViewAdapter mCoverViewAdapter;
    private ArrayList<HashMap<String, String>> mList = new ArrayList<>();
    private int mCurrentID;
    private WebOperation mWebOperation;
    private HashMap<String, WebOperation> webOperationMap;
    private static ThreadPoolExecutor mThreadPoolExecutor;
    private boolean inSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        FloatingActionButton fab = findViewById(R.id.fab_more);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!inSearch && isNotStarPage() && mWebOperation != null) {
                    Log.d(TAG, "fab onclick updatePage");
                    mWebOperation.updatePage();
                }
            }
        });

        FloatingActionButton change = findViewById(R.id.fab_change);
        change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String clazz = getStringById(mCurrentID);
                if (clazz.equals("")) {
                    return;
                }
                final DbWebRecord dbWebRecord = DbManager.getInstance(MainPage.this).queryWebRecord(clazz);
                if (dbWebRecord == null) {
                    Log.d(TAG, "dbWebRecord is null, class is " + clazz);
                    return;
                }
                String display = dbWebRecord.url + "|" + dbWebRecord.sections;
                final EditText et = new EditText(MainPage.this);
                et.setText(display);
                new AlertDialog.Builder(MainPage.this).setTitle(clazz)
                    .setView(et)
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String[] text = et.getText().toString().split("\\|");
                            dbWebRecord.url = text[0];
                            dbWebRecord.sections = text[1];
                            DbManager.getInstance(MainPage.this).insertWebRecord(dbWebRecord);
                        }
                    }).setNegativeButton("Cancel",null).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (inSearch) {
            if (mWebOperation != null) {
                inSearch = false;
                mList.clear();
                updateSearchPercentage(0);
                mCoverViewAdapter.notifyDataSetChanged();
                mWebOperation.updatePage();
            }
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_page, menu);
        initSearchView(menu.findItem(R.id.action_search));
        initCoverView();
        initWebOperation();
        initUserInfo();
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (!userValid()) {
            return true;
        }
        // Handle navigation view item clicks here.
        if (mCurrentID == item.getItemId()) {
            return true;
        }
        inSearch = false;
        mList.clear();
        mCoverViewAdapter.notifyDataSetChanged();
        updateSearchPercentage(0);

        mCurrentID = item.getItemId();

        Log.d(TAG, "current id is " + mCurrentID);
        mWebOperation = webOperationMap.get(getStringById(mCurrentID));
        if (mWebOperation != null) {
            mWebOperation.updatePage();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void initUserInfo() {
        ImageView imageView = findViewById(R.id.user_icon);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final EditText inputServer = new EditText(MainPage.this);
                AlertDialog.Builder builder = new AlertDialog.Builder(MainPage.this);
                builder.setTitle("Input User Name").setIcon(android.R.drawable.ic_dialog_info).setView(inputServer);
                builder.setNegativeButton("Cancel", null);
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        TextView textView = findViewById(R.id.user_name);
                        textView.setText(inputServer.getText());
                    }
                });
                builder.show();
            }
        });
    }

    private boolean userValid() {
        TextView textView = findViewById(R.id.user_name);
        return getString(R.string.user_control).equals(textView.getText().toString());
    }

    private boolean isNotStarPage() {
        return mCurrentID != R.id.nav_star;
    }

    private String getStringById(int id) {
        switch (id) {
            case R.id.nav_star:
                return Star.class.getSimpleName();
            case R.id.nav_mhxxoo:
                return Mhxxoo.class.getSimpleName();
            case R.id.nav_semanhua:
                return Semanhua.class.getSimpleName();
            case R.id.nav_zhuotu:
                return Zhuotu.class.getSimpleName();
            case R.id.nav_xixi:
                return Xixi.class.getSimpleName();
            case R.id.nav_aitaotu:
                return Aitaotu.class.getSimpleName();
            case R.id.nav_wuzhi:
                return Wuzhi.class.getSimpleName();
        }
        return "";
    }

    private void initWebOperation() {
        webOperationMap = new HashMap<>();
        webOperationMap.put(Star.class.getSimpleName(), new Star(MainPage.this, mThreadPoolExecutor));
        webOperationMap.put(Mhxxoo.class.getSimpleName(), new Mhxxoo(MainPage.this, mThreadPoolExecutor));
        webOperationMap.put(Semanhua.class.getSimpleName(), new Semanhua(MainPage.this, mThreadPoolExecutor));
        webOperationMap.put(Zhuotu.class.getSimpleName(), new Zhuotu(MainPage.this, mThreadPoolExecutor));
        webOperationMap.put(Xixi.class.getSimpleName(), new Xixi(MainPage.this, mThreadPoolExecutor));
        webOperationMap.put(Aitaotu.class.getSimpleName(), new Aitaotu(MainPage.this, mThreadPoolExecutor));
        webOperationMap.put(Wuzhi.class.getSimpleName(), new Wuzhi(MainPage.this, mThreadPoolExecutor));
    }

    private void initSearchView(MenuItem menuItem) {
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                if (mWebOperation != null && isNotStarPage()) {
                    inSearch = true;
                    mList.clear();
                    mCoverViewAdapter.notifyDataSetChanged();
                    mWebOperation.searchPage(s);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
    }

    private void initCoverView() {
        RecyclerView recyclerView = findViewById(R.id.content_cover_view);
        mCoverViewAdapter = new CoverViewAdapter(MainPage.this);
        recyclerView.setAdapter(mCoverViewAdapter);
        int column = getResources().getInteger(R.integer.grid_columns);
        final StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(column, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        registerForContextMenu(recyclerView);
        if (mThreadPoolExecutor == null) {
            mThreadPoolExecutor = new ThreadPoolExecutor(50, 200, 5,
                    TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>(1024));
        }
    }

    private void deleteStarSet(int index) {
        DbManager dbManager = DbManager.getInstance(this);
        dbManager.deleteRecord(mList.get(index).get(URL_KEY));

        mList.remove(mList.get(index));
        mCoverViewAdapter.notifyDataSetChanged();

        Toast toast = Toast.makeText(this, getResources().getString(R.string.delete_star_success), Toast.LENGTH_SHORT);
        toast.show();
    }

    class CoverViewAdapter extends RecyclerView.Adapter<CoverViewAdapter.CoverViewHolder> {
        private Context mAdapterContext;

        private CoverViewAdapter(Context context) {
            mAdapterContext = context;
            setHasStableIds(true);
        }

        @Override
        public @NonNull CoverViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mAdapterContext).inflate(R.layout.cover_item, parent, false);
            return new CoverViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CoverViewHolder viewHolder, int position) {
            HashMap<String, String> map = mList.get(position);
            if (map == null) {
                Log.d(TAG, "map is null");
                return;
            }
            String imageData = map.get(IMAGE_KEY);
            String textData = map.get(TEXT_KEY);

            if (imageData != null && textData != null) {
                Util.setPicFromUrl(MainPage.this, imageData, viewHolder.imageView);
                viewHolder.textView.setText(textData);
            }

            setOnClickListener(viewHolder, position);
            setMenuListener(viewHolder, position);
        }

        private void setOnClickListener(CoverViewHolder viewHolder, final int pos) {
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    HashMap<String, String> map = mList.get(pos);
                    if (map == null) {
                        return;
                    }
                    WebOperation webOperation = webOperationMap.get(mList.get(pos).get(TYPE_KEY));
                    if (webOperation == null) {
                        return;
                    }
                    WebOperationView webOperationView= webOperation.getViewWebOperation();
                    Intent intent = new Intent(MainPage.this, ViewPage.class);
                    intent.putExtra(TYPE_KEY, getStringById(mCurrentID));
                    intent.putExtra(TEXT_KEY, mList.get(pos).get(TEXT_KEY));
                    intent.putExtra(URL_KEY, mList.get(pos).get(URL_KEY));
                    intent.putExtra(CLASS_KEY, webOperationView);
                    startActivity(intent);
                }
            });
        }

        private void setMenuListener(CoverViewHolder viewHolder, final int pos) {
            viewHolder.itemView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
                @Override
                public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                    if (isNotStarPage()) {
                        return;
                    }
                    getMenuInflater().inflate(R.menu.mainpage_menu, menu);
                    for (int index = 0; index < menu.size(); index++) {
                        MenuItem item = menu.getItem(index);
                        if (item.getItemId() == R.id.mainpage_delete_set) {
                            item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                                @Override
                                public boolean onMenuItemClick(MenuItem item) {
                                    deleteStarSet(pos);
                                    return false;
                                }
                            });
                        }
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mList.size();
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        class CoverViewHolder extends RecyclerView.ViewHolder {
            private View itemView;
            private ImageView imageView;
            private TextView textView;

            CoverViewHolder(View view) {
                super(view);
                itemView = view;
                imageView = view.findViewById(R.id.image_item);
                textView = view.findViewById(R.id.text_item);
            }
        }
    }

    public void updateCoverView(final HashMap<String, String> map) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mList.add(map);
                mCoverViewAdapter.notifyDataSetChanged();
            }
        });
    }

    public void updateSearchPercentage(int percentage) {
        ProgressBar progressBar = findViewById(R.id.search_progress);
        progressBar.setProgress(percentage);
    }

    public boolean getInSearchStatus() {
        return inSearch;
    }
}
