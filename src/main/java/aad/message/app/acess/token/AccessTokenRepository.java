package aad.message.app.acess.token;

import aad.message.app.refresh_token.RefreshToken;
import aad.message.app.user.User;

import java.util.Optional;

public interface AccessTokenRepository {
    Optional<RefreshToken> findByUser(User user);

    Optional<RefreshToken> findByUserId(Long userId);
}
