FROM node:16-buster

WORKDIR /usr/src/app

COPY package*.json ./
RUN yarn install

COPY prisma/ .
RUN npx prisma generate

COPY config/* config/

COPY . .
RUN yarn build


ENTRYPOINT  yarn run init-db && node dist/src/main.js
