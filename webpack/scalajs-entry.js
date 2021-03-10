if (process.env.NODE_ENV === "production") {
  const opt = require("./epub-images-viewer-opt.js");
  opt.main();
  module.exports = opt;
} else {
  window.require = require("./epub-images-viewer-fastopt-entrypoint.js").require;
  window.global = window;

  const fastOpt = require("./epub-images-viewer-fastopt.js");
  fastOpt.main()
  module.exports = fastOpt;

  if (module.hot) {
    module.hot.accept();
  }
}
