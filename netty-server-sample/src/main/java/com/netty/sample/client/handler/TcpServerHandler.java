package com.netty.sample.client.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by sk-cailicheng on 2017/9/21.
 */
public class TcpServerHandler extends SimpleChannelInboundHandler<Object> {
    private static final Logger LOGGER = LoggerFactory.getLogger(TcpServerHandler.class);

    @Override
    public void channelActive (ChannelHandlerContext ctx) throws Exception {
        System.out.println(ctx.name());
        System.out.println(ctx.attr(AttributeKey.valueOf("mac")));
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println(ctx.channel().remoteAddress());
        LOGGER.info("SERVER接收到消息:" + msg);
        System.out.println("SERVER接收到消息:" + msg);
        ctx.channel().writeAndFlush("server accepted msg:" + msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.warn("exceptionCaught!", cause);
        System.out.println("exceptionCaught!" + cause);
        ctx.close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (IdleStateEvent.class.isAssignableFrom(evt.getClass())) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE)
                System.out.println("read idle");
            else if (event.state() == IdleState.WRITER_IDLE)
                System.out.println("write idle");
            else if (event.state() == IdleState.ALL_IDLE)
                System.out.println("all idle");
        }
    }

    @Override
    public void channelInactive (ChannelHandlerContext ctx) throws Exception {
        System.out.println("out");
    }
}
