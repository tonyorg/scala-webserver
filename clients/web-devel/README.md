# monarchy-web-devel

This is a web-based API client for the monarchy backend intended for easing development and debugging.

### Deploying to Heroku

Set the environment config vars FRONTEND_URL and BACKEND_URL for CORS

### Usage

Install dependencies

```
$ yarn
```

Run development server

```
$ yarn dev
```

### Building

```
$ yarn build
```

Will create a `dist` directory containing your compiled code.

Depending on your needs, you might want to do more optimization to the production build.

## Webpack Bundle Analyzer

Run in development

```
$ yarn dev:bundleanalyzer
```

Run on the production optimized build

```
$ yarn build:bundleanalyzer
```
