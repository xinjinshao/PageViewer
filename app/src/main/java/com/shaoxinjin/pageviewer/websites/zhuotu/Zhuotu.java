package com.shaoxinjin.pageviewer.websites.zhuotu;

import com.shaoxinjin.pageviewer.MainPage;
import com.shaoxinjin.pageviewer.Util;
import com.shaoxinjin.pageviewer.websites.BaseWebOperation;
import com.shaoxinjin.pageviewer.websites.WebOperationView;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.concurrent.ThreadPoolExecutor;

public class Zhuotu extends BaseWebOperation {
    public Zhuotu(MainPage mainPage, ThreadPoolExecutor threadPoolExecutor) {
        super(mainPage, threadPoolExecutor);
    }

    @Override
    public WebOperationView getViewWebOperation() {
        return mWebOperationView;
    }

    @Override
    public void initWebInfo() {
        String url = "http://m.win4000.com";
        String[] sections = new String[] {
            "/meitu_2_",
            "/meitu_3_",
            "/meitu_4_",
            "/meitu_29_",
            "/meitu_31_",
            "/meitu_32_",
            "/meitu_47_",
            "/meitu_59_"
        };
        initWebInfo(url, sections, Zhuotu.class.getSimpleName());
        mWebOperationView = new ZhuotuView();
    }

    @Override
    public int getTotalPageNum(String url) throws Exception {
        Document doc = Util.getDocument(url);
        Element element = doc.select("div.ym a").last();
        if (element != null) {
            String href = element.attr("href");
            return Integer.valueOf(href.split(".html")[0].split("_")[2]);
        }
        return 0;
    }

    @Override
    public void setListFromHtmlTable(String url, String s) throws Exception {
        Document doc = Util.getDocument(url);
        Elements aTags = doc.select("div.img_cont a");
        for (Element e : aTags) {
            String href = e.attr("href");
            Element img = e.selectFirst("img");
            if (img != null) {
                String imgSrc = img.attr("data-original");
                if (imgSrc == null || imgSrc.equals("")) {
                    continue;
                }
                String name = img.attr("alt");
                HashMap<String, String> map = new HashMap<>();
                map.put(MainPage.TEXT_KEY, Util.getCommonName(name));
                if (s.equals("") && mMainPage.getInSearchStatus()) {
                    return;
                }
                if (!s.equals("") && !map.get(MainPage.TEXT_KEY).contains(s)) {
                    continue;
                }
                map.put(MainPage.IMAGE_KEY, imgSrc);
                map.put(MainPage.URL_KEY, href);
                map.put(MainPage.TYPE_KEY, Zhuotu.class.getSimpleName());
                mMainPage.updateCoverView(map);
            }
        }
    }
}
