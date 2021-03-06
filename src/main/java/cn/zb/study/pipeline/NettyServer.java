package cn.zb.study.pipeline;

import cn.zb.study.pipeline.handler.inbound.InBoundHandlerA;
import cn.zb.study.pipeline.handler.inbound.InBoundHandlerB;
import cn.zb.study.pipeline.handler.inbound.InBoundHandlerC;
import cn.zb.study.pipeline.handler.outbound.OutBoundHandlerA;
import cn.zb.study.pipeline.handler.outbound.OutBoundHandlerB;
import cn.zb.study.pipeline.handler.outbound.OutBoundHandlerC;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.Date;

/**
 * @Description: Netty服务端
 * @Author: zb
 * @Date: 2020-02-25
 */
public class NettyServer {

    private static final Integer PORT = 8000;


    public static void main(String[] args) {
        NioEventLoopGroup bossGrup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        final ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap
                .group(bossGrup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) {
                        // inBound，处理读数据的逻辑链
                        ch.pipeline().addLast(new InBoundHandlerA());
                        ch.pipeline().addLast(new InBoundHandlerB());
                        ch.pipeline().addLast(new InBoundHandlerC());

                        // outBound，处理写数据的逻辑链
                        ch.pipeline().addLast(new OutBoundHandlerA());
                        ch.pipeline().addLast(new OutBoundHandlerB());
                        ch.pipeline().addLast(new OutBoundHandlerC());
                    }
                });

        bind(serverBootstrap, PORT);
    }

    /**
     * 绑定端口
     */
    private static void bind(final ServerBootstrap serverBootstrap, final int port) {
        serverBootstrap.bind(port).addListener(future -> {
            if (future.isSuccess()){
                System.out.println(new Date() + ": 端口[" + port + "]绑定成功!");
            } else {
                System.err.println(new Date() + ": 端口[" + port + "]绑定失败!");
            }
        });
    }
}
