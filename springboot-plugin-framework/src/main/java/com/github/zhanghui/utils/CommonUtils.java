package com.github.zhanghui.utils;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

/**
 * Description:
 * 通用工具类
 * @author createdBy huizhang43.
 * @date createdAt 2021/2/7 11:05
 **/
public class CommonUtils {

    /**
     * 倒序排序
     * @param list
     * @param keyFunction
     * @returnDES
     */
    public static <T> List<T> sortWithDES(List<T> list, Function<T,Integer> keyFunction){
        if(list == null || list.isEmpty()){
            return list;
        }
        Collections.sort(list, Comparator.comparing(keyFunction, Comparator.nullsLast(Comparator.reverseOrder())));
        return list;
    }

    /**
     * 对 OrderPriority 进行排序操作
     * @param order OrderPriority
     * @param <T> 当前操作要被排序的bean
     * @return Comparator
     */
    public static <T> Comparator<T> orderPriority(final Function<T, OrderPriority> order){
        return Comparator.comparing(t -> {
            OrderPriority orderPriority = order.apply(t);
            if(orderPriority == null){
                orderPriority = OrderPriority.getLowPriority();
            }
            return orderPriority.getPriority();
        }, Comparator.nullsLast(Comparator.reverseOrder()));
    }
}
