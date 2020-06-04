package thai.test.spring.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TokenServiceImpl implements TokenService {

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Override
    public Token findByToken(String token) {
        return tokenRepository.findByToken(token);
    }

    @Override
    public RefreshToken createReToken(RefreshToken token) {
        return refreshTokenRepository.saveAndFlush(token);
    }

    @Override
    public RefreshToken findByReToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public Token createToken(Token token){
        return tokenRepository.saveAndFlush(token);
    }
}