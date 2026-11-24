package lx.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 价格历史观测记录：按同款键(matchKey)沉淀每次抓到的价格，用于近 N 天最低价比较。
 * 主键复用爆料 articleId，一条爆料对应一条观测记录。
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "PRICE_HISTORY")
public class PriceRecord {

    @Id
    String id;

    // 同款键：商城 + 归一化标题
    String matchKey;

    // 解析出的数值价格
    Double priceValue;

    String articleId;

    String title;

    String mall;

    String url;

    // 观测时间(yyyy-MM-ddTHH:mm:ss)
    String observedAt;
}
