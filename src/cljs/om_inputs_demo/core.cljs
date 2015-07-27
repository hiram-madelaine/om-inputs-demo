  (ns ^:figwheel-always om-inputs-demo.core
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [om-inputs.core :as in :refer [make-input-comp]]
            [schema.core :as s]
            [sablono.core :as html :refer-macros [html]]
            [cljs.repl :as repl]
            [cljs.core.async :refer [chan put! >! <! alts!]]))

(enable-console-print!)

(println "Edits to this text should show up in your developer console.")

;; define your app data so that it doesn't get over-written on reload

(defonce app-state (atom {}))


#_(defn demo-comp
  "Diplay a the information of a demo"
  [app owner {:keys [title desc k comp src style] :as opts}]
  (om/component
    (dom/div #js {}
             (dom/div #js {:className (in/styles "demo panel" style)}
              (dom/div #js {:className "panel-heading"}
                       (dom/h1 #js {:className "panel-title"}
                               (dom/label #js {} title)))
              (dom/div #js {:className "demo-body panel-body"}
                       (when desc (dom/pre #js {}
                                           (html desc)))
                       (dom/h4 #js {} "Source : ")
                       (dom/pre #js {}
                                (dom/code #js {:className "clojure"}
                                          src))
                       (dom/h4 #js {} "Display : ")
                       (om/build comp app {:state (om/get-state owner)})

                       (dom/div #js {:className ""}
                                (dom/h4 #js {} "Result : ")
                                (dom/pre #js {}
                                         (dom/code #js {:className "clojure"}
                                                   (print-str (get app k))))))
              )
             (dom/div #js{:className "demo-footer"} ""))))


(defn demo-comp
  "Diplay a the information of a demo"
  [app owner {:keys [title desc k comp src style] :as opts}]
  (om/component
    (dom/div #js {}
             (dom/div #js {:className (str "card-" style)}
                      (dom/div #js {:className "card-header"}
                               (dom/label #js {} title))
                      (dom/div #js {:className "card-body"}
                               (when desc (dom/div #js {:className "well"}
                                                   (html desc)))
                               (dom/h4 #js {} "Source : ")
                               (dom/pre #js {}
                                        (dom/code #js {:className "clojure"}
                                                  src))
                               (dom/h4 #js {} "Display : ")
                               (om/build comp app {:state (om/get-state owner)})

                               (dom/div #js {:className ""}
                                        (dom/h4 #js {} "Result : ")
                                        (dom/pre #js {}
                                                 (dom/code #js {:className "clojure"}
                                                           (print-str (get app k))))))
                      (dom/div #js{:className "card-footer"} "")))))


(def demo-string
  (make-input-comp
    :demo-string
    {:string s/Str}
    (fn [app owner result]
      (om/update! app :demo-1 result))))


(def demo-num
  (make-input-comp
    :demo-num
    {:number s/Num}
    (fn [app owner result]
      (om/update! app :demo-num result))))

(def demo-int
  (make-input-comp
    :demo-int
    {:integer s/Int}
    (fn [app owner result]
      (om/update! app :demo-int result))))


  (def demo-bool
    (make-input-comp
      :demo-bool
      {:boolean s/Bool}
      (fn [app owner result]
        (om/update! app :demo-bool result))
      {:init {:boolean false}}))

  (def demo-num-stepper
    (make-input-comp
      :demo-num-stepper
      {:guests s/Int}
      (fn [app owner result]
        (om/update! app :demo-num-stepper result))
      {:guests {:type  "stepper"}}))

(def demo-num-segmented
  (make-input-comp
    :demo-num-segmented
    {:guests s/Int}
    (fn [app owner result]
      (om/update! app :demo-num-segmented result))
    {:guests {:type  "range-btn-group"
              :attrs {:min 1 :max 8 :step 1}}}))

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


(def action-no-reset
  (make-input-comp
    :action-no-reset
    {:no-reset s/Str}
    (fn [a o v ]
      (om/update! a :action-no-reset v))
    {:action {:no-reset true}}))


(def demo-action-action-one-shot
  (make-input-comp
    :action-one-shot
    {:one-shot s/Str}
    (fn [a o v]
      (om/update! a :action-one-shot v))
    {:action {:one-shot true}}))

(def demo-action-action-resetable
  (make-input-comp
    :action-resetable
    {:resetable s/Str}
    (fn [a o v]
      (om/update! a :action-resetable v))
    (fn [a o]
      (let [clean-chan (om/get-state o :clean-chan)]
        (put! clean-chan :go)))
    {:action {:one-shot true}}))


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

(defn templ-section
  [{:keys [title] :as opts} & body]
  (dom/section #js {}
               (dom/h2 #js {} title)
               (apply dom/div #js {:className "schema-types"} body)))




(om/root
  (fn [app owner]
    (reify
      om/IInitState
      (init-state [_]
        {:lang "en"})
      om/IRenderState
      (render-state [_ state]
        (dom/div #js {:className ""}
                 (templ-section {:title "Basic types"}
                                (om/build demo-comp app {:opts       {:comp  demo-string
                                                                      :src   (with-out-str (repl/source demo-string))
                                                                      :k     :demo-1
                                                                      :title "String"
                                                                      :desc  [:div
                                                                              [:h5 "the function make-input-comp takes 3 mandatory args"]
                                                                              [:ul
                                                                               [:li "A name of the component as a keyword"]
                                                                               [:li "A prismatic Schema"]
                                                                               [:li "An action function"]]]
                                                                      :style "string"}
                                                         :init-state state})
                                (om/build demo-comp app {:opts {:comp  demo-bool
                                                                :src   (with-out-str (repl/source demo-bool))
                                                                :k     :demo-bool
                                                                :title "Boolean"
                                                                :desc  "A boolean is by default represented with a checkbox"
                                                                :style "bool"}})
                                (om/build demo-comp app {:opts       {:comp  demo-num
                                                                      :src   (with-out-str (cljs.repl/source demo-num))
                                                                      :k     :demo-num
                                                                      :title "Number"
                                                                      :desc  "You can only type numeric characters : numbers and ."
                                                                      :style "numeric"}
                                                         :init-state state})
                                (om/build demo-comp app {:opts       {:comp  demo-int
                                                                      :src   (with-out-str (cljs.repl/source demo-int))
                                                                      :k     :demo-int
                                                                      :title "Integer"
                                                                      :desc  "You can only type numeric characters"
                                                                      :style "numeric"}
                                                         :init-state state})
                                (om/build demo-comp app {:opts       {:comp  demo-inst
                                                                      :src   (with-out-str (cljs.repl/source demo-inst))
                                                                      :k     :demo-inst
                                                                      :title "Date"
                                                                      :desc  "With the google Closure DatePicker, the rendering is the same across browsers"
                                                                      :style "inst"}
                                                         :init-state state})
                                (om/build demo-comp app {:opts       {:comp  demo-enum
                                                                      :src   (with-out-str (cljs.repl/source demo-enum))
                                                                      :k     :demo-enum
                                                                      :title "Enum"
                                                                      :desc  "An enum is rendered by default with a select. We know this is not the best fot the UX. You have options to change the display."
                                                                      :style "enum"}
                                                         :init-state state}))

                 (templ-section {:title "UX variation around Integer"}
                                (om/build demo-comp app {:opts       {:comp  demo-num-stepper
                                                                      :src   (with-out-str (cljs.repl/source demo-num-stepper))
                                                                      :k     :demo-num-stepper
                                                                      :title "Stepper for Integer adjustement"
                                                                      :desc  "A numeric field can be displayed as a stepper"
                                                                      :style "numeric"}
                                                         :init-state state})
                                (om/build demo-comp app {:opts       {:comp  demo-num-segmented
                                                                      :src   (with-out-str (cljs.repl/source demo-num-segmented))
                                                                      :k     :demo-num-segmented
                                                                      :title "Segmented control for Integer adjustement"
                                                                      :desc  "A numeric field can be displayed as a stepper"
                                                                      :style "numeric"}
                                                         :init-state state}))
                 (dom/div #js {:className "schema-types"}
                          (om/build demo-comp app {:opts       {:comp  demo-date
                                                                :src   (with-out-str (cljs.repl/source demo-date))
                                                                :k     :demo-date
                                                                :title "A single Inst field handled with native date input"
                                                                :desc  "If you want to use the native chrome date input add the option :type=\"date\""
                                                                :style "inst"
                                                                }
                                                   :init-state state})
                          (om/build demo-comp app {:opts       {:comp  demo-regex
                                                                :src   (with-out-str (cljs.repl/source demo-regex))
                                                                :k     :demo-regex
                                                                :title "It is possible to constrant a String with a Regex"
                                                                :desc  "During typing, the string must conform to the regex."
                                                                :style "string"}
                                                   :init-state state}))
                 (templ-section {:title "UX variations around lists"}
                                (om/build demo-comp app {:opts       {:comp  demo-enum-radio
                                                                      :src   (with-out-str (cljs.repl/source demo-enum-radio))
                                                                      :k     :demo-enum
                                                                      :title "Display an enum as radio liste"
                                                                      :desc  "An enum is displayed by default with a select"
                                                                      :style "enum"}
                                                         :init-state state})
                                (om/build demo-comp app {:opts       {:comp  demo-enum-inline
                                                                      :src   (with-out-str (cljs.repl/source demo-enum-inline))
                                                                      :k     :demo-enum
                                                                      :title "Display an enum as radio inline"
                                                                      :desc  "An enum is displayed by default with a select"
                                                                      :style "enum"}
                                                         :init-state state})
                                (om/build demo-comp app {:opts       {:comp  demo-enum-btn
                                                                      :src   (with-out-str (cljs.repl/source demo-enum-btn))
                                                                      :k     :demo-enum
                                                                      :title "handle en enum"
                                                                      :desc  "An enum is displayed by default with a select"
                                                                      :style "enum"}
                                                         :init-state state}))
                 (om/build demo-comp app {:opts       {:comp  demo-optional
                                                       :src   demo-optional-src
                                                       :k     :demo-optional
                                                       :title "Declare optional data"}
                                          :init-state state})

                 (templ-section
                   {:title "Extra Validations"}
                   (om/build demo-comp app {:opts       {:comp  demo-validation-email
                                                         :src   (with-out-str (cljs.repl/source demo-validation-email))
                                                         :title "Let's see how to declare valiation rule"
                                                         :desc  [:p "The library " [:a "Verily"] " is used"]
                                                         :k     :demo-validation-email}
                                            :init-state state}))
                 (templ-section
                   {:title "Action's options"}
                   (om/build demo-comp app {:opts {:comp action-no-reset
                                                   :src (with-out-str (cljs.repl/source action-no-reset))
                                                   :title "Keep the last posted value"
                                                   :desc "By default the form is reset but with the option :no-reset true
                                                   the last values are kept"
                                                   :k :action-no-reset}})
                   (om/build demo-comp app {:opts       {:comp  demo-action-action-one-shot
                                                         :src   (with-out-str (cljs.repl/source demo-action-action-one-shot))
                                                         :title "This action can be done only once"
                                                         :desc  "with this options {:action {:one-shot true}} the action can be triggered once"
                                                         :k     :action-one-shot}
                                            :init-state state})
                   (om/build demo-comp app {:opts       {:comp  demo-action-action-resetable
                                                         :src   (with-out-str (cljs.repl/source demo-action-action-resetable))
                                                         :title "This action can be done only once"
                                                         :desc  "with this options {:action {:one-shot true}} the action can be triggered once"
                                                         :k     :action-resetable}
                                            :init-state state}))
                 #_(dom/div #js {:className "card-string"}
                          (dom/div #js {:className "card-header"}
                                   (dom/label nil "title"))
                          (dom/div #js {:className "card-body"} "body")
                          (dom/div #js {:className "card-footer"} ""))))))
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

