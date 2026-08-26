# syntax=docker/dockerfile:1
# Must build from repo root (needs packages/fe-shared):
#   docker build -f apps/merchant/Dockerfile .
ARG NPM_REGISTRY=https://registry.npmmirror.com
FROM node:22-alpine AS build
ARG NPM_REGISTRY
WORKDIR /src

COPY apps/merchant/package.json apps/merchant/package-lock.json ./apps/merchant/
WORKDIR /src/apps/merchant
RUN npm config set registry "${NPM_REGISTRY}" \
  && npm ci

WORKDIR /src
COPY packages/fe-shared ./packages/fe-shared
COPY apps/merchant ./apps/merchant
WORKDIR /src/apps/merchant
RUN npx vite build

FROM nginx:1.27-alpine
COPY apps/merchant/nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /src/apps/merchant/dist /usr/share/nginx/html
EXPOSE 80
