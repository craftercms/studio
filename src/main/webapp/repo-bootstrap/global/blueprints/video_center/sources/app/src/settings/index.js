const common = {
  repoUrl: "https://github.com/craftercms/video-center-blueprint"
}

const studioConfig = {
  baseUrl: "http://localhost:8080",
  site: window.siteName ? window.siteName : "video-center",
  navTreeBase: "/site/website"
}

const themeConfig = {
  theme: 'themedefault'
};

export {
  common,
  studioConfig,
  themeConfig
};
