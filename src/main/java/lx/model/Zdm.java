package lx.model;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "ZDM")
public class Zdm implements Crawlable {

    //主键用值得买接口提供的id
    @Id
    @JSONField(name = "article_id")
    String articleId;

    //标题
    @JSONField(name = "article_title")
    String title;

    //优惠信息正文链接
    @JSONField(name = "article_url")
    String url;

    //优惠信息商品图片缩略图
    @JSONField(name = "article_pic_url")
    String picUrl;

    //价格
    @JSONField(name = "article_price")
    String price;

    //点值的数量
    @JSONField(name = "article_rating")
    String voted;

    //评论的数量
    @JSONField(name = "article_comment")
    String comments;

    //商城(京东/天猫/拼多多...)
    @JSONField(name = "article_mall")
    String articleMall;

    //优惠信息发布时间(由接口返回的时间戳格式, 因为不方便阅读,这个字段不持久化进数据库)
    @Transient
    @JSONField(name = "timesort")
    String timesort;

    //优惠信息发布时间(yyyy-MM-ddTHH:mm:ss)
    String article_time;

    //是否已推送
    Boolean pushed;

    /**
     * 平台角标(接口字段 zhifa_tag), 正常是 {"name":"历史低价","url":...}
     * 没有角标时可能是字符串"无", 或者整个字段缺失, 所以用 Object 接
     */
    @Transient
    @JSONField(name = "zhifa_tag")
    Object zhifaTag;

    /**
     * 兼容另一种返回结构: article_tags / article_tese_tags 数组, 元素形如 {"name":"新低"}
     * 当前取数接口没有这两个字段, 保留是为了接口变动时不用再改代码
     */
    @Transient
    @JSONField(name = "article_tags", alternateNames = {"article_tag"})
    List<JSONObject> articleTags;

    @Transient
    @JSONField(name = "article_tese_tags")
    List<JSONObject> teseTags;

    //已抽取的标签文本(逗号分隔), 不持久化
    @Transient
    String tags;

    /**
     * 合并所有来源的角标名(zhifa_tag / article_tags / article_tese_tags), 去重后用逗号连接。
     * 标签过滤和邮件展示都读这个方法, 所以无论接口改用哪个字段, 只要在下面加一个收集方法就行。
     */
    public String getTags() {
        Set<String> names = new LinkedHashSet<>();
        if (tags != null) {
            for (String s : tags.split(",")) {
                String t = s == null ? "" : s.trim();
                if (!t.isEmpty() && !"无".equals(t)) {
                    names.add(t);
                }
            }
        }
        collectZhifaTag(zhifaTag, names);
        collectNames(articleTags, names);
        collectNames(teseTags, names);
        return String.join(",", names);
    }

    private static void collectZhifaTag(Object raw, Set<String> out) {
        if (raw == null) {
            return;
        }
        String name = null;
        if (raw instanceof JSONObject) {
            name = ((JSONObject) raw).getString("name");
        } else if (raw instanceof String) {
            name = (String) raw;
        }
        if (name == null) {
            return;
        }
        name = name.trim();
        if (name.isEmpty() || "无".equals(name)) {
            return;
        }
        out.add(name);
    }

    private static void collectNames(List<JSONObject> src, Set<String> out) {
        if (src == null) {
            return;
        }
        for (JSONObject o : src) {
            if (o == null) {
                continue;
            }
            String name = o.getString("name");
            if (name == null || name.trim().isEmpty()) {
                name = o.getString("title");
            }
            if (name != null && !name.trim().isEmpty()) {
                out.add(name.trim());
            }
        }
    }

    @Override
    public String toHtmlTr() {
        String tagText = getTags();
        String tagHtml = tagText.isEmpty()
                ? ""
                : "<br/><span style='color:#e4393c;font-size:12px'>【" + tagText + "】</span>";
        return "<tr>" +
                "<td><img src='" + picUrl + "'/></td>" +
                "<td>" + "<a target='_blank' href='" + url + "'>" + title + "</a>" + tagHtml + "</td>" +
                "<td>" + price + "</td>" +
                "<td>" + voted + "/" + comments + "</td>" +
                "<td>" + articleMall + "</td>" +
                "</tr>";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Zdm) {
            return articleId.equals(((Zdm) obj).articleId);
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(articleId);
    }
}
