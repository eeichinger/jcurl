import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class OAuthConsumerCredentials {
    private String consumerKey;
    private String consumerSecret;
}
