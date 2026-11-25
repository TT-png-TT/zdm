package lx.mapper;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import org.hibernate.query.criteria.JpaPredicate;

import jakarta.persistence.criteria.Root;
import lx.model.PriceRecord;
import lx.utils.HibernateUtil;

public class PriceRecordMapper {

    public static void save(PriceRecord record) {
        try (Session session = HibernateUtil.getSessionFactory()) {
            Transaction transaction = session.beginTransaction();
            try {
                session.saveOrUpdate(record);
                transaction.commit();
            } catch (Exception e) {
                transaction.rollback();
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 查询指定同款键在近 days 天内的历史最低价格；无记录返回 null。
     * 列表取回后在 Java 侧取最低，避免不同 SQLite 方言下聚合函数的兼容问题。
     */
    public static Double minPriceSince(String matchKey, int days) {
        try (Session session = HibernateUtil.getSessionFactory()) {
            HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
            JpaCriteriaQuery<PriceRecord> query = builder.createQuery(PriceRecord.class);
            Root<PriceRecord> root = query.from(PriceRecord.class);
            String threshold = LocalDateTime.now().minusDays(days).toString();
            JpaPredicate predicate = builder.and(
                    builder.equal(root.get("matchKey"), matchKey),
                    builder.greaterThanOrEqualTo(root.get("observedAt"), threshold));
            query.select(root).where(predicate);
            List<PriceRecord> list = session.createQuery(query).list();
            Double min = null;
            for (PriceRecord record : list) {
                Double v = record.getPriceValue();
                if (v != null && (min == null || v < min)) {
                    min = v;
                }
            }
            return min;
        }
    }
}
