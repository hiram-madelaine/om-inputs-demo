(ns ^:figwheel-always om-inputs-demo.core
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [om-inputs.core :as in :refer [make-input-comp]]
            [schema.core :as s]
            ))

(enable-console-print!)

(println "Edits to this text should show up in your developer console.")

;; define your app data so that it doesn't get over-written on reload

(defonce app-state (atom {:text "Hello world!"}))


(defn demo-comp
  "Diplay a the information of a demo"
  [app owner {:keys [title desc k comp src] :as opts}]
  (om/component
    (dom/div #js {:className "demo"}
             (dom/div #js {:className "panel panel-default"}
                      (dom/div #js {:className "panel-heading"}
                               (dom/h3 #js {:className "panel-title"} title))
                      (dom/div #js {:className "panel-body"} (dom/pre #js {}
                                               desc)
                               (dom/pre #js {}
                                        (dom/code #js {:className "clojure"}
                                                  src))
                               (om/build comp app)
                               (dom/label #js {} "Result")
                               (dom/pre #js {}
                                        (dom/code #js {:className "clojure"}
                                                  (print-str (get app k)))))))))

(def demo-1
  (make-input-comp
    :demo1
    {:name s/Str}
    (fn [app owner result]
      (om/update! app :demo-1 result))))


(def demo-2
  (make-input-comp
    :demo-2
    {:number s/Num}
    (fn [app owner result]
      (om/update! app :demo-2 result))))

(def demo-3
  (make-input-comp
    :demo-3
    {:langage (s/enum "Clojure"
                      "clojureScript"
                      "ClojureCLR")}
    (fn [app owner result]
      (om/update! app :demo-3 result))))



(om/root
  (fn [app owner]
    (reify om/IRender
      (render [_]
        (dom/div #js {:className "container"}
                 (om/build demo-comp app {:opts {:comp  demo-1
                                                 :src   (with-out-str (cljs.repl/source demo-2))
                                                 :k     :demo-1
                                                 :title "The simplest example"
                                                 :desc  "Define a schema with "}})
                 (om/build demo-comp app {:opts {:comp demo-2
                                                 :src (with-out-str (cljs.repl/source demo-2))
                                                 :k :demo-2
                                                 :title "Numeric field"
                                                 :desc ""}})
                 (om/build demo-comp app {:opts {:comp demo-3
                                                 :src (with-out-str (cljs.repl/source demo-3))
                                                 :k :demo-3
                                                 :title "handle en enum"
                                                 :desc "An enum is displayed by default with a select"}})
          ))))
  app-state
  {:target (. js/document (getElementById "app"))})


(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
) 

