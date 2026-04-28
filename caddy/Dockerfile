FROM caddy:2.11-builder AS builder
RUN xcaddy build --with github.com/mholt/caddy-ratelimit

FROM caddy:2.11-alpine AS app
COPY --from=builder /usr/bin/caddy /usr/bin/caddy
