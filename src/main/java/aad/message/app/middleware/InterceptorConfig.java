package aad.message.app.middleware;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    private final GroupAccessInterceptor groupAccessInterceptor;

    public InterceptorConfig(GroupAccessInterceptor groupAccessInterceptor) {
        this.groupAccessInterceptor = groupAccessInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RequestBodyInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/login");

        registry.addInterceptor(groupAccessInterceptor)
                .addPathPatterns("/groups/{id}", "/groups/{id}/users")
                .excludePathPatterns("/login");
    }
}
