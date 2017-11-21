package com.netty.sample.client.server;

import com.netty.sample.client.handler.TcpServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;


/**
 * Created by sk-cailicheng on 2017/9/21.
 */
public class TcpServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(TcpServer.class);

    private static final String IP = "127.0.0.1";
    private static final int PORT = 8989;
    /**
     * 用于分配处理业务线程的线程组个数
     */
    private static final int GROUP_SIZE = Runtime.getRuntime().availableProcessors() * 2; // 默认
    /**
     * 业务出现线程大小
     */
    private static final int THREAD_SIZE = 4;
    /**
     * NioEventLoopGroup实际上就是个线程池,
     * NioEventLoopGroup在后台启动了n个NioEventLoop来处理Channel事件,
     * 每一个NioEventLoop负责处理m个Channel,
     * NioEventLoopGroup从NioEventLoop数组里挨个取出NioEventLoop来处理Channel
     */
    private static final EventLoopGroup bossGroup = new NioEventLoopGroup(GROUP_SIZE);
    private static final EventLoopGroup workerGroup = new NioEventLoopGroup(THREAD_SIZE);

    private static void run() throws Exception {
        //ssl
        KeyStore ks = KeyStore.getInstance("JKS");
        InputStream ksInputStream = new FileInputStream("D:\\work\\Netty-Sample\\netty-server-sample\\src\\main\\resources\\sChat.jks");
        ks.load(ksInputStream, "sNetty".toCharArray());
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, "sNetty".toCharArray());
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), null, null);

        final SSLEngine sslEngine = sslContext.createSSLEngine();
        sslEngine.setUseClientMode(false); //服务器端模式
        sslEngine.setNeedClientAuth(false); //不需要验证客户端


        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup);
        b.channel(NioServerSocketChannel.class);
        b.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();
//                pipeline.addFirst("ssl", new SslHandler(sslEngine));

                pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
                pipeline.addLast("frameEncoder", new LengthFieldPrepender(4));
                pipeline.addLast("decoder", new StringDecoder(CharsetUtil.UTF_8));
                pipeline.addLast("encoder", new StringEncoder(CharsetUtil.UTF_8));
                pipeline.addLast(new IdleStateHandler(10, 0, 0), new TcpServerHandler());
            }
        });

        b.bind(IP, PORT).sync();
        LOGGER.info("TCP服务器已启动");
    }

    protected static void shutdown() {
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
    }

    public static void main(String[] args) throws Exception {
        LOGGER.info("启动TCP服务器...");
        TcpServer.run();
        // TcpServer.shutdown();
    }
}
