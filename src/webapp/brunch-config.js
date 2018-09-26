module.exports = {
    config: {
        server: {
            port: 3000,
            hostname: "172.16.0.223"
        },
        paths: {
            watched: ["src", "spec"]
        },
        files: {
            javascripts: {joinTo: "javascripts/application.js"},
            stylesheets: {joinTo: "stylesheets/application.css"}
        },
        plugins: {
            sass: {
                mode: 'native'
            },
            elmBrunch: {
                makeParameters: [],
                mainModules: ['src/elm/Main.elm'],
                outputFolder: "public/javascripts/",
                outputFile: "elm.js"
            }
        }
    }
}
