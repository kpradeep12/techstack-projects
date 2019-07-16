package micronaut.config.api;

import io.micronaut.context.annotation.Value;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;

@Controller("/user")
public class UserController {
    @Value("${welcome-message:Hello all!}")
    private String message;

    @Get(produces = MediaType.TEXT_PLAIN)
    public String index() {
        return message;
    }
}
