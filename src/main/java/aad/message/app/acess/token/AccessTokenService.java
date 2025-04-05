package aad.message.app.acess.token;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccessTokenService {

    private final AccessTokenRepository accessTokenRepository;

    public AccessTokenService(AccessTokenRepository accessTokenRepository) {
        this.accessTokenRepository = accessTokenRepository;
    }

    @Transactional
    public void deleteByUserId(Long userId) {
        accessTokenRepository.deleteAccessTokenByUser_Id(userId);
    }
}

