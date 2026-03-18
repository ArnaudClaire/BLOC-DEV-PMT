FROM node:20-alpine AS build
WORKDIR /app

COPY package.json package-lock.json ./
COPY nx.json project.json tsconfig.json tsconfig.app.json tsconfig.spec.json ./
COPY .postcssrc.json tailwind.config.js proxy.conf.json playwright.config.ts ./
COPY public ./public
COPY src ./src

RUN npm ci
RUN npm run build

FROM nginx:1.27-alpine
COPY nginx/default.conf.template /etc/nginx/templates/default.conf.template
COPY --from=build /app/dist/pmt-frontend/browser /usr/share/nginx/html

EXPOSE 80
