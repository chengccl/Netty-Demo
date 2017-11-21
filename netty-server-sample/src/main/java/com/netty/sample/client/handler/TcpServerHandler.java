package com.netty.sample.client.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * tcp server处理器
 * Created by sk-cailicheng on 2017/11/21.
 */
public class TcpServerHandler extends SimpleChannelInboundHandler<Object> {
    private static final Logger LOGGER = LoggerFactory.getLogger(TcpServerHandler.class);

    /**
     * 建立连接处理
     * */
    @Override
    public void channelActive (ChannelHandlerContext ctx) throws Exception {
        LOGGER.info("连接建立，channel id ：" + ctx.channel().id());
    }

    /**
     * 接收到消息处理
     * */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        LOGGER.info("SERVER接收到channel " + ctx.channel().id() + " 消息:" + msg);
        ctx.channel().writeAndFlush("server accepted msg:" + msg);
    }

    /**
     * 异常处理
     * */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.warn("exceptionCaught!", cause);
        ctx.close();
    }

    /**
     * 心跳机制处理
     * */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (IdleStateEvent.class.isAssignableFrom(evt.getClass())) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                LOGGER.info("read idle");
            }
            else if (event.state() == IdleState.WRITER_IDLE) {
                LOGGER.info("write idle");
            }
            else if (event.state() == IdleState.ALL_IDLE) {
                LOGGER.info("all idle");
            }
        }
    }

    /**
     * 断开连接处理
     * */
    @Override
    public void channelInactive (ChannelHandlerContext ctx) throws Exception {
        LOGGER.info("连接断开，channel id ：" + ctx.channel().id());
    }
}
