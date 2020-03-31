package com.leesin.protocol;

import lombok.Data;

import java.io.Serializable;

/**
 * @description:
 * @author: Leesin.Dong
 * @date: Created in 2020/3/31 21:13
 * @version: ${VERSION}
 * @modified By:
 */
@Data
public class InvokerProtocol implements Serializable {
    private String className;
    private String methodName;
    private Class<?>[] parames;//形参列表
    private Object[] values;//实参列表
}
