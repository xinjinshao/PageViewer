package com.shaoxinjin.pageviewer.websites.mhxxoo;

import com.shaoxinjin.pageviewer.MainPage;
import com.shaoxinjin.pageviewer.Util;
import com.shaoxinjin.pageviewer.websites.BaseWebOperation;
import com.shaoxinjin.pageviewer.websites.WebOperationView;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.concurrent.ThreadPoolExecutor;

public class Mhxxoo extends BaseWebOperation {
    public Mhxxoo(MainPage mainPage, ThreadPoolExecutor threadPoolExecutor) {
        super(mainPage, threadPoolExecutor);
    }

    @Override
    public WebOperationView getViewWebOperation() {
        return mWebOperationView;
    }

    @Override
    public void initWebInfo() {
        String url = "http://m.ccn2.com";
        String[] sections = new String[] {
            "/benzi/list_1_",
            "/wuyiniao/list_2_",
            "/lifanacg/list_4_"
        };
        initWebInfo(url, sections, Mhxxoo.class.getSimpleName());
        mWebOperationView = new MhxxooView();
    }

    @Override
    public int getTotalPageNum(String url) throws Exception {
        Document doc = Util.getDocument(url);
        String pageNum = doc.selectFirst("span.pageinfo strong").text();
        return pageNum == null ? 0 : Integer.valueOf(pageNum);
    }

    @Override
    public void setListFromHtmlTable(String url, String s) throws Exception {
        Document doc = Util.getDocument(url);
        Elements aTags = doc.select("div.news a");
        for (Element e : aTags) {
            String href = e.attr("href");
            Element img = e.selectFirst("img");
            if (img != null) {
                String name = img.attr("alt");
                HashMap<String, String> map = new HashMap<>();
                map.put(MainPage.TEXT_KEY, Util.getCommonName(name));
                if (s.equals("") && mMainPage.getInSearchStatus()) {
                    return;
                }
                if (!s.equals("") && !map.get(MainPage.TEXT_KEY).contains(s)) {
                    continue;
                }
                String imgSrc = img.attr("src");
                map.put(MainPage.IMAGE_KEY, imgSrc);
                map.put(MainPage.URL_KEY, URL_BASE + href);
                map.put(MainPage.TYPE_KEY, Mhxxoo.class.getSimpleName());
                mMainPage.updateCoverView(map);
            }
        }
    }
}
