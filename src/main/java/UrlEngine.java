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

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import oauth.signpost.OAuth;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.http.HttpParameters;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Random;
import java.util.SortedSet;

/**
 * @author Erich Eichinger
 * @since 01/03/2016
 */
public class UrlEngine implements Engine {
    private Random random = new Random();

    @Override
    public ResponseEntity<String> submit(JCurlRequestOptions requestOptions) throws Exception {
        System.setProperty("http.keepAlive", "true");

        ResponseEntity<String> responseEntity = null;
        URL obj = new URL(requestOptions.getUrl());

        for (int i=0;i< requestOptions.getCount();i++) {
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            OAuthConsumerCredentials oAuthConsumerCredentials = requestOptions.getOAuthConsumerCredentials();
            if (oAuthConsumerCredentials != null) {
                OAuthConsumer consumer = new DefaultOAuthConsumer(oAuthConsumerCredentials.getConsumerKey(),
                        oAuthConsumerCredentials.getConsumerSecret());
                OAuthUserCredentials oAuthUserCredentials = requestOptions.getOAuthUserCredentials();
                if (oAuthUserCredentials != null) {
                    consumer.setTokenWithSecret(oAuthUserCredentials.getUserToken(),
                            oAuthUserCredentials.getUserTokenSecret());
                    HttpParameters parameters = new HttpParameters();
                    for (Map.Entry<String, SortedSet<String>> parameterEntry : oAuthUserCredentials.getUserParameters()
                            .entrySet()) {
                        parameters.put(parameterEntry.getKey(), parameterEntry.getValue());
                    }
                    if (!parameters.containsKey(OAuth.OAUTH_NONCE)) {
                        String nonce = generateNonce();
                        if (StringUtils.isNotBlank(nonce)) {
                            parameters.put(OAuth.OAUTH_NONCE, nonce, true);
                        }
                    }
                    consumer.setAdditionalParameters(parameters);
                }
                consumer.sign(con);
            }

            // add request header
            con.setRequestMethod("GET");
            con.setConnectTimeout(2000);
            for(Map.Entry<String,String> e : requestOptions.getHeaderMap().entrySet()) {
                con.setRequestProperty(e.getKey(), e.getValue());
            }

            System.out.println("\nSending 'GET' request to URL : " + requestOptions.getUrl());
            long startTime = System.currentTimeMillis();
            con.connect();

            int responseCode = con.getResponseCode();
            System.out.println("Response Code : " + responseCode + "    Total Time: " + (System.currentTimeMillis() - startTime));

            final InputStream is = con.getInputStream();
            String response = IOUtils.toString(is);
            is.close();

            //print result
            System.out.println(response);

            responseEntity = new ResponseEntity<String>(response, HttpStatus.valueOf(responseCode));
        }
        return responseEntity;
    }

    private String generateNonce() {
        String nonce = null;
        if (this.random != null) {
            nonce = Long.toString(this.random.nextLong());
        }
        return nonce;
    }

}
