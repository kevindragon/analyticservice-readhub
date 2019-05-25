(defproject analyticservice-readhub "0.1.0"
  :description "Interview exercise"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.jsoup/jsoup "1.12.1"]
                 [cheshire "5.8.1"]
                 [clj-http "3.10.0"]
                 [ring "1.7.1"]
                 [compojure "1.6.1"]
                 [ring/ring-defaults "0.3.2"]
                 [ring/ring-json "0.4.0"]]
  :main ^:skip-aot asrh.core
  :target-path "target/%s"
  :plugins [[lein-cljsbuild "1.1.7"]
            [lein-figwheel "0.5.16"]]
  :profiles
  {:provided
   {:dependencies
    [[org.clojure/clojurescript "1.10.439"]
     [com.google.protobuf/protobuf-java "3.6.1"]
     [cljsjs/moment "2.22.2-2"]
     [reagent "0.8.1"]
     [cljs-ajax "0.7.5"]]}
   :uberjar {:aot :all
             :prep-tasks [["cljsbuild" "once" "min"] ["compile"]]
             :clean-targets ^{:protect false} ["target" "resources/public/js"]}
   :dev {:dependencies [[cider/piggieback "0.4.0"]
                        [figwheel-sidecar "0.5.17"]]
         :repl-options {:nrepl-middleware [cider.piggieback/wrap-cljs-repl]}}}
  :cljsbuild
  {:builds [{:id "min"
             :source-paths ["src-cljs"]
             :compiler {:main asrh.core
                        :output-to "resources/public/js/app.js"
                        ;; :output-dir "resources/js/libs"
                        :optimizations :advanced}}
            {:id "dev"
             :source-paths ["src-cljs"]
             :figwheel {:on-jsload "asrh.core/run"}
             :compiler {:main asrh.core
                        :asset-path "js/out"
                        :output-to "resources/public/js/app.js"
                        :output-dir "resources/public/js/out"
                        :optimizations :none
                        :source-map true
                        :source-map-timestamp true}}]}
  :figwheel {:css-dirs ["resources/public/css"]})
