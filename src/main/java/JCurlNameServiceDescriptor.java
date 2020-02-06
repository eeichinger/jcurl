/**
 * 
 */
import sun.net.spi.nameservice.*;

public final class JCurlNameServiceDescriptor implements NameServiceDescriptor {
    public NameService createNameService() throws Exception {
        return new JCurlNameService();
    }

    public String getProviderName() {
        return "mine";
    }

    public String getType() {
        return "dns";
    }
}
