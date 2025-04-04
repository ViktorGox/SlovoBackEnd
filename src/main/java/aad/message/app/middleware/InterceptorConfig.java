package aad.message.app.middleware;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    private final GroupAccessInterceptor groupAccessInterceptor;
    private final AdminOwnerInterceptor adminOwnerInterceptor;
    private final AdminCannotModifyOwnerInterceptor adminCannotModifyOwnerInterceptor;
    private final RegisterInterceptor registerInterceptor;
    private final UpdateUserInterceptor updateUserInterceptor;

    public InterceptorConfig(GroupAccessInterceptor groupAccessInterceptor, AdminOwnerInterceptor adminOwnerInterceptor, AdminCannotModifyOwnerInterceptor adminCannotModifyOwnerInterceptor, RegisterInterceptor registerInterceptor, UpdateUserInterceptor updateUserInterceptor) {
        this.groupAccessInterceptor = groupAccessInterceptor;
        this.adminOwnerInterceptor = adminOwnerInterceptor;
        this.adminCannotModifyOwnerInterceptor = adminCannotModifyOwnerInterceptor;
        this.registerInterceptor = registerInterceptor;
        this.updateUserInterceptor = updateUserInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(updateUserInterceptor)
                .addPathPatterns("/users");
        registry.addInterceptor(registerInterceptor)
                .addPathPatterns("/users");
        registry.addInterceptor(groupAccessInterceptor)
                .addPathPatterns("/groups/**",
                        "/messages/{id:\\d+}",
                        "/messages/{id:\\d+}/{id:\\d+}",
                        "/messages/group/{id:\\d+}/{id:\\d+}")
                .excludePathPatterns("/login", "/groups", "/groups/recentChats");
        registry.addInterceptor(adminOwnerInterceptor)
                .addPathPatterns("/groups/{group_id}/{user_id}/{role_id}",
                        "/groups/{group_id}/{user_id}",
                        "/groups/{group_id}/reminder")
                .excludePathPatterns("/login",
                        "/groups/{id}/users",
                        "/groups/{groupId}/users/{userId}",
                        "/groups/{id}/name",
                        "/groups/{id}/image",
                        "/groups/{group_id}/self",
                        "/groups/recentChats");
        registry.addInterceptor(adminCannotModifyOwnerInterceptor)
                .addPathPatterns("/groups/{group_id}/{user_id}/{role_id}",
                        "/groups/{group_id}/{user_id}")
                .excludePathPatterns("/login",
                        "/groups/{id}/users",
                        "/groups/{groupId}/users/{userId}",
                        "/groups/{id}/name",
                        "/groups/{id}/image",
                        "/groups/{group_id}/self",
                        "/groups/recentChats",
                        "/groups/{group_id}/reminder");
    }
}
