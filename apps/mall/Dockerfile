# syntax=docker/dockerfile:1
# Must build from repo root (needs packages/fe-shared):
#   docker build -f apps/mall/Dockerfile .
ARG NPM_REGISTRY=https://registry.npmmirror.com
FROM node:22-alpine AS build
ARG NPM_REGISTRY
WORKDIR /src

COPY apps/mall/package.json apps/mall/package-lock.json ./apps/mall/
WORKDIR /src/apps/mall
RUN npm config set registry "${NPM_REGISTRY}" \
  && npm ci

WORKDIR /src
COPY packages/fe-shared ./packages/fe-shared
COPY apps/mall ./apps/mall
WORKDIR /src/apps/mall
RUN npx vite build

FROM nginx:1.27-alpine
COPY apps/mall/nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /src/apps/mall/dist /usr/share/nginx/html
EXPOSE 80
