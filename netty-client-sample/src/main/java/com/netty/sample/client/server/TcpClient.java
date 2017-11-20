package com.netty.sample.client.server;

import com.netty.sample.client.handler.TcpClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;

/**
 * Created by sk-cailicheng on 2017/9/21.
 */
public class TcpClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(TcpClient.class);

    private static String HOST = "127.0.0.1";
    private static int PORT = 8989;

    private static Bootstrap bootstrap = getBootstrap();
    private static Channel channel = getChannel(HOST, PORT);

    /**
     * 初始化Bootstrap
     */
    private static Bootstrap getBootstrap() {
        //ssl
        try {
            KeyStore ks = KeyStore.getInstance("JKS");
            InputStream ksInputStream = new FileInputStream("D:\\work\\Netty-Sample\\netty-client-sample\\src\\main\\resources\\cChat.jks");
            ks.load(ksInputStream, "cNetty".toCharArray());
            TrustManagerFactory tf = TrustManagerFactory.getInstance("SunX509");
            tf.init(ks);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tf.getTrustManagers(), null);
            final SSLEngine sslEngine = sslContext.createSSLEngine();

            sslEngine.setUseClientMode(true); //服务器端模式
//            sslEngine.setNeedClientAuth(true); //不需要验证客户端


            EventLoopGroup group = new NioEventLoopGroup();
            Bootstrap b = new Bootstrap();
            b.group(group).channel(NioSocketChannel.class);
            b.handler(new ChannelInitializer<Channel>() {
                @Override
                protected void initChannel(Channel ch) throws Exception {
                    ChannelPipeline pipeline = ch.pipeline();
                    pipeline.addFirst("ssl", new SslHandler(sslEngine));

                    pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
                    pipeline.addLast("frameEncoder", new LengthFieldPrepender(4));
                    pipeline.addLast("decoder", new StringDecoder(CharsetUtil.UTF_8));
                    pipeline.addLast("encoder", new StringEncoder(CharsetUtil.UTF_8));
                    pipeline.addLast("handler", new TcpClientHandler());
                }
            });
            b.option(ChannelOption.SO_KEEPALIVE, true);
            return b;
        } catch (Exception e) {
            System.out.println("error: " + e);
        }
        return null;
    }

    private static Channel getChannel(String host, int port) {
        Channel channel = null;
        try {
            channel = bootstrap.connect(host, port).sync().channel();
        } catch (Exception e) {
            LOGGER.error("连接Server(IP{},PORT{})失败", host, port, e);
            System.out.println("连接Server(IP{" + host + "},PORT{" + port + "})失败" + e);
            return null;
        }
        return channel;
    }

    private static void sendMsg(String msg) throws Exception {
        if (channel != null) {
            channel.writeAndFlush(msg).sync();
        } else {
            LOGGER.warn("消息发送失败,连接尚未建立!");
            System.out.println("消息发送失败,连接尚未建立!");
        }
    }

    public static void main(String[] args) {
        try {
            long t0 = System.nanoTime();
            for (int i = 0; i < 100; i++) {
                TcpClient.sendMsg(i + "你好!");
            }

            long t1 = System.nanoTime();
            LOGGER.info("time used:{}", t1 - t0);
            System.out.println("time used:{" + (t1 - t0) + "}");
        } catch (Exception e) {
            LOGGER.error("main err:", e);
            System.out.println("main err:" + e);
        }
    }
}
