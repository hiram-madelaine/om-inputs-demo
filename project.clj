(defproject om-inputs-demo "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.122"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [sablono "0.3.4"]
                 [org.omcljs/om "0.8.8"]
                 [om-inputs "0.3.9"]
                 [cljsjs/codemirror "5.6.0-0"]
                 [com.cognitect/transit-clj "0.8.275"]
                 [com.cognitect/transit-cljs "0.8.225"]]

  :plugins [[lein-cljsbuild "1.1.0"]
            [lein-figwheel "0.3.8"]]

  :source-paths ["src"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]
  
  :cljsbuild {
              :builds [{:id           "dev"
                        :source-paths ["src"]

                        :figwheel     {:on-jsload "om-inputs-demo.core/on-js-reload"}

                        :compiler     {:main                 om-inputs-demo.core
                                       :asset-path           "js/compiled/out"
                                       :output-to            "resources/public/js/compiled/om_inputs_demo.js"
                                       :output-dir           "resources/public/js/compiled/out"
                                       :source-map-timestamp true
                                       :dump-core            false
                                       :foreign-libs
                                                             [{:provides ["cljsjs.codemirror.addons.closebrackets"]
                                                               :requires ["cljsjs.codemirror"]
                                                               :file     "resources/public/js/codemirror/closebrackets.js"}
                                                              {:provides ["cljsjs.codemirror.addons.matchbrackets"]
                                                               :requires ["cljsjs.codemirror"]
                                                               :file     "resources/public/js/codemirror/matchbrackets.js"}]}}
                       {:id           "min"
                        :source-paths ["src/cljs"]
                        :compiler     {:optimizations :simple
                                       :pretty-print false
                                       :dump-core false
                                       :static-fns true
                                       :optimize-constants true
                                       :verbose true
                                       :output-to          "resources/public/js/compiled/om_inputs_demo.js"
                                       :output-dir           "resources/public/js/compiled/out"
                                       :asset-path         "js/compiled/out"
                                       :foreign-libs       [{:provides ["cljsjs.codemirror.addons.closebrackets"]
                                                             :requires ["cljsjs.codemirror"]
                                                             :file     "resources/public/js/codemirror/closebrackets.js"}
                                                            {:provides ["cljsjs.codemirror.addons.matchbrackets"]
                                                             :requires ["cljsjs.codemirror"]
                                                             :file     "resources/public/js/codemirror/matchbrackets.js"}]}}]}

  :figwheel {
             ;; :http-server-root "public" ;; default and assumes "resources" 
             ;; :server-port 3449 ;; default
             :css-dirs ["resources/public/css"] ;; watch and update CSS

             ;; Start an nREPL server into the running figwheel process
             ;; :nrepl-port 7888

             ;; Server Ring Handler (optional)
             ;; if you want to embed a ring handler into the figwheel http-kit
             ;; server, this is for simple ring servers, if this
             ;; doesn't work for you just run your own server :)
             ;; :ring-handler hello_world.server/handler

             ;; To be able to open files in your editor from the heads up display
             ;; you will need to put a script on your path.
             ;; that script will have to take a file path and a line number
             ;; ie. in  ~/bin/myfile-opener
             ;; #! /bin/sh
             ;; emacsclient -n +$2 $1
             ;;
             ;; :open-file-command "myfile-opener"

             ;; if you want to disable the REPL
             ;; :repl false

             ;; to configure a different figwheel logfile path
             ;; :server-logfile "tmp/logs/figwheel-logfile.log" 
             })
