# --- Stage 1: build static assets with Node ---
FROM node:22-alpine AS build
WORKDIR /app

# Install dependencies first (cached layer, only re-runs when lockfile changes).
COPY package.json package-lock.json ./
RUN npm ci

# Build args become env vars Vite bakes into the static bundle at build time.
ARG VITE_API_URL
ARG VITE_USE_MOCKS
ENV VITE_API_URL=$VITE_API_URL
ENV VITE_USE_MOCKS=$VITE_USE_MOCKS

COPY . .
RUN npm run build

# --- Stage 2: serve the static files with nginx ---
FROM nginx:1.27-alpine AS run
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 80
