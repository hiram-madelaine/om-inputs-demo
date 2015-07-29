(ns ^:figwheel-always om-inputs-demo.core
  (:require-macros [cljs.core.async.macros :as m :refer [go alt!]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [om-inputs.core :as in :refer [make-input-comp]]
            [schema.core :as s]
            [sablono.core :refer-macros [html]]
            [cljs.repl :as repl]
            [cljs.core.async :refer [chan put! >! <! alts! timeout]]))

(enable-console-print!)

;________________________________________________
;                                                |
;         The usual suspects                     |
;                                                |
;________________________________________________|


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

  (def demo-inst
    (make-input-comp
      :demo-inst
      {:Inst s/Inst}
      (fn [app owner result]
        (om/update! app :demo-inst result))))

  (def demo-enum
    (make-input-comp
      :demo-enum
      {:langage (s/enum "Clojure"
                        "clojureScript"
                        "ClojureCLR")}
      (fn [app owner result]
        (om/update! app :demo-enum result))))
;________________________________________________
;                                                |
;         Variation around Numbers               |
;                                                |
;________________________________________________|

  (def demo-num-stepper
    (make-input-comp
      :demo-num-stepper
      {:guests s/Int}
      (fn [app owner result]
        (om/update! app :demo-num-stepper result))
      {:guests {:type  "stepper"}
       :init {:guests 1}}))

(def demo-num-segmented
  (make-input-comp
    :demo-num-segmented
    {:guests s/Int}
    (fn [app owner result]
      (om/update! app :demo-num-segmented result))
    {:guests {:type  "range-btn-group"
              :attrs {:min 1 :max 8 :step 1}}}))


;________________________________________________
;                                                |
;         Native Date Picker                     |
;                                                |
;________________________________________________|

(def demo-date
  (make-input-comp
    :demo-date
    {:date s/Inst}
    (fn [app owner result]
      (om/update! app :demo-date result))
    {:date {:type "date"}}))


;________________________________________________
;                                                |
;         Variation around Lists                 |
;                                                |
;________________________________________________|


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

;________________________________________________
;                                                |
;         Opitonal                               |
;                                                |
;________________________________________________|


  (def demo-optional-field
    (make-input-comp
      :demo-optional-field
      {:email  s/Str
       (s/optional-key :name) s/Str}
      (fn [a o v]
        (om/update! a :demo-optional-field v))))

(def demo-optional-value
  (make-input-comp
    :demo-optional-value
    {:email s/Str
     :name  (s/maybe s/Str)}
    (fn [a o v]
      (om/update! a :demo-optional-value v))))


;________________________________________________
;                                                |
;         Variation around Actions               |
;                                                |
;________________________________________________|

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
      (prn "Let's create an other item !"))
    {:action {:one-shot true}}))



;________________________________________________
;                                                |
;         Asyn Actions                           |
;                                                |
;________________________________________________|


  (def async-action
    (make-input-comp
      :async-action
      {:async-action s/Str}
      (fn [a o v c]
        (go
          (<! (timeout 1000))
          (om/update! a :async-action v)
          (>! c [:ok]))
        )
      {:action {:async true}}))


(def async-action-error
  (make-input-comp
    :async-action-error
    {:async-action-error s/Str}
    (fn [a o v c]
      (go
        (<! (timeout 1000))
        (>! c [:ko]))
      )
    {:action {:async true}}))


;________________________________________________
;                                                |
;         Validations                            |
;                                                |
;________________________________________________|

  (def demo-validation-email
    (make-input-comp
      :demo-validation-email
      {:email s/Str}
      (fn [a o v]
        (om/update! a :demo-validation-email v))
      {:validations
       [[:email [:email] :bad-email]]}))


;________________________________________________
;                                                |
;         Validations                            |
;                                                |
;________________________________________________|


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


;; define your app data so that it doesn't get over-written on reload

