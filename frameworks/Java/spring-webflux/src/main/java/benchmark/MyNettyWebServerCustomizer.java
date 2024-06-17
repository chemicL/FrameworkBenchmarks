package benchmark;

import io.netty.channel.ChannelOption;
import io.netty.channel.epoll.EpollChannelOption;
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.stereotype.Component;

@Component
public class MyNettyWebServerCustomizer
		implements WebServerFactoryCustomizer<NettyReactiveWebServerFactory> {

	@Override
	public void customize(NettyReactiveWebServerFactory factory) {
		factory.addServerCustomizers(httpServer ->
				httpServer
//						.wiretap(true) // debugging only
						.option(ChannelOption.SO_BACKLOG, 8192)
						.option(ChannelOption.SO_REUSEADDR, true)
						.option(EpollChannelOption.SO_REUSEPORT, true)
						.childOption(ChannelOption.SO_REUSEADDR, true)
						.httpRequestDecoder(spec -> spec.maxInitialLineLength(4096)
								.maxHeaderSize(8192)
								.maxChunkSize(8192)
								.validateHeaders(false)));
	}
}