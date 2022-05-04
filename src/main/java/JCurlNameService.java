import java.io.File;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import io.netty.resolver.HostsFileEntries;
import io.netty.resolver.HostsFileParser;
import sun.net.spi.nameservice.NameService;

public class JCurlNameService implements NameService {
    private static String hostNames;
    private HostsFileEntries systemEntries;
    private HostsFileEntries customEntries;
    private Map<Inet4Address, String> s4;
    private Map<Inet6Address, String> s6;
    private Map<Inet4Address, String> c4;
    private Map<Inet6Address, String> c6;

    public static void setHostNames(String hostNames) {
        JCurlNameService.hostNames = hostNames;
    }

    public JCurlNameService() throws Exception {
        HostsFileEntries hfe;
        if (hostNames == null) {
            hfe = new HostsFileEntries(new HashMap<String, Inet4Address>(), new HashMap<String, Inet6Address>());
        } else {
            hfe = HostsFileParser.parse(new File(hostNames));
        }
        this.init(hfe);
    }

    public void init(HostsFileEntries customEntries) {
        systemEntries = HostsFileParser.parseSilently();
        this.customEntries = customEntries;
        s4 = invert4(systemEntries.inet4Entries());
        s6 = invert6(systemEntries.inet6Entries());
        c4 = invert4(customEntries.inet4Entries());
        c6 = invert6(customEntries.inet6Entries());
    }

    // Fixme: Handle duplicate Inet4Addresses?
    private Map<Inet4Address, String> invert4(Map<String, Inet4Address> m) {
        Map<Inet4Address, String> n = new HashMap<>();
        for (Entry<String, Inet4Address> e: m.entrySet()) {
            n.put(e.getValue(), e.getKey());
        }
        return n;
    }

    // Fixme: Handle duplicate Inet6Addresses?
    private Map<Inet6Address, String> invert6(Map<String, Inet6Address> m) {
        Map<Inet6Address, String> n = new HashMap<>();
        for (Entry<String, Inet6Address> e: m.entrySet()) {
            n.put(e.getValue(), e.getKey());
        }
        return n;
    }

    private InetAddress lookup(String host, HostsFileEntries he) {
        InetAddress ia = this.customEntries.inet4Entries().get(host);
        if (ia == null) {
            ia = this.customEntries.inet6Entries().get(host);
        }
        return ia;
    }
    public InetAddress[] lookupAllHostAddr(String host) throws UnknownHostException {
        InetAddress ia = lookup(host, customEntries);
        if (ia == null) {
            ia = lookup(host, systemEntries);
        }
        if (ia == null) {
            throw new UnknownHostException(host);
        }
        return new InetAddress[]{ia};
    }

    public String getHostByAddr(byte[] addr) throws UnknownHostException {
        InetAddress ia = InetAddress.getByAddress(addr);
        String host = c4.get(ia);
        if (host == null) {
            host = c6.get(ia);
        }
        if (host == null) {
            host = s4.get(ia);
        }
        if (host == null) {
            host = s6.get(ia);
        }
        if (host == null) {
            host = ia.getHostName();
        }
        if (host == null) {
            throw new UnknownHostException(Arrays.toString(addr));
        }
        return host;
    }
}
