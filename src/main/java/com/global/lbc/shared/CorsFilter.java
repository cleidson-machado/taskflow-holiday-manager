package com.global.lbc.shared;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;

/**
 * Simple CORS Filter for Local Development
 *
 * This filter adds CORS headers to ALL responses, allowing the front-end
 * application running on localhost:3000 to access the API.
 *
 * For production, use application.properties configuration with specific domains.
 */
@Provider
public class CorsFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) throws IOException {

        // Allow requests from localhost:3000 (your React/Vue/Angular app)
        responseContext.getHeaders().add("Access-Control-Allow-Origin", "http://localhost:3000");

        // Allow credentials (cookies, authorization headers)
        responseContext.getHeaders().add("Access-Control-Allow-Credentials", "true");

        // Allow common HTTP methods
        responseContext.getHeaders().add("Access-Control-Allow-Methods",
            "GET, POST, PUT, DELETE, PATCH, OPTIONS");

        // Allow common headers
        responseContext.getHeaders().add("Access-Control-Allow-Headers",
            "Content-Type, Authorization, Accept, X-Requested-With");

        // Expose headers that the browser can read
        responseContext.getHeaders().add("Access-Control-Expose-Headers",
            "Content-Type, Content-Disposition");

        // Cache preflight requests for 24 hours
        responseContext.getHeaders().add("Access-Control-Max-Age", "86400");
    }
}

