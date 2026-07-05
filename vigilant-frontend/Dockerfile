# Stage 1: Build the Angular app
FROM node:20-alpine AS builder
WORKDIR /app

# Copy package metadata and install dependencies
COPY package.json package-lock.json ./
RUN npm ci

# Copy the rest of the source code
COPY . .

# Build the app in production mode
RUN npm run build --configuration=production

# Stage 2: Serve the app with Nginx
FROM nginx:alpine

# Remove default nginx static assets
RUN rm -rf /usr/share/nginx/html/*

# Copy the build output from the builder stage
COPY --from=builder /app/dist/vigilant-frontend/browser /usr/share/nginx/html

# Copy custom nginx configuration for Angular routing
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Expose port 80
EXPOSE 80

# Start nginx
CMD ["nginx", "-g", "daemon off;"]
