package sunday.app.bairead.parse;

import android.text.Html;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Created by sunday on 2017/1/17.
 */

public class ParseChapterText extends ParseXml {

    @Override
    public String parse() {

        if(document == null) return null;
        String s = "content_read";
        Elements elements = document.getElementsByClass(s);
        for (Element element : elements) {
            String idString = element.select("div[id]").attr("id");
            if (idString.equals("content")) {
                String text = element.select("div[id]").get(0).toString();
                return String.valueOf(Html.fromHtml(text)).trim();
            }
        }
        return null;
    }
}
