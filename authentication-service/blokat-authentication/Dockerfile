from node:16-stretch

WORKDIR /usr/src/app

COPY package*.json ./
RUN yarn install

COPY prisma/ .
RUN npx prisma generate

COPY . .
RUN yarn build

COPY src/auth/saml/* dist/auth/saml/
COPY src/*.pem dist/

CMD [ "node", "dist/main.js" ]