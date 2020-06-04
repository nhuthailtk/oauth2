package thai.test.spring.security;

public interface UserService {
    User createUser(User user);
    UserPrincipal findByUsername(String username);
}