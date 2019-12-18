package com.shaoxinjin.pageviewer.websites.wuzhi;

import com.shaoxinjin.pageviewer.MainPage;
import com.shaoxinjin.pageviewer.Util;
import com.shaoxinjin.pageviewer.websites.BaseWebOperation;
import com.shaoxinjin.pageviewer.websites.WebOperationView;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.concurrent.ThreadPoolExecutor;

public class Wuzhi extends BaseWebOperation {
    public Wuzhi(MainPage mainPage, ThreadPoolExecutor threadPoolExecutor) {
        super(mainPage, threadPoolExecutor);
    }

    @Override
    public WebOperationView getViewWebOperation() {
        return mWebOperationView;
    }

    @Override
    public void initWebInfo() {
        URL_BASE = "http://www.mmee33.com";
        URL_END = ".html";
        mSectionInfo = new SectionInfo[]{
            new SectionInfo("/raad_6_", 1, 0)};
        mWebOperationView = new WuzhiView();
    }

    @Override
    public int getTotalPageNum(String url) throws Exception {
        Document doc = Util.getDocument(url);
        String pageNum = doc.selectFirst("a.end").text();
        return pageNum == null ? 0 : Integer.valueOf(pageNum);
    }

    @Override
    public void setListFromHtmlTable(String url, String s) throws Exception {
        Document doc = Util.getDocument(url);
        Elements aTags = doc.select("ul.textList a");
        for (Element e : aTags) {
            String name = e.text();
            HashMap<String, String> map = new HashMap<>();
            map.put(MainPage.TEXT_KEY, Util.getCommonName(name));
            if (s.equals("") && mMainPage.getInSearchStatus()) {
                return;
            }
            if (!s.equals("") && !map.get(MainPage.TEXT_KEY).contains(s)) {
                continue;
            }
            String href = e.attr("href");
            /* no covers in this website, use first pic as cover */
            String[] imgSrc = mWebOperationView.getPicUrl(URL_BASE + href, 1);
            map.put(MainPage.IMAGE_KEY, imgSrc[0]);
            map.put(MainPage.URL_KEY, URL_BASE + href);
            map.put(MainPage.TYPE_KEY, Wuzhi.class.getSimpleName());
            mMainPage.updateCoverView(map);
        }
    }
}