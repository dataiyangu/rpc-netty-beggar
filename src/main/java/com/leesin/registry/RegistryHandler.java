package com.leesin.registry;

import com.leesin.protocol.InvokerProtocol;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.string.StringEncoder;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @description:
 * @author: Leesin.Dong
 * @date: Created in 2020/3/31 21:51
 * @version: ${VERSION}
 * @modified By:
 */
public class RegistryHandler extends ChannelInboundHandlerAdapter {

    //保存所有的服务
    public static  ConcurrentHashMap<String, Object> registryMap = new ConcurrentHashMap<String, Object>();

    //保存所有的服务类
    public List<String> classNames = new ArrayList<String>();


    public RegistryHandler() throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        scanClass("com.leesin.provider");
        doRegister();
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Object result=null;
        InvokerProtocol protocol = (InvokerProtocol) msg;
        if (registryMap.containsKey(protocol.getClassName())) {
            String className = protocol.getClassName();
            String methodName = protocol.getMethodName();
            Class<?>[] parames = protocol.getParames();
            Object[] values = protocol.getValues();
            Method method = registryMap.get(protocol.getClassName()).getClass().getMethod(methodName, parames);
            result = method.invoke(registryMap.get(className), values);
        }
        ctx.write(result);
        ctx.flush();
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }

    private void scanClass(String packageName) {
        URL url = this.getClass().getClassLoader().getResource(packageName.replaceAll("\\.","/"));
        File file = new File(url.getFile());
        for (File listFile : file.listFiles()) {
            if (listFile.isDirectory()) {
                scanClass(packageName + "." + listFile.getName());
            } else {
                classNames.add(packageName+"."+listFile.getName().replace(".class", "").trim());
            }
        }

    }

    private void doRegister() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        if (classNames.isEmpty()) { return; }
        for (String className : classNames) {
            Class<?> clazz = Class.forName(className);
            Class<?> clazzInterface = clazz.getInterfaces()[0];
            registryMap.put(clazzInterface.getName(), clazz.newInstance());
        }

    }

    public static void main(String[] args) {
        // URL url = RegistryHandler.class.getClassLoader().getResource("com.leesin.provider");
        // URL url = RegistryHandler.class.getClassLoader().getResource("com/leesin/provider");
        URL url = RegistryHandler.class.getClassLoader().getResource("com.leesin.provider".replaceAll("\\.","/"));

        System.out.println(url);
    }
}
