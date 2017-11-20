package com.netty.sample.client.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by sk-cailicheng on 2017/9/21.
 */
public class TcpClientHandler extends SimpleChannelInboundHandler<Object> {
    private static final Logger LOGGER = LoggerFactory.getLogger(TcpClientHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        LOGGER.info("client接收到服务器返回的消息:" + msg);
        System.out.println("client接收到服务器返回的消息:" + msg);
    }
}
