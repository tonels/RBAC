package com.dxj.common.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;
import com.dxj.common.annotation.Query;

import javax.persistence.criteria.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author dxj
 * @date 2019-6-4 14:59:48
 */
@Slf4j
public class BaseQuery {

    /**
     * @描述 :  转换为Predicate
     * @作者 :  Dong ZhaoYang
     * @日期 :  2017/8/7
     * @时间 :  17:25
     */
    @SuppressWarnings("unchecked")
    public static <R, Q> Predicate getPredicate(Root<R> root, Q query, CriteriaBuilder cb) {
        List<Predicate> list = new ArrayList<>();

        if (query == null) {
            return cb.and(list.toArray(new Predicate[0]));
        }
        try {
            List<Field> fields = getAllFields(query.getClass(), new ArrayList<>());
            for (Field field : fields) {
                boolean accessible = field.isAccessible();
                field.setAccessible(true);
                Query q = field.getAnnotation(Query.class);
                if (q != null) {
                    String propName = q.propName();
                    String joinName = q.joinName();
                    String attributeName = isBlank(propName) ? field.getName() : propName;
                    Class<?> fieldType = field.getType();
                    Object val = field.get(query);
                    if (ObjectUtil.isNull(val)) {
                        continue;
                    }
                    Join join = null;
                    if (ObjectUtil.isNotEmpty(joinName)) {
                        switch (q.join()) {
                            case LEFT:
                                join = root.join(joinName, JoinType.LEFT);
                                break;
                            case RIGHT:
                                join = root.join(joinName, JoinType.RIGHT);
                                break;
                        }
                    }
                    switch (q.type()) {
                        case EQUAL:
                            list.add(cb.equal(getExpression(attributeName, join, root)
                                    .as((Class<? extends Comparable>) fieldType), val));
                            break;
                        case GREATER_THAN_EQUAL:
                            list.add(cb.greaterThanOrEqualTo(getExpression(attributeName, join, root)
                                    .as((Class<? extends Comparable>) fieldType), (Comparable) val));
                            break;
                        case LESS_THAN_EQUAL:
                            list.add(cb.lessThanOrEqualTo(getExpression(attributeName, join, root)
                                    .as((Class<? extends Comparable>) fieldType), (Comparable) val));
                            break;
                        case GREATER_THAN:
                            list.add(cb.greaterThan(getExpression(attributeName, join, root)
                                    .as((Class<? extends Comparable>) fieldType), (Comparable) val));
                        case LESS_THAN:
                            list.add(cb.lessThan(getExpression(attributeName, join, root)
                                    .as((Class<? extends Comparable>) fieldType), (Comparable) val));
                            break;
                        case LIKE:
                            list.add(cb.like(getExpression(attributeName, join, root)
                                    .as(String.class), "%" + val.toString() + "%"));
                            break;
                        case IN:
                            if (CollUtil.isNotEmpty((Collection<Long>) val)) {
                                list.add(getExpression(attributeName, join, root).in((Collection<Long>) val));
                            }
                            break;
                    }
                }
                field.setAccessible(accessible);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return cb.and(list.toArray(new Predicate[0]));
    }

    @SuppressWarnings("unchecked")
    private static <T, R> Expression<T> getExpression(String attributeName, Join join, Root<R> root) {
        if (ObjectUtil.isNotEmpty(join)) {
            return join.get(attributeName);
        } else return root.get(attributeName);
    }

    private static boolean isBlank(final CharSequence cs) {
        int strLen;
        if (cs == null || (strLen = cs.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private static List<Field> getAllFields(Class clazz, List<Field> fields) {
        if (clazz != null) {
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            getAllFields(clazz.getSuperclass(), fields);
        }
        return fields;
    }
}
