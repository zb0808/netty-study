package cn.zb.study.demo.server.handler;

import cn.zb.study.demo.util.SessionUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @Description: 鉴权逻辑处理器
 * @Author: zb
 * @Date: 2020-03-10
 */
@ChannelHandler.Sharable // 加上注解标识，表明该 handler 是可以多个 channel 共享的
public class AuthHandler extends ChannelInboundHandlerAdapter {

    // 构造单例
    public static final AuthHandler INSTANCE = new AuthHandler();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!SessionUtil.hasLogin(ctx.channel())){
            ctx.channel().close();
        } else {
            // 一行代码实现逻辑的删除
            ctx.pipeline().remove(this);
            super.channelRead(ctx, msg);
        }
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        if (SessionUtil.hasLogin(ctx.channel())) {
            System.out.println("当前连接登录验证完毕，无需再次验证, AuthHandler 被移除");
        } else {
            System.out.println("无登录验证，强制关闭连接!");
        }
    }
}