(def app-state (atom [{:title   "Basic types : The usual suspects"
                           :id      "usual-suspects"
                           :content [{:comp  demo-string
                                      :src   (with-out-str (repl/source demo-string))
                                      :k     :demo-1
                                      :title "String"
                                      :desc  (html [:div
                                                    [:h5 "the function make-input-comp takes 3 mandatory args"]
                                                    [:ul
                                                     [:li "A name of the component as a keyword"]
                                                     [:li "A prismatic Schema"]
                                                     [:li "An action function"]]])
                                      :style "string"}
                                     {:comp  demo-bool
                                      :src   (with-out-str (repl/source demo-bool))
                                      :k     :demo-bool
                                      :title "Boolean"
                                      :desc  "A boolean is by default represented with a checkbox"
                                      :style "bool"}
                                     {:comp  demo-num
                                      :src   (with-out-str (cljs.repl/source demo-num))
                                      :k     :demo-num
                                      :title "Number"
                                      :desc  "You can only type numeric characters : numbers and ."
                                      :style "numeric"}
                                     {:comp  demo-int
                                      :src   (with-out-str (cljs.repl/source demo-int))
                                      :k     :demo-int
                                      :title "Integer"
                                      :desc  "You can only type numeric characters"
                                      :style "numeric"}
                                     {:comp  demo-inst
                                      :src   (with-out-str (cljs.repl/source demo-inst))
                                      :k     :demo-inst
                                      :title "Date"
                                      :desc  "With the google Closure DatePicker, the rendering is the same across browsers"
                                      :style "inst"}
                                     {:comp  demo-enum
                                      :src   (with-out-str (cljs.repl/source demo-enum))
                                      :k     :demo-enum
                                      :title "Enum"
                                      :desc  "An enum is rendered by default with a select. We know this is not the best fot the UX. You have options to change the display."
                                      :style "enum"}
                                     ]}
                          {:title "Optional data"
                           :id "optional-data"
                           :content [{:comp  demo-optional-field
                                      :src   (with-out-str (cljs.repl/source demo-optional-field))
                                      :k     :demo-optional-field
                                      :desc "A field is marked optional with the schema (schema/optional-key)"
                                      :title "Optional field"}
                                     {:comp  demo-optional-value
                                      :src   (with-out-str (cljs.repl/source demo-optional-value))
                                      :k     :demo-optional-value
                                      :desc "If you want the key to be present mark the value as maybe"
                                      :title "Optional value"}]}
                          {:title "UX variation around Integer"
                           :id    "integer-variations"
                           :content [{:comp  demo-num-stepper
                                      :src   (with-out-str (cljs.repl/source demo-num-stepper))
                                      :k     :demo-num-stepper
                                      :title "Stepper for Integer adjustement"
                                      :desc  "A numeric field can be displayed as a stepper"
                                      :style "numeric"}
                                     {:comp  demo-num-segmented
                                      :src   (with-out-str (cljs.repl/source demo-num-segmented))
                                      :k     :demo-num-segmented
                                      :title "Segmented control for Integer adjustement"
                                      :desc  "A numeric field can be displayed as a stepper"
                                      :style "numeric"}]}
                          {:title "UX Around Date Picker"
                           :id "date-options"
                           :content [{:comp  demo-date
                                      :src   (with-out-str (cljs.repl/source demo-date))
                                      :k     :demo-date
                                      :title "A single Inst field handled with native date input"
                                      :desc  "If you want to use the native chrome date input add the option :type=\"date\""
                                      :style "inst"
                                      }]}
                          {:title "UX variations around lists"
                           :id    "lists-variations"
                           :content [{:comp  demo-enum-radio
                                      :src   (with-out-str (cljs.repl/source demo-enum-radio))
                                      :k     :demo-enum
                                      :title "Display an enum as radio liste"
                                      :desc  "An enum is displayed by default with a select."
                                      :style "enum"}
                                     {:comp  demo-enum-inline
                                      :src   (with-out-str (cljs.repl/source demo-enum-inline))
                                      :k     :demo-enum
                                      :title "Display an enum as radio inline"
                                      :desc  "An enum is displayed by default with a select"
                                      :style "enum"}
                                     {:comp  demo-enum-btn
                                      :src   (with-out-str (cljs.repl/source demo-enum-btn))
                                      :k     :demo-enum
                                      :title "handle en enum"
                                      :desc  "An enum is displayed by default with a select"
                                      :style "enum"}]}
                          {:title "Constraint what can be typed"
                           :id "constraint-typing"
                           :content [{:comp  demo-regex
                                      :src   (with-out-str (cljs.repl/source demo-regex))
                                      :k     :demo-regex
                                      :title "Assist the keyboard input of a String using a Regex"
                                      :desc  "During typing, the string must conform to the regex.
                                                                Because you are using regex you now have an other problem..."
                                      :style "string"}]}
                          {:title "Help you users with information"
                           :id    "help-users"
                           :content [{:comp  demo-help
                                      :title "Add help information"
                                      :src   (with-out-str (cljs.repl/source demo-help))
                                      :k     :demo-comp
                                      :style "string"}]}
                      {:title   "Extra Validations"
                       :id      "extra-validations"
                       :content [{:comp  demo-validation-email
                                  :src   (with-out-str (cljs.repl/source demo-validation-email))
                                  :title "Let's see how to declare valiation rule"
                                  :desc  (html [:p "The library " [:a "Verily"] " is used"])
                                  :k     :demo-validation-email}]}
                          {:title "Action's options"
                           :id    "action-options"
                           :content [ {:comp  action-no-reset
                                       :src   (with-out-str (cljs.repl/source action-no-reset))
                                       :title "Keep the last posted value"
                                       :desc  "By default the form is reset but with the option :no-reset true
                                                   the last values are kept"
                                       :k     :action-no-reset
                                       :style "action-dark"}
                                     {:comp  demo-action-action-one-shot
                                      :src   (with-out-str (cljs.repl/source demo-action-action-one-shot))
                                      :title "This action can be done only once"
                                      :desc  "with this options {:action {:one-shot true}} the action can be triggered once
                                                          \"Clean\" button has no action and can be hidden with CSS"
                                      :k     :action-one-shot
                                      :style "action"}
                                     {:comp  demo-action-action-resetable
                                      :src   (with-out-str (cljs.repl/source demo-action-action-resetable))
                                      :title "Cycle between action & clean"
                                      :desc  "with this options {:action {:one-shot true}} the action can be triggered once
                                                         but as a clean action is provided then the form can be cleaned and resubmitted again."
                                      :k     :action-resetable
                                      :style "action-light"}]}
                          {:title "Asynchronous actions"
                           :id    "asynchronous-actions"
                           :content [ {:comp  async-action
                                       :src   (with-out-str (repl/source async-action))
                                       :title "Action can be asynchronous"
                                       :desc  "When the action is asynchronous the action fn has an extra parameter
                                                    : a channel. You must use it to indicate if the result of the operation is succesful or not.
                                                     Respectively with [:ok] or failed with [:ko error]"
                                       :k     :async-action
                                       :style "action"}
                                     {:comp  async-action-error
                                      :src   (with-out-str (repl/source async-action-error))
                                      :title "Asynchronous action and errors"
                                      :desc  "When an error occurs put [:ko] in the channel.
                                      In this example the result will always be nil"
                                      :k     :async-action-error
                                      :style "action"}]}]))


