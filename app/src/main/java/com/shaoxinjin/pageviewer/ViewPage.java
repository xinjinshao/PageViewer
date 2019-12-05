package com.shaoxinjin.pageviewer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.shaoxinjin.pageviewer.db.DbManager;
import com.shaoxinjin.pageviewer.websites.WebOperationView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ViewPage extends AppCompatActivity {
    private static final String TAG = Util.PREFIX + ViewPage.class.getSimpleName();
    private ImageViewAdapter mImageViewAdapter;
    private ThreadPoolExecutor mThreadPoolExecutor;
    private ArrayList<String> mList = new ArrayList<>();
    private int mCurrentPage;
    private String mCurrentName;
    private String mCurrentType;
    private String mCurrentUrl;
    private WebOperationView mWebOperationView;
    private int mCurrentColNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_page);
        mCurrentPage = 0;
        if (mThreadPoolExecutor == null) {
            mThreadPoolExecutor = new ThreadPoolExecutor(1, 1, 5,
                    TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>(1024));
        }

        final RecyclerView recyclerView = findViewById(R.id.content_image_view);
        mImageViewAdapter = new ImageViewAdapter(ViewPage.this);
        recyclerView.setAdapter(mImageViewAdapter);
        mCurrentColNum = 1;
        final StaggeredGridLayoutManager layoutManager =
            new StaggeredGridLayoutManager(mCurrentColNum, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        registerForContextMenu(recyclerView);

        FloatingActionButton fab = findViewById(R.id.fab_table);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentColNum == 1) {
                    mCurrentColNum = getResources().getInteger(R.integer.grid_columns);
                } else {
                    mCurrentColNum = 1;
                }
                final StaggeredGridLayoutManager layoutManager =
                    new StaggeredGridLayoutManager(mCurrentColNum, StaggeredGridLayoutManager.VERTICAL);
                recyclerView.setLayoutManager(layoutManager);
            }
        });
        Intent intent = getIntent();
        mCurrentType = intent.getStringExtra(MainPage.TYPE_KEY);
        mCurrentName = intent.getStringExtra(MainPage.TEXT_KEY);
        mCurrentUrl = intent.getStringExtra(MainPage.URL_KEY);
        mWebOperationView = intent.getParcelableExtra(MainPage.CLASS_KEY);
        updateImage();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mThreadPoolExecutor.shutdownNow();
    }

    private void starPicSet(int pageNum) {
        DbManager dbManager = DbManager.getInstance(this);
        dbManager.insertRecord(mCurrentType, mCurrentName, mCurrentUrl, mList.get(pageNum));
        Toast toast = Toast.makeText(this, getResources().getString(R.string.star_success), Toast.LENGTH_SHORT);
        toast.show();
    }

    private void updateImage() {
        updateSomeImages();
    }

    private void updateSomeImages() {
        mThreadPoolExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    while(true) {
                        mCurrentPage++;
                        String[] picUrl = mWebOperationView.getPicUrl(mCurrentUrl, mCurrentPage);
                        if (picUrl == null) {
                            return;
                        }
                        List<String> list = Arrays.asList(picUrl);
                        updateImageView(list);
                    }
                } catch (Exception e) {
                    Log.d(TAG, "exception in updateSomeImages is " + e.getClass());
                    e.printStackTrace();
                }
            }
        });
    }

    class ImageViewAdapter extends RecyclerView.Adapter<ImageViewAdapter.ImageViewHolder> {
        private Context mAdapterContext;

        private ImageViewAdapter(Context context) {
            mAdapterContext = context;
        }

        @Override
        public int getItemCount() {
            return mList.size();
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public @NonNull
        ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mAdapterContext).inflate(R.layout.image_item, parent, false);
            return new ImageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ImageViewHolder viewHolder, int position) {
            String imageData = mList.get(position);
            if (imageData != null) {
                Util.setPicFromUrl(ViewPage.this, imageData, viewHolder.imageView);
            }
            setMenuListener(viewHolder, position);
        }

        private void setMenuListener(ImageViewHolder viewHolder, final int pos) {
            viewHolder.itemView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
                @Override
                public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                    getMenuInflater().inflate(R.menu.viewpage_menu, menu);
                    for (int index = 0; index < menu.size(); index++) {
                        MenuItem item = menu.getItem(index);
                        if (item.getItemId() == R.id.viewpage_star) {
                            item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                                @Override
                                public boolean onMenuItemClick(MenuItem item) {
                                    starPicSet(pos);
                                    return false;
                                }
                            });
                        }
                    }
                }
            });
        }

        class ImageViewHolder extends RecyclerView.ViewHolder {
            private ImageView imageView;

            ImageViewHolder(View view) {
                super(view);
                imageView = view.findViewById(R.id.image_item);
            }
        }
    }

    public void updateImageView(final List<String> list) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mList.addAll(list);
                mImageViewAdapter.notifyDataSetChanged();
            }
        });
    }
}
