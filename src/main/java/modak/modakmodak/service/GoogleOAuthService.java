package modak.modakmodak.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class GoogleOAuthService {

    @Value("${google.oauth.client-id}")
    private String clientId;

    /**
     * Google ID 토큰을 검증하고 사용자 정보를 추출합니다.
     * 
     * @param idToken Google에서 발급한 ID 토큰
     * @return GoogleIdToken.Payload (사용자 정보)
     * @throws Exception 토큰 검증 실패 시
     */
    public GoogleIdToken.Payload verifyToken(String idToken) throws Exception {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                new GsonFactory())
                .setAudience(Collections.singletonList(clientId))
                .build();

        GoogleIdToken googleIdToken = verifier.verify(idToken);

        if (googleIdToken == null) {
            throw new IllegalArgumentException("유효하지 않은 Google ID 토큰입니다.");
        }

        return googleIdToken.getPayload();
    }

    /**
     * Payload에서 사용자 정보를 추출합니다.
     */
    public String getEmail(GoogleIdToken.Payload payload) {
        return payload.getEmail();
    }

    public String getProviderId(GoogleIdToken.Payload payload) {
        return payload.getSubject(); // Google 고유 사용자 ID
    }

    public String getName(GoogleIdToken.Payload payload) {
        return (String) payload.get("name");
    }

    public String getPictureUrl(GoogleIdToken.Payload payload) {
        return (String) payload.get("picture");
    }
}