;________________________________________________
;                                                |
;         Navigation components                  |
;                                                |
;________________________________________________|

  (defn nav-entry-view
    [{:keys [title id]} owner]
    (om/component
      (html [:a {:href (str "#" id) :class "list-group-item"} title])))


  (defn navigation-view
    [app owner]
    (om/component
      (dom/div #js {:id "navigation"}
               (html
                 [:div {:id "index"}
                  [:div {:class "list-group"}
                   (om/build-all nav-entry-view app)]]))))



;________________________________________________
;                                                |
;         Content components                     |
;                                                |
;________________________________________________|

  (defn demo-view
    [{:keys [title desc k comp src style] :as demo} owner]
    (reify
      om/IRenderState
      (render-state [_ state]
        (dom/div #js {}
          (dom/div #js {:className (str "card-" style)}
                  (dom/div #js {:className "card-header"}
                           (dom/label #js {} title))
                  (dom/div #js {:className "card-body"}
                           (when desc (dom/div #js {:className "well"}
                                               desc))
                           (dom/h4 #js {} "Source : ")
                           (dom/pre #js {}
                                    (dom/code #js {:className "clojure"}
                                              src))
                           (dom/h4 #js {} "Display : ")
                           (om/build comp demo {:state (om/get-state owner)})

                           (dom/div #js {:className ""}
                                    (dom/h4 #js {} "Result : ")
                                    (dom/pre #js {}
                                             (dom/code #js {:className "clojure"}
                                                       (print-str (get demo k))))))
                  (dom/div #js{:className "card-footer"} ""))))))

  (defn section-view
    [{:keys [title id] :as section} owner]
    (reify
      om/IRenderState
      (render-state [_ state]
        (dom/section #js {:id id}
                     (dom/h2 #js {:className "section-title"} title)
                     (apply dom/div #js {:className "schema-types"}
                            (om/build-all demo-view (:content section) {:init-state state}))))))

(defn content-view
    [app owner]
    (reify
      om/IRenderState
      (render-state
        [_ state]
        (apply dom/div #js {:id "content"}
                 (om/build-all section-view app {:init-state state})))))



;________________________________________________
;                                                |
;         app component                          |
;                                                |
;________________________________________________|
(om/root
  (fn [app owner]
    (reify
      om/IInitState
      (init-state [_]
        {:lang "en"})
      om/IRenderState
      (render-state [_ state]
        (dom/div #js {:id "main"}
                 (om/build navigation-view app)
                 (om/build content-view app)))))
  app-state
  {:target (. js/document (getElementById "app"))
   :shared {:i18n {"fr" {:errors {:mandatory "Cette donnée est obligatoire"
                                  :bad-email "Invalid email"}
                         :demo-help {:email {:desc "We won't spam you, ever"
                                             :ph "you.email@org"
                                             :info "Your email will only be used to send your travel details"}}}
                   "en" {:errors {:mandatory "This information is mandatory"
                                  :bad-email "Invalid email"}
                         :demo-help {:email {:info "Your email will only be used to send your travel details"}}}}}})


(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
) 

