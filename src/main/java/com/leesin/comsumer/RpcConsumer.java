package com.leesin.comsumer;

import com.leesin.api.IRpcHelloService;
import com.leesin.api.IRpcService;
import com.leesin.comsumer.proxy.RpcProxy;

/**
 * @description:
 * @author: Leesin.Dong
 * @date: Created in 2020/3/31 22:40
 * @version: ${VERSION}
 * @modified By:
 */
public class RpcConsumer {
    public static void main(String[] args) {
        IRpcHelloService iRpcHelloService = RpcProxy.create(IRpcHelloService.class);
        String hello = iRpcHelloService.hello("帅帅帅");
        System.out.println(hello);


        IRpcService service = RpcProxy.create(IRpcService.class);
        System.out.println("8 + 2 = " + service.add(8, 2));
        System.out.println("8 - 2 = " + service.sub(8, 2));
        System.out.println("8 * 2 = " + service.mult(8, 2));
        System.out.println("8 / 2 = " + service.div(8, 2));
    }
}
