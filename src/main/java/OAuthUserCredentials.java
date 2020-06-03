import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;

import lombok.Data;

@Data
public class OAuthUserCredentials {
    private String userToken;
    private String userTokenSecret;
    private Map<String, SortedSet<String>> userParameters = new HashMap<>();

    public OAuthUserCredentials(String userToken, String userSecret) {
	this.userToken = userToken;
	this.userTokenSecret = userSecret;
    }
}
