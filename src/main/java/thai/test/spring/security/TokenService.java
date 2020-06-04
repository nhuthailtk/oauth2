package thai.test.spring.security;

public interface TokenService {

    Token createToken(Token token);
    Token findByToken(String token);
    RefreshToken createReToken(RefreshToken token);
    RefreshToken findByReToken(String token);
}
