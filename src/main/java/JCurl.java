
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

import java.io.IOException;
import java.util.logging.LogManager;

import ch.qos.logback.classic.BasicConfigurator;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;

import static java.util.Arrays.asList;

public class JCurl {

    public static void main(String[] args) throws Exception {
        new JCurl().execute(args);
    }

    @NonNull
    final OptionParser parser;
    @NonNull
    final OptionSpec<String> headerSpec;
    @NonNull
    final OptionSpec<String> engineOptionSpec;
    @NonNull
    final OptionSpec<String> hostnamesSpec;

    public JCurl() {
        parser = new OptionParser();
        parser.acceptsAll(asList("help", "h"), "print help");
        headerSpec = parser
            .acceptsAll(asList("header", "H"), "header syntax equivalent to curl, e.g. '-H \"Accept: application/json\"'")
            .withRequiredArg()
            .ofType(String.class);
        parser.acceptsAll(asList("verbose", "v"), "activate verbose logging");
        parser.acceptsAll(asList("count", "c"), "repeat call x times")
              .withRequiredArg()
              .ofType(Integer.class);
        engineOptionSpec = parser
            .acceptsAll(asList("engine", "e")
                , "which engine to use:" +
                    "\n'url': java.net.URL (default)" +
                    "\n'hc': Apache HttpClient" +
                    "\n'hcnio': Apache HttpAsyncClient" +
                    "\n'nnio': Netty" +
                    "\n'okhttp': OkHttp" +
                    "\n'jetty': Jetty"
            )
            .withRequiredArg()
            .ofType(String.class);
        hostnamesSpec = parser.acceptsAll(asList("hostnames", "n"), "hostnames /etc/hosts")
            .withRequiredArg()
            .ofType(String.class);
    }

    public ResponseEntity<String> execute(String... args) throws Exception {
        final LoggerContext context = initLogging();

        System.out.println("Starting jCurl in " + System.getProperty("user.dir"));

        OptionSet optionSet = parseOptionSet(args);
        if (optionSet == null) {
            return null;
        }

        final JCurlRequestOptions options = new JCurlRequestOptions();

        String url = String.valueOf(optionSet.nonOptionArguments().get(0));
        options.setUrl(url);

        JCurlEngineType engineType = JCurlEngineType.URL;
        if (optionSet.has("engine")) {
            engineType = JCurlEngineType.valueOf(optionSet.valueOf(engineOptionSpec).toUpperCase());
        }

        if (optionSet.has("verbose")) {
            LogManager.getLogManager().getLogger("").setLevel(java.util.logging.Level.ALL);
            context.getLogger(Logger.ROOT_LOGGER_NAME).setLevel(Level.ALL);
        }

        if (optionSet.has("count")) {
            options.setCount(Integer.valueOf("" + optionSet.valueOf("count")));
        }

        if (optionSet.has("header")) {
            for (String header : optionSet.valuesOf(headerSpec)) {
                String[] vals = header.split(":");
                if (vals.length == 1) {
                    options.setHeader(vals[0].trim(), null);
                } else {
                    options.setHeader(vals[0].trim(), vals[1].trim());
                }
            }
        }
        
        if (optionSet.has("hostnames")) {
            String hostnames = optionSet.valueOf(hostnamesSpec);
            JCurlNameService.setHostNames(hostnames);
            System.setProperty("sun.net.spi.nameservice.provider.1", "dns,mine");
        }

        return engineType.getEngine().submit(options);
    }

    private OptionSet parseOptionSet(String[] args) throws IOException {
        OptionSet optionSet = parser.parse(args);

        if (args.length == 0 || optionSet.has("help")) {
            printUsage(parser);
            return null;
        }

        if (optionSet.nonOptionArguments().size() != 1) {
            System.err.println("missing <url> argument");
            printUsage(parser);
            return null;
        }
        return optionSet;
    }

    protected void printUsage(OptionParser parser) throws IOException {
        System.out.println("Usage: jcurl [options...] <url>");
        System.out.println();
        System.out.println("Note: options may be used in their long form with 2 hyphens or abbreviated using 1 hyphen, e.g. '--count 3' and '-c 3' are equivalent");
        System.out.println();
        parser.printHelpOn(System.out);
    }

    protected LoggerContext initLogging() throws IOException {
        LogManager.getLogManager().reset();
        LogManager.getLogManager().readConfiguration();
        LogManager.getLogManager().getLogger("").setLevel(java.util.logging.Level.INFO);

        final LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        context.reset();
        BasicConfigurator configurator = new BasicConfigurator();
        configurator.setContext(context);
        configurator.configure(context);
        context.getLogger(Logger.ROOT_LOGGER_NAME).setLevel(Level.INFO);
        org.slf4j.bridge.SLF4JBridgeHandler.removeHandlersForRootLogger();
        org.slf4j.bridge.SLF4JBridgeHandler.install();
        return context;
    }
}
