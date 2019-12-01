package com.shaoxinjin.pageviewer;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class Util {
    public static final String PREFIX = "PageViewer_";
    private static final String TAG = PREFIX + Util.class.getSimpleName();

    public static Document getDocument(String path) throws Exception {
        Document document = null;
        for (int i = 0; i < 5; i++) {
            try {
                document = Jsoup.connect(path).timeout(3000).get();
                break;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (document == null) {
            throw new Exception("getDocument retry several times, but failed.");
        }
        return document;
    }

    public static String getCommonPageUrl(String firstPicUrl, int pageNum) {
        String currentPage;
        if (pageNum == 1) {
            currentPage = firstPicUrl;
        } else {
            currentPage = firstPicUrl.replace(".html", "_" + pageNum + ".html");
        }
        return currentPage;
    }

    public static String getCommonName(String originName) {
        if (originName.split("：|之").length >= 2) {
            return originName.split("：|之")[1];
        } else {
            return originName;
        }
    }

    static void setPicFromUrl(Context context, String url, ImageView imageView) {
        RequestOptions options = new RequestOptions().placeholder(R.drawable.ic_loading);
        if (url.endsWith("gif")) {
            Glide.with(context).asGif().load(url).apply(options).into(imageView);
        } else {
            Glide.with(context).load(url).apply(options).into(imageView);
        }
    }
}
