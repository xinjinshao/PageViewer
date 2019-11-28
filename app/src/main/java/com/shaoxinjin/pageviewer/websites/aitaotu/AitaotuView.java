package com.shaoxinjin.pageviewer.websites.aitaotu;

import android.os.Parcel;
import android.os.Parcelable;

import com.shaoxinjin.pageviewer.Util;
import com.shaoxinjin.pageviewer.websites.WebOperationView;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class AitaotuView implements WebOperationView {
    private int totalPicNum = Integer.MAX_VALUE;

    private int getTotalPicNum(Document doc) {
        String picNum = doc.selectFirst("div.clearfix.article-page a").text();
        /* format is "1/xx" */
        return picNum == null ? 0 : Integer.valueOf(picNum.split("/")[1]);
    }

    @Override
    public String[] getPicUrl(String firstPicUrl, int pageNum) throws Exception {
        String currentPage = Util.getCommonPageUrl(firstPicUrl, pageNum);
        Document doc = Util.getDocument(currentPage);
        if (pageNum == 1) {
            totalPicNum = getTotalPicNum(doc);
        }
        if (totalPicNum < pageNum) {
            return null;
        }
        Elements elements = doc.select("div.clearfix.arcmain img");
        String[] srcs = new String[elements.size()];
        for (int i = 0; i < srcs.length; i++) {
            srcs[i] = elements.get(i).attr("src").trim();
        }
        return srcs;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
    }

    public static final Parcelable.Creator<AitaotuView> CREATOR = new Creator<AitaotuView>() {
        @Override
        public AitaotuView createFromParcel(Parcel source) {
            return new AitaotuView();
        }

        @Override
        public AitaotuView[] newArray(int size) {
            return new AitaotuView[size];
        }
    };
}
