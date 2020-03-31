package com.leesin.registry;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.serialization.ClassResolver;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;


/**
 * @description:
 * @author: Leesin.Dong
 * @date: Created in 2020/3/31 21:28
 * @version: ${VERSION}
 * @modified By:
 */
public class RpcRegistry {
    private int port;

    public RpcRegistry(int port) {
        this.port = port;
    }

    public void start() {
        EventLoopGroup boosGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(boosGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            //（1） maxFrameLength - 发送的数据包最大长度；
                            //
                            // （2） lengthFieldOffset - 长度域偏移量，指的是长度域位于整个数据包字节数组中的下标；
                            //
                            // （3） lengthFieldLength - 长度域的自己的字节数长度。
                            //
                            // （4） lengthAdjustment – 长度域的偏移量矫正。 如果长度域的值，除了包含有效数据域的长度外，还包含了其他域（如长度域自身）长度，那么，就需要进行矫正。矫正的值为：包长 - 长度域的值 – 长度域偏移 – 长度域长。
                            //
                            // （5） initialBytesToStrip – 丢弃的起始字节数。丢弃处于有效数据前面的字节数量。比如前面有4个节点的长度域，则它的值为4。
                            //第一个参数为1024，表示数据包的最大长度为1024；第二个参数0，表示长度域的偏移量为0，也就是长度域放在了最前面，处于包的起始位置；
                            // 第三个参数为4，表示长度域占用4个字节；第四个参数为0，表示长度域保存的值，仅仅为有效数据长度，不包含其他域（如长度域）的长度；
                            // 第五个参数为4，表示最终的取到的目标数据包，抛弃最前面的4个字节数据，长度域的值被抛弃。
                            socketChannel.pipeline().addLast(new LengthFieldBasedFrameDecoder(
                                    Integer.MAX_VALUE, 0, 4, 0, 4));
                            //自定义协议编码器
                            socketChannel.pipeline().addLast(new LengthFieldPrepender(4));
                            //对象类型编码器
                            socketChannel.pipeline().addLast("encoder", new ObjectEncoder());
                            //对象类型解码器
                            socketChannel.pipeline().addLast("decoder", new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(null)));
                            socketChannel.pipeline().addLast(new RegistryHandler());
                        }
                    }).option(ChannelOption.SO_BACKLOG, 128)
                    .option(ChannelOption.SO_KEEPALIVE, true);
            ChannelFuture future = serverBootstrap.bind(port).sync();
            System.out.println("RPC Registry start listen at "+ port);
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            boosGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        new RpcRegistry(8080).start();
    }
}
