/*
 *  Copyright 2015-2016 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.ssl.*;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.Netty4ClientHttpRequestFactory;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.client.AsyncRestTemplate;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Erich Eichinger
 * @since 01/03/2016
 */
public class NNIOEngine implements Engine {
    @Override
    public ResponseEntity<String> submit(JCurlRequestOptions requestOptions) throws Exception {
        int ioWorkerCount = Runtime.getRuntime().availableProcessors() * 2;
        NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup(ioWorkerCount);

        try {
            final Netty4ClientHttpRequestFactory netty4ClientHttpRequestFactory = new Netty4ClientHttpRequestFactory(eventLoopGroup);
            netty4ClientHttpRequestFactory.setConnectTimeout(2000);
            netty4ClientHttpRequestFactory.setReadTimeout(2000);
/*
            SslContext sslContext = SslContextBuilder
                .forClient()
                .sslProvider(SslProvider.JDK)
                .build()
            ;
*/
            if (requestOptions.getUrl().toLowerCase().startsWith("https://")) {
                netty4ClientHttpRequestFactory.setSslContext(new JdkSslClientContext());
            }
            netty4ClientHttpRequestFactory.afterPropertiesSet();

            ResponseEntity<String> stringResponseEntity = null;
            for (int i = 0; i < requestOptions.getCount(); i++) {
                final HttpHeaders headers = new HttpHeaders();
                for(Map.Entry<String,String> e : requestOptions.getHeaderMap().entrySet()) {
                    headers.put(e.getKey(), Collections.singletonList(e.getValue()));
                }

                final HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

                AsyncRestTemplate template = new AsyncRestTemplate(netty4ClientHttpRequestFactory);
                final ListenableFuture<ResponseEntity<String>> exchange = template.exchange(requestOptions.getUrl(), HttpMethod.GET, requestEntity, String.class);
                stringResponseEntity = exchange.get();
                System.out.println(stringResponseEntity.getBody());
            }
            return stringResponseEntity;
        } finally {
            eventLoopGroup.shutdownGracefully(100, 500, TimeUnit.MILLISECONDS);
        }
    }
}
