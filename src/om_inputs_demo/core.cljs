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
                               (dom/h1 #js {:className "panel-title"} title))
                      (dom/div #js {:className "panel-body"} (dom/pre #js {}
                                               desc)
                               (dom/h4 #js {} "Source : ")
                               (dom/pre #js {}
                                        (dom/code #js {:className "clojure"}
                                                  src))
                               (dom/h4 #js {} "Display : ")
                               (om/build comp app {:state (om/get-state owner)})
                               (dom/h4 #js {} "Result : ")
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

(def demo-inst
  (make-input-comp
    :demo-inst
    {:Inst s/Inst}
    (fn [app owner result]
      (om/update! app :demo-inst result))))

(def demo-3
  (make-input-comp
    :demo-3
    {:langage (s/enum "Clojure"
                      "clojureScript"
                      "ClojureCLR")}
    (fn [app owner result]
      (om/update! app :demo-3 result))))

(def demo-3'
  (make-input-comp
    :demo-3
    {:langage (s/enum "Clojure"
                      "clojureScript"
                      "ClojureCLR")}
    (fn [app owner result]
      (om/update! app :demo-3 result))
    {:langage {:type "radio-group"}}))

(def demo-3''
  (make-input-comp
    :demo-3
    {:langage (s/enum "Clojure"
                      "clojureScript"
                      "ClojureCLR")}
    (fn [app owner result]
      (om/update! app :demo-3 result))
    {:langage {:type "radio-group-inline"}}))

(def demo-3'''
  (make-input-comp
    :demo-3
    {:langage (s/enum "Clojure"
                      "clojureScript"
                      "ClojureCLR")}
    (fn [app owner result]
      (om/update! app :demo-3 result))
    {:langage {:type "btn-group"}}))


(defn action
  [k]
  (fn
    [app owner result]
    (om/update! app k result)))

(def demo-4
  (make-input-comp
    :demo-4
    {:regex #"^[A-Z]{0,2}[0-9]{0,12}$"}
    (action :demo-4)))


(om/root
  (fn [app owner]
    (reify
      om/IInitState
      (init-state [_]
        {:lang "en"})
      om/IRenderState
      (render-state [_ state]
        (dom/div #js {:className ""}
          (dom/div #js {:id "schema-types"}
                  (om/build demo-comp app {:opts       {:comp  demo-1
                                                        :src   (with-out-str (cljs.repl/source demo-1))
                                                        :k     :demo-1
                                                        :title "A single field of type String"
                                                        :desc  "Define a schema with "
                                                        }
                                           :init-state state})
                  (om/build demo-comp app {:opts       {:comp  demo-2
                                                        :src   (with-out-str (cljs.repl/source demo-2))
                                                        :k     :demo-2
                                                        :title "A single field of type Numeric"
                                                        :desc  ""}
                                           :init-state state})
                   (om/build demo-comp app {:opts       {:comp  demo-inst
                                                         :src   (with-out-str (cljs.repl/source demo-inst))
                                                         :k     :demo-inst
                                                         :title "A single Inst field"
                                                         :desc  ""}
                                            :init-state state})
                   (om/build demo-comp app {:opts       {:comp  demo-4
                                                         :src   (with-out-str (cljs.repl/source demo-4))
                                                         :k     :demo-4
                                                         :title "ANd now a Regex"
                                                         :desc  "The typing is controled"}
                                            :init-state state})
                  (om/build demo-comp app {:opts       {:comp  demo-3
                                                        :src   (with-out-str (cljs.repl/source demo-3))
                                                        :k     :demo-3
                                                        :title "handle en enum"
                                                        :desc  "An enum is displayed by default with a select"}
                                           :init-state state})
                  (om/build demo-comp app {:opts       {:comp  demo-3'
                                                        :src   (with-out-str (cljs.repl/source demo-3'))
                                                        :k     :demo-3
                                                        :title "handle en enum"
                                                        :desc  "An enum is displayed by default with a select"}
                                           :init-state state})
                   (om/build demo-comp app {:opts       {:comp  demo-3''
                                                         :src   (with-out-str (cljs.repl/source demo-3''))
                                                         :k     :demo-3
                                                         :title "handle en enum"
                                                         :desc  "An enum is displayed by default with a select"}
                                            :init-state state})
                   (om/build demo-comp app {:opts       {:comp  demo-3'''
                                                         :src   (with-out-str (cljs.repl/source demo-3'''))
                                                         :k     :demo-3
                                                         :title "handle en enum"
                                                         :desc  "An enum is displayed by default with a select"}
                                            :init-state state})

                  )))))
  app-state
  {:target (. js/document (getElementById "app"))
   :shared {:i18n {"fr" {:errors {:mandatory "Cette donnée est obligatoire"}}
                   "en" {:errors {:mandatory "This information is mandatory"}}}}})


(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
) 

