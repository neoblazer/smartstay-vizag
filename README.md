# SmartStay Vizag

A hotel discovery and booking application for Visakhapatnam. The React frontend and Spring Boot API live in one repository.

## Project layout

| Path | Purpose |
| --- | --- |
| `frontend/` | React, Vite, Tailwind CSS, hotel search, booking, wishlist, payment flow, and admin dashboard |
| `backend/` | Spring Boot REST API, MySQL persistence, JWT authentication, hotel and room management |

The payment endpoint currently simulates a successful payment; it is not a live payment gateway.

## Run locally

Requirements: Node.js with npm, Java 17, and MySQL.

1. Create a MySQL database for the application.
2. Set the backend environment variables shown in `backend/.env.example` in your shell or IDE. At minimum, set `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, and `GOOGLE_CLIENT_ID`. The backend reads environment variables; it does not automatically load an `.env` file.
3. From `backend/`, run `./mvnw spring-boot:run` (Windows: `mvnw.cmd spring-boot:run`). The default port is 8080.
4. From `frontend/`, copy `.env.example` to `.env.local` and set the URL and Firebase public client settings you use. Run `npm ci`, then `npm run dev`. Vite uses port 3000 in this project.

The frontend uses `VITE_API_BASE_URL` for API calls; when omitted it calls `http://localhost:8080`. Firebase configuration is only needed for the Firebase phone sign-in flow.

## Features

- Hotel browsing and search, room availability, bookings, ratings, and wishlists
- Email/password, Google, and phone authentication flows
- JWT-secured user and admin endpoints
- Admin dashboard for hotels, rooms, bookings, users, and revenue summaries
- Simulated payment confirmation and booking history

## Deployment

This repository does not change the existing Vercel or Render deployments until their Git integration is switched.

| Service | Root directory | Build or runtime |
| --- | --- | --- |
| Vercel frontend | `frontend` | `npm run build`; output `dist` |
| Render or Railway backend | `backend` | Dockerfile in `backend/` (Java 17) |

Set `VITE_API_BASE_URL` on the frontend service to the deployed backend URL **without** a trailing slash. Set backend database, JWT, Google client ID, and `APP_CORS_ALLOWED_ORIGINS` environment variables on the backend service. Include the deployed frontend origin in `APP_CORS_ALLOWED_ORIGINS`, separated by commas if there is more than one origin. Keep credentials in the hosting provider's environment settings.

The original repositories remain available for their complete individual commit histories:

- [Frontend source](https://github.com/neoblazer/hotel-frontend)
- [Backend source](https://github.com/neoblazer/hotel-backend)

The initial commit in this repository imports the source snapshots under `frontend/` and `backend/`. Prior commits remain in the original repositories.
