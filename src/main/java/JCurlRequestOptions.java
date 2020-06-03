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

import java.util.Map;
import java.util.TreeMap;

import lombok.Data;
import lombok.NonNull;

/**
 * @author Erich Eichinger
 * @since 21/03/2016
 */
@Data
public class JCurlRequestOptions {

    public static final String USER_AGENT = "Mozilla/5.0; jcurl";

    @NonNull
    private String url;
    private boolean parallel = false;
    private int count = 1;
    private final Map<String, String> headerMap = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
    private OAuthConsumerCredentials oAuthConsumerCredentials;
    private OAuthUserCredentials oAuthUserCredentials;

    public JCurlRequestOptions() {
        headerMap.put("User-Agent", USER_AGENT);
        headerMap.put("Accept", "*/*");
    }

    public JCurlRequestOptions setHeader(@NonNull String header, String value) {
        headerMap.put(header, value);
        return this;
    }


}
