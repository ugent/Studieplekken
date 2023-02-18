### STAGE 1: Build ###

FROM node:14.21-alpine AS build
WORKDIR /app
RUN npm install -g @angular/cli
COPY package.json package-lock.json ./
RUN npm install --legacy-peer-deps
COPY . .

ARG configuration=production

RUN ng build --configuration=${configuration}

RUN echo ${configuration}

### STAGE 2: Run ###

FROM nginx:1.17.1-alpine
COPY nginx.conf /etc/nginx/nginx.conf
COPY --from=build /app/dist/frontend /usr/share/nginx/html
