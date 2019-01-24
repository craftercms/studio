# Video Center Blueprint

Live, live-to-VOD, and video serving blueprint on Crafter CMS.

## Installation

- Created using:
  - [Create React App](https://github.com/facebookincubator/create-react-app).

1. Install [yarn](https://yarnpkg.com/en/)
2. Run `yarn`


## Available Scripts

In the project directory, you can run:

### `yarn start`

Runs the app in the development mode.<br>
Open [http://localhost:3000](http://localhost:3000) to view it in the browser.

The page will reload if you make edits.<br>
You will also see any lint errors in the console.

### `yarn build`

Builds the app for production to the site `static-assets` folder.<br>
It correctly bundles React in production mode and optimizes the build for the best performance.

The build is minified and the filenames include the hashes.<br>

## Site Configuration

### React Dev Server

1. Configure the `previewServer` property in the site configuration file, this can be done
   from Studio or changing directly `/config/studio/site-config.xml`:
   ```
   <previewServer>http://localhost:3000</previewServer>
   ```

2. Configure the dev server to proxy API calls to Engine, in `package.json` add the following configuration:
  ```
  "proxy": {
    "/api": {
      "target": "http://localhost:8080"
    },
    "/static-assets": {
      "target": "http://localhost:8080"
    }
  }
  ```

Using this configuration Crafter Studio preview will show the react dev server and it will support
hot reload when the js sources are changed.

To use the dev server directly without Studio you will need to set a cookie with the site name:

```
crafterSite={siteName}
```


*__TBD__: Add docs on how to configure Box & AWS profiles.*
