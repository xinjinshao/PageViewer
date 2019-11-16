package com.shaoxinjin.pageviewer.websites.semanhua;

import com.shaoxinjin.pageviewer.MainPage;
import com.shaoxinjin.pageviewer.Util;
import com.shaoxinjin.pageviewer.websites.BaseWebOperation;
import com.shaoxinjin.pageviewer.websites.WebOperationView;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.concurrent.ThreadPoolExecutor;

public class Semanhua extends BaseWebOperation {
    public Semanhua(MainPage mainPage, ThreadPoolExecutor threadPoolExecutor) {
        super(mainPage, threadPoolExecutor);
    }

    @Override
    public WebOperationView getViewWebOperation() {
        return mWebOperationView;
    }

    @Override
    public void initWebInfo() {
        URL_BASE = "https://m.wujiecaola.net";
        URL_END = ".html";
        mSectionInfo = new SectionInfo[]{
                new SectionInfo("/meinv/list_8_", 1, 0)};
        mWebOperationView = new SemanhuaView();
    }

    @Override
    public int getTotalPageNum(String url) throws Exception {
        Document doc = Util.getDocument(url);
        return doc.select("select.paging-select option").size();
    }

    @Override
    public void setListFromHtmlTable(String url, String s) throws Exception {
        Document doc = Util.getDocument(url);
        Elements aTags = doc.select("ul.pic a");
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
                /* covers in this website are wrong, use first pic as cover */
                String[] imgSrc = mWebOperationView.getPicUrl(URL_BASE + href, 1);
                map.put(MainPage.IMAGE_KEY, imgSrc[0]);
                map.put(MainPage.URL_KEY, URL_BASE + href);
                map.put(MainPage.TYPE_KEY, Semanhua.class.getSimpleName());
                mMainPage.updateGridView(map);
            }
        }
    }
}
