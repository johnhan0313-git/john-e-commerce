# syntax=docker/dockerfile:1
# Must build from repo root (needs packages/fe-shared):
#   docker build -f apps/admin/Dockerfile .
ARG NPM_REGISTRY=https://registry.npmmirror.com
FROM node:22-alpine AS build
ARG NPM_REGISTRY
WORKDIR /src

COPY apps/admin/package.json apps/admin/package-lock.json ./apps/admin/
WORKDIR /src/apps/admin
RUN npm config set registry "${NPM_REGISTRY}" \
  && npm ci

WORKDIR /src
COPY packages/fe-shared ./packages/fe-shared
COPY apps/admin ./apps/admin
WORKDIR /src/apps/admin
RUN npx vite build

FROM nginx:1.27-alpine
COPY apps/admin/nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /src/apps/admin/dist /usr/share/nginx/html
EXPOSE 80
