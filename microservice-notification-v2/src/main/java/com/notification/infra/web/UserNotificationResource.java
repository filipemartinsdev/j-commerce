package com.notification.infra.web;

import com.notification.application.dto.UserNotificationResponse;
import com.notification.application.service.UserNotificationService;
import com.notification.domain.entity.UserNotification;
import io.github.responsekit.core.PagedResponse;
import io.github.responsekit.core.StandardResponse;
import io.quarkus.security.Authenticated;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.resteasy.reactive.RestPath;
import org.jboss.resteasy.reactive.RestQuery;
import org.jboss.resteasy.reactive.RestResponse;

import java.util.UUID;

@Path("/api/v2/notifications")
@Produces(MediaType.APPLICATION_JSON)
public class UserNotificationResource {
    private final UserNotificationService userNotificationService;
    private final JsonWebToken jwt;

    public UserNotificationResource(UserNotificationService userNotificationService, JsonWebToken jwt) {
        this.userNotificationService = userNotificationService;
        this.jwt = jwt;
    }

    @GET
    @Authenticated
    public RestResponse<StandardResponse<PagedResponse<UserNotificationResponse>>> getUserNotifications(
            @RestQuery @DefaultValue("0") int page,
            @RestQuery @DefaultValue("20") int size
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());

        return RestResponse.ok(
                StandardResponse.success(
                        userNotificationService.getAllByUserId(userId, page, size)
                ).build()
        );
    }

    @POST @Path("/{id}/view")
    @Authenticated
    public RestResponse<StandardResponse<Void>> viewUserNotification(@RestPath UUID id) {
        UUID userId = UUID.fromString(jwt.getSubject());
        userNotificationService.view(id, userId);

        return RestResponse.ok(
                StandardResponse.success().build()
        );
    }
}
