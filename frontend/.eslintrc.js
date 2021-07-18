module.exports = {
  "root": true,
  "parserOptions": {
    "sourceType": "module",
    "ecmaVersion": 6
  },
  "rules": {
    // https://stackoverflow.com/q/39114446/2771889
    "linebreak-style": ["error", (process.platform === "win32" ? "windows" : "unix")],
  }
};
