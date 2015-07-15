  (ns ^:figwheel-always om-inputs-demo.core
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [om-inputs.core :as in :refer [make-input-comp]]
            [schema.core :as s]
            [sablono.core :as html :refer-macros [html]]
            [cljs.repl :as repl]))

(enable-console-print!)

(println "Edits to this text should show up in your developer console.")

;; define your app data so that it doesn't get over-written on reload

(defonce app-state (atom {}))


(defn demo-comp
  "Diplay a the information of a demo"
  [app owner {:keys [title desc k comp src] :as opts}]
  (om/component
    (dom/div #js {:className "demo panel panel-primary"}
             (dom/div #js {:className "panel-heading"}
                      (dom/h1 #js {:className "panel-title"} title))
             (dom/div #js {:className "demo-body panel-body"}
                      (when desc (dom/pre #js {}
                                          (html desc)))
                      (dom/h4 #js {} "Source : ")
                      (dom/pre #js {}
                               (dom/code #js {:className "clojure"}
                                         src))
                      (dom/h4 #js {} "Display : ")
                      (om/build comp app {:state (om/get-state owner)})
                      (dom/h4 #js {} "Result : ")
                      (dom/pre #js {}
                               (dom/code #js {:className "clojure"}
                                         (print-str (get app k))))))))

(def demo-string
  (make-input-comp
    :demo-string
    {:name s/Str}
    (fn [app owner result]
      (om/update! app :demo-1 result))))


(def demo-num
  (make-input-comp
    :demo-num
    {:number s/Num}
    (fn [app owner result]
      (om/update! app :demo-num result))))

  (def demo-num-stepper
    (make-input-comp
      :demo-num-stepper
      {:number s/Num}
      (fn [app owner result]
        (om/update! app :demo-num-stepper result))
      {:number {:type  "stepper"}}))

(def demo-inst
  (make-input-comp
    :demo-inst
    {:Inst s/Inst}
    (fn [app owner result]
      (om/update! app :demo-inst result))))

(def demo-date
  (make-input-comp
    :demo-date
    {:date s/Inst}
    (fn [app owner result]
      (om/update! app :demo-date result))
    {:date {:type "date"}}))

(def demo-enum
  (make-input-comp
    :demo-enum
    {:langage (s/enum "Clojure"
                      "clojureScript"
                      "ClojureCLR")}
    (fn [app owner result]
      (om/update! app :demo-enum result))))

(def demo-enum-radio
  (make-input-comp
    :demo-enum
    {:langage (s/enum "Clojure"
                      "clojureScript"
                      "ClojureCLR")}
    (fn [app owner result]
      (om/update! app :demo-enum result))
    {:langage {:type "radio-group"}}))

(def demo-enum-inline
  (make-input-comp
    :demo-enum
    {:langage (s/enum "Clojure"
                      "clojureScript"
                      "ClojureCLR")}
    (fn [app owner result]
      (om/update! app :demo-enum result))
    {:langage {:type "radio-group-inline"}}))

(def demo-enum-btn
  (make-input-comp
    :demo-enum
    {:langage (s/enum "Clojure"
                      "clojureScript"
                      "ClojureCLR")}
    (fn [app owner result]
      (om/update! app :demo-enum result))
    {:langage {:type "btn-group"}}))

  (def demo-optional
    (make-input-comp
      :demo-optional
      {:email  s/Str
       (s/optional-key :name) s/Str}
      (fn [a o v]
        (om/update! a :demo-optional v))))

  (def demo-optional-src
    (with-out-str (cljs.repl/source demo-optional)))



  (def demo-validation-email
    (make-input-comp
      :demo-validation-email
      {:email s/Str}
      (fn [a o v]
        (om/update! a :demo-validation-email v))
      {:validations
       [[:email [:email] :bad-email]]}))


  (def demo-help
    (make-input-comp
      :demo-help
      {:email s/Str}
      (fn [a o v]
        (om/update! a :demo-help v))
      {:email {:desc "Your email"
               :ph "name@org.com"
               }}))



(defn action
  [k]
  (fn
    [app owner result]
    (om/update! app k result)))

(def demo-regex
  (make-input-comp
    :demo-regex
    {:regex #"^[A-Z]{0,2}[0-9]{0,12}$"}
    (action :demo-regex)))



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
                  (om/build demo-comp app {:opts       {:comp  demo-string
                                                        :src   (with-out-str (repl/source demo-string))
                                                        :k     :demo-1
                                                        :title "A single field of type String"
                                                        :desc  [:div
                                                                [:p.hyphenate "the function make-input-comp takes 3 mandatory args :"]
                                                                [:ul
                                                                 [:li "A name of the component as a keyword"]
                                                                 [:li "A prismatic Schema"]
                                                                 [:li "An action function"]]]}
                                           :init-state state})
                  (om/build demo-comp app {:opts       {:comp  demo-num
                                                        :src   (with-out-str (cljs.repl/source demo-num))
                                                        :k     :demo-num
                                                        :title "A single field of type Numeric"
                                                        :desc  "You can only type numeric characters"}
                                           :init-state state})
                   (om/build demo-comp app {:opts       {:comp  demo-num-stepper
                                                         :src   (with-out-str (cljs.repl/source demo-num-stepper))
                                                         :k     :demo-num-stepper
                                                         :title "A single field of type Numeric"
                                                         :desc  ""}
                                            :init-state state})

                   (om/build demo-comp app {:opts       {:comp  demo-inst
                                                         :src   (with-out-str (cljs.repl/source demo-inst))
                                                         :k     :demo-inst
                                                         :title "A single Inst field"
                                                         :desc  "With the google Closure Calendar the rendering is the same across browsers"}
                                            :init-state state})
                   (om/build demo-comp app {:opts       {:comp  demo-date
                                                         :src   (with-out-str (cljs.repl/source demo-date))
                                                         :k     :demo-date
                                                         :title "A single Inst field handled with native date input"
                                                         :desc  "If you want to use the native chrome date input add the option :type=\"date\""}
                                            :init-state state})
                   (om/build demo-comp app {:opts       {:comp  demo-regex
                                                         :src   (with-out-str (cljs.repl/source demo-regex))
                                                         :k     :demo-regex
                                                         :title "It is possible to constrant a String with a Regex"
                                                         :desc  "During typing, the string must conform to the regex."}
                                            :init-state state})
                  (om/build demo-comp app {:opts       {:comp  demo-enum
                                                        :src   (with-out-str (cljs.repl/source demo-enum))
                                                        :k     :demo-enum
                                                        :title "handle en enum"
                                                        :desc  "An enum is displayed by default with a select"}
                                           :init-state state})
                  (om/build demo-comp app {:opts       {:comp  demo-enum-radio
                                                        :src   (with-out-str (cljs.repl/source demo-enum-radio))
                                                        :k     :demo-enum
                                                        :title "handle en enum"
                                                        :desc  "An enum is displayed by default with a select"}
                                           :init-state state})
                   (om/build demo-comp app {:opts       {:comp  demo-enum-inline
                                                         :src   (with-out-str (cljs.repl/source demo-enum-inline))
                                                         :k     :demo-enum
                                                         :title "handle en enum"
                                                         :desc  "An enum is displayed by default with a select"}
                                            :init-state state})
                   (om/build demo-comp app {:opts       {:comp  demo-enum-btn
                                                         :src   (with-out-str (cljs.repl/source demo-enum-btn))
                                                         :k     :demo-enum
                                                         :title "handle en enum"
                                                         :desc  "An enum is displayed by default with a select"}
                                            :init-state state})
                   (om/build demo-comp app {:opts {:comp demo-optional
                                                   :src demo-optional-src
                                                   :k :demo-optional
                                                   :title "Declare optional data"}
                                            :init-state state})

                  (om/build demo-comp app {:opts       {:comp  demo-validation-email
                                                        :src   (with-out-str (cljs.repl/source demo-validation-email))
                                                        :title "Let's see how to declare valiation rule"
                                                        :desc  [:p "The library " [:a "Verily"] " is used"]
                                                        :k     :demo-validation-email}
                                           :init-state state}))))))
  app-state
  {:target (. js/document (getElementById "app"))
   :shared {:i18n {"fr" {:errors {:mandatory "Cette donnée est obligatoire"
                                  :bad-email "Invalid email"}}
                   "en" {:errors {:mandatory "This information is mandatory"
                                  :bad-email "Invalid email"}}}}})


(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
) 

