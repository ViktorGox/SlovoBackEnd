package aad.message.app.middleware;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    private final GroupAccessInterceptor groupAccessInterceptor;
    private final AdminOwnerInterceptor adminOwnerInterceptor;
    private final AdminCannotModifyOwnerInterceptor adminCannotModifyOwnerInterceptor;

    public InterceptorConfig(GroupAccessInterceptor groupAccessInterceptor, AdminOwnerInterceptor adminOwnerInterceptor, AdminCannotModifyOwnerInterceptor adminCannotModifyOwnerInterceptor) {
        this.groupAccessInterceptor = groupAccessInterceptor;
        this.adminOwnerInterceptor = adminOwnerInterceptor;
        this.adminCannotModifyOwnerInterceptor = adminCannotModifyOwnerInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RequestBodyInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/login");

        registry.addInterceptor(groupAccessInterceptor)
                .addPathPatterns("/groups/**",
                        "/messages/{id:\\d+}")
                .excludePathPatterns("/login", "/groups");
        registry.addInterceptor(adminOwnerInterceptor)
                .addPathPatterns("/groups/{group_id}/{user_id}/{role_id}",
                        "/groups/{group_id}/{user_id}")
                .excludePathPatterns("/login",
                        "/groups/{id}/users",
                        "/groups/{groupId}/users/{userId}",
                        "/groups/{id}/name",
                        "/groups/{id}/image",
                        "/groups/{group_id}/self");
        registry.addInterceptor(adminCannotModifyOwnerInterceptor)
                .addPathPatterns("/groups/{group_id}/{user_id}/{role_id}",
                        "/groups/{group_id}/{user_id}")
                .excludePathPatterns("/login",
                        "/groups/{id}/users",
                        "/groups/{groupId}/users/{userId}",
                        "/groups/{id}/name",
                        "/groups/{id}/image",
                        "/groups/{group_id}/self");
    }
}
