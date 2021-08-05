package com.gxx.collectionuserbehaviorlibrary.costtime;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * @date 创建时间:2021/7/28 0028
 * @auther gaoxiaoxiong
 * @Descriptiion  方法进行耗时统计
 * https://juejin.cn/post/6844903466188406792#heading-4
 **/
@Target(ElementType.METHOD)
public @interface CostTime {
}