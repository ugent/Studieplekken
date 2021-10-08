from node:16-stretch

WORKDIR /usr/src/app

COPY package*.json ./
RUN yarn install

COPY prisma/ .
RUN npx prisma generate

COPY . .
RUN yarn build

CMD [ "node", "dist/main.js" ]