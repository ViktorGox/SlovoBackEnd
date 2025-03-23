package aad.message.app.middleware;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    private final GroupAccessInterceptor groupAccessInterceptor;
    private final AdminOwnerInterceptor adminOwnerInterceptor;

    public InterceptorConfig(GroupAccessInterceptor groupAccessInterceptor, AdminOwnerInterceptor adminOwnerInterceptor) {
        this.groupAccessInterceptor = groupAccessInterceptor;
        this.adminOwnerInterceptor = adminOwnerInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RequestBodyInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/login");

        registry.addInterceptor(groupAccessInterceptor)
                .addPathPatterns("/groups/{id}",
                        "/groups/{id}/users",
                        "/messages/{id:\\d+}",
                        "/groups/{id}/name",
                        "/groups/{id}/image")
                .excludePathPatterns("/login");
        registry.addInterceptor(adminOwnerInterceptor)
                .addPathPatterns("")
                .excludePathPatterns("/login");
    }
}
