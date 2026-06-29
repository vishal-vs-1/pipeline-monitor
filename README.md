# Pipeline Monitor Dashboard

A real-time CI/CD pipeline monitoring dashboard that automatically detects performance degradation and flaky builds across multiple GitHub repositories and branches.

This project is structured as a **monorepo**, containing both a Spring Boot backend and an Angular frontend.

## Architecture

* **Backend (`/vigilant-backend`)**: A Spring Boot application that acts as a polling engine. It queries the GitHub Actions API, stores build states in PostgreSQL, publishes events to Apache Kafka, runs anomaly detection algorithms, and pushes real-time updates via WebSockets.
* **Frontend (`/vigilant-frontend`)**: An Angular 17+ single-page application styled with TailwindCSS and DaisyUI. It subscribes to WebSocket topics to display live pipeline statuses and pop up alerts for detected anomalies.
* **Infrastructure**: `docker-compose.yml` provides a local PostgreSQL database and a Kafka broker (with Zookeeper) to run the backend services.

## Prerequisites

* Java 21+
* Node.js & npm
* Docker Desktop (for Postgres and Kafka)
* A GitHub Personal Access Token (Classic) with `repo` (or `public_repo`) scope.

## Getting Started

1. **Start Infrastructure**: Run `docker-compose up -d` in the root directory to spin up Postgres and Kafka.
2. **Backend**: 
   * Create a `.env` file inside `vigilant-backend` based on your database configuration.
   * Run the backend using `./mvnw spring-boot:run` from inside the `vigilant-backend` directory.
3. **Frontend**:
   * Navigate to `vigilant-frontend` and run `npm install`.
   * Start the dev server using `npm run start`.
   * Open `http://localhost:4200` in your browser.

## Features

* **Real-time Synchronization**: WebSockets ensure the dashboard updates instantly without page reloads.
* **Flaky Pipeline Detection**: Alerts if a pipeline fails multiple times consecutively.
* **Performance Degradation Alert**: Alerts if a build takes 1.5x longer than the historical average.
* **Branch Tracking**: Track and monitor different branches of the same repository independently.
