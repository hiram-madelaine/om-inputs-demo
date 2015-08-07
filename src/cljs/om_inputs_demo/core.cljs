(ns ^:figwheel-always om-inputs-demo.core
  (:require-macros [cljs.core.async.macros :as m :refer [go alt!]])
  (:require [goog.dom :as gdom]
            [goog.events :as events]
            [goog.object :as gobj]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [om-inputs.core :as in :refer [make-input-comp]]
            [schema.core :as s]
            [cljs.tools.reader :as r :refer [read-string]]
            [sablono.core :refer-macros [html]]
            [cljs.repl :as repl]
            [cljs.core.async :refer [chan put! >! <! alts! timeout]]
            [cljs.js :as cljs]
            [cljs.pprint :refer [pprint]]
            [cljsjs.codemirror.mode.clojure]
            [cljsjs.codemirror.addons.matchbrackets]
            [cljsjs.codemirror.addons.closebrackets]
            [fipp.clojure])
  (:import [goog.events EventType]
           [goog.net XhrIo]))

(enable-console-print!)




;________________________________________________
;                                                |
;         The usual suspects                     |
;                                                |
;________________________________________________|

(def demo-string
  (make-input-comp
    {:name :demo-string
     :schema {:string s/Str}
     :action (fn [app owner result]
       (om/update! app :demo-1 result))}))

(def demo-num
  (make-input-comp
    {:name   :demo-num
     :schema {:number s/Num}
     :action (fn [app owner result]
               (om/update! app :demo-num result))}))

(def demo-int
  (make-input-comp
    {:name   :demo-int
     :schema {:integer s/Int}
     :action (fn [app owner result]
        (om/update! app :demo-int result))}))


  (def demo-bool
    (make-input-comp
      {:name   :demo-bool

       :schema {:boolean s/Bool}
       :action (fn [app owner result]
                 (om/update! app :demo-bool result))
       :opts {:init {:boolean false}}}))

  (def demo-inst
    (make-input-comp
      {:name   :demo-inst
       :schema {:Inst s/Inst}
       :action (fn [app owner result]
                 (om/update! app :demo-inst result))}))

  (def demo-enum
    (make-input-comp
      {:name   :demo-enum
       :schema {:langage (s/enum "Clojure"
                                 "clojureScript"
                                 "ClojureCLR")}
       :action (fn [app owner result]
                 (om/update! app :demo-enum result))}))
;________________________________________________
;                                                |
;         Variation around Numbers               |
;                                                |
;________________________________________________|

  (def demo-num-stepper
    (make-input-comp
      {:name   :demo-num-stepper
       :schema {:guests s/Int}
       :action (fn [app owner result]
                 (om/update! app :demo-num-stepper result))
       :opts {:guests {:type "stepper"}
              :init   {:guests 1}}}))

(def demo-num-segmented
  (make-input-comp
    {:name   :demo-num-segmented
     :schema {:guests s/Int}
     :action (fn [app owner result]
               (om/update! app :demo-num-segmented result))
     :opts {:guests {:type  "range-btn-group"
               :attrs {:min 1 :max 8 :step 1}}}}))


;________________________________________________
;                                                |
;         Native Date Picker                     |
;                                                |
;________________________________________________|

(def demo-date
  (make-input-comp
    {:name   :demo-date
     :schema {:date s/Inst}
     :action (fn [app owner result]
               (om/update! app :demo-date result))
     :opts {:date {:type "date"}}}))


(def demo-date-now
  (make-input-comp
    {:name   :demo-date-now
     :schema {:date s/Inst}
     :action (fn [app owner result]
               (om/update! app :demo-date-now result))
     :opts {:date {:type "now"}}}))

;________________________________________________
;                                                |
;         Variation around Lists                 |
;                                                |
;________________________________________________|


(def demo-enum-radio
  (make-input-comp
    {:name   :demo-enum
     :schema {:langage (s/enum "Clojure"
                               "clojureScript"
                               "ClojureCLR")}
     :action (fn [app owner result]
               (om/update! app :demo-enum result))
     :opts {:langage {:type "radio-group"}}}))

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
      {:name   :demo-optional-field
       :schema {:email                 s/Str
                (s/optional-key :name) s/Str}
       :action (fn [a o v]
                 (om/update! a :demo-optional-field v))}))

(def demo-optional-value
  (make-input-comp
    {:name   :demo-optional-value
     :schema {:email s/Str
              :name  (s/maybe s/Str)}
     :action (fn [a o v]
       (om/update! a :demo-optional-value v))}))


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
      {:name   :demo-validation-email
       :schema {:email s/Str}
       :action (fn [a o v]
                 (om/update! a :demo-validation-email v))
       :opts {:validations
        [[:email [:email] :bad-email]]}}))


;________________________________________________
;                                                |
;         Help i18n                              |
;                                                |
;________________________________________________|


(def demo-help-info
  (make-input-comp
    :demo-help-info
    {:email s/Str}
    (fn [a o v]
      (om/update! a :demo-help-info v))))

(def demo-help-desc
    (make-input-comp
      :demo-help-desc
      {:email s/Str}
      (fn [a o v]
        (om/update! a :demo-help-desc v))))


(def demo-help-placeholder
  (make-input-comp
    :demo-help-placeholder
    {:email s/Str}
    (fn [a o v]
      (om/update! a :demo-help-placeholder v))))



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


(def i18n {:i18n {"fr" {:errors                {:mandatory "Cette donnée est obligatoire"
                                                :bad-email "Invalid email"}
                        :demo-help-info        {:email {:info "Your email will only be used to send your travel details"}}
                        :demo-help-desc        {:email {:desc "We won't spam you, ever"}}
                        :demo-help-placeholder {:email {:ph   "you.email@org"}}}
                  "en" {:errors         {:mandatory "This information is mandatory"
                                         :bad-email "Invalid email"}
                        :demo-help-info        {:email {:info "Your email will only be used to send your travel details"}}
                        :demo-help-desc        {:email {:desc "We won't spam you, ever"}}
                        :demo-help-placeholder {:email {:ph   "you.email@org"}}
                         }}})

(def app-state (atom [{:title   "Basic types : The usual suspects"
                       :id      "usual-suspects"
                       :desc  (html [:div
                                     [:h5 "To create a form you need :"]
                                     [:ul
                                      [:li "The name of the component"]
                                      [:li "A prismatic Schema"]
                                      [:li "An action function"]]])
                       :content [{:src   "{:name :demo-string\n :schema {:name s/Str}\n :action (fn [app owner result]\n     (om/update! app :demo-1 result))}"
                                  :k     :demo-1
                                  :title "String"
                                  :desc "Let's start with the String data"
                                  :style "string"}
                                 {:src   "{:name   :demo-num\n :schema {:weight s/Num}\n :action (fn [app owner result]\n    (om/update! app :demo-num result))}"
                                  :k     :demo-num
                                  :title "Number"
                                  :desc  "You can only type numeric characters : numbers and ."
                                  :style "numeric"}
                                 {:src   "{:name   :demo-int\n :schema {:age s/Int}\n :action (fn [app owner result]\n     (om/update! app :demo-int result))}"
                                  :k     :demo-int
                                  :title "Integer"
                                  :desc  "You can only type numeric characters"
                                  :style "numeric"}
                                 {:src   "{:name   :demo-inst\n :schema {:departure s/Inst}\n :action (fn [app owner result]\n (om/update! app :demo-inst result))}"
                                  :k     :demo-inst
                                  :title "Date"
                                  :desc  "With the google Closure DatePicker, the rendering is the same across browsers"
                                  :style "inst"}
                                 {:src   "{:name   :demo-enum\n :schema {:langage (s/enum \"Clojure\"\n         \"clojureScript\"\n        \"ClojureCLR\")}\n :action (fn [app owner result]\n     (om/update! app :demo-enum result))}"
                                  :k     :demo-enum
                                  :title "Enum"
                                  :desc  "An enum is rendered by default with a select. We know this is not the best fot the UX. You have options to change the display."
                                  :style "enum"}
                                 {:src   "{:name  :demo-bool\n :schema {:boolean s/Bool}\n :action (fn [app owner result]\n     (om/update! app :demo-bool result))\n :opts {:init {:boolean false}}}"
                                  :k     :demo-bool
                                  :title "Boolean"
                                  :desc  "A boolean is by default represented with a checkbox"
                                  :style "bool"}]}
                      {:title   "Optional data"
                       :id      "optional-data"
                       :content [{:src   "{:name   :demo-optional-field\n :schema {:email s/Str\n          (s/optional-key :name) s/Str}\n :action (fn [a o v]\n    (om/update! a :demo-optional-field v))}"
                                  :k     :demo-optional-field
                                  :desc  "A field is marked optional with the schema (schema/optional-key)"
                                  :title "Optional field"}
                                 {:src   "{:name   :demo-optional-value\n     :schema {:email s/Str\n              :name  (s/maybe s/Str)}\n     :action (fn [a o v]\n       (om/update! a :demo-optional-value v))}"
                                  :k     :demo-optional-value
                                  :desc  "If you want the key to be present mark the value as maybe"
                                  :title "Optional value"}]}
                      {:title   "UX variation around Integer"
                       :id      "integer-variations"
                       :content [{
                                  :src   "{:name   :demo-num-stepper\n :schema {:guests s/Int}\n :action (fn [app owner result]\n (om/update! app :demo-num-stepper result))\n :opts {:guests {:type \"stepper\"}\n    :init   {:guests 1}}}"
                                  :k     :demo-num-stepper
                                  :title "Stepper for Integer adjustement"
                                  :desc  "A numeric field can be displayed as a stepper"
                                  :style "numeric"}
                                 {:src   "{:name   :demo-num-segmented\n :schema {:guests s/Int}\n :action (fn [app owner result]\n    (om/update! app :demo-num-segmented result))\n :opts\n {:guests\n  {:type \"range-btn-group\"\n   :attrs {:min 1 :max 8 :step 1}}}}"
                                  :k     :demo-num-segmented
                                  :title "Segmented control for Integer adjustement"
                                  :desc  "A numeric field can be displayed as a stepper"
                                  :style "numeric"}]}
                      {:title   "UX Around Date Picker"
                       :id      "date-options"
                       :content [{:src   "{:name   :demo-date\n :schema {:date s/Inst}\n :action (fn [app owner result]\n   (om/update! app :demo-date result))\n :opts {:date {:type \"date\"}}}"
                                  :k     :demo-date
                                  :title "Want the native Chrome date picker ?"
                                  :desc  "If you want to use the native chrome date input add the option :type=\"date\""
                                  :style "inst"
                                  }
                                 {:src   "{:name   :demo-date-now\n :schema {:date s/Inst}\n :action (fn [app owner result]\n   (om/update! app :demo-date-now result))\n :opts {:date {:type \"now\"}}}"
                                  :k     :demo-date-now
                                  :title "Capture a precise instant"
                                  :desc  "If you want to capture a precise instant just click "
                                  :style "inst"
                                  }]}
                      {:title   "UX variations around lists"
                       :id      "lists-variations"
                       :content [{:k     :demo-enum
                                  :src   "{:name   :demo-enum\n :schema {:langage (s/enum \"Clojure\"\n                           \"clojureScript\"\n                           \"ClojureCLR\")}\n :action (fn [app owner result]\n               (om/update! app :demo-enum result))\n :opts {:langage {:type \"radio-group\"}}}"
                                  :title "Display an enum as radio liste"
                                  :desc  "An enum is displayed by default with a select."
                                  :style "enum"}
                                 {:src   "{:name   :demo-enum\n :schema {:langage (s/enum \"Clojure\"\n                           \"clojureScript\"\n                           \"ClojureCLR\")}\n :action (fn [app owner result]\n               (om/update! app :demo-enum result))\n :opts {:langage {:type \"radio-group-inline\"}}}"
                                  :k     :demo-enum
                                  :title "Display an enum as radio inline"
                                  :desc  "An enum is displayed by default with a select"
                                  :style "enum"}
                                 {:src   "{:name   :demo-enum\n :schema {:langage (s/enum \"Clojure\"\n                           \"clojureScript\"\n                           \"ClojureCLR\")}\n :action (fn [app owner result]\n               (om/update! app :demo-enum result))\n :opts {:langage {:type \"btn-group\"}}}"
                                  :k     :demo-enum
                                  :title "handle en enum"
                                  :desc  "An enum is displayed by default with a select"
                                  :style "enum"}]}
                      {:title   "Constraint what can be typed"
                       :id      "constraint-typing"
                       :content [{:comp  demo-regex
                                  :src   (with-out-str (cljs.repl/source demo-regex))
                                  :k     :demo-regex
                                  :title "Assist the keyboard input of a String using a Regex"
                                  :desc  "During typing, the string must conform to the regex.
                                                                Because you are using regex you now have an other problem..."
                                  :style "string"}]}
                      {:title   "Extra Validations"
                       :id      "extra-validations"
                       :content [{:src   "{:name   :demo-validation-email\n :schema {:email s/Str}\n :action (fn [a o v]\n                 (om/update! a :demo-validation-email v))\n :opts {:validations\n        [[:email [:email] :bad-email]]}}"
                                  :title "Let's see how to declare valiation rule"
                                  :desc  (html [:p "The library " [:a "Verily"] " is used"])
                                  :k     :demo-validation-email}]}
                      {:title   "i18n - Help your users with information"
                       :id      "help-users"
                       :content [{:comp     demo-help-info
                                  :title    "Add an info tooltip"
                                  :desc     "When you enter the field, a tooltip with an help message appears.
                                      The tooltip disappears when you leave the field"
                                  :src-i18n true
                                  :k        :demo-help-info
                                  :style    "string"}
                                 {:comp     demo-help-desc
                                  :title    "Add a field description"
                                  :desc     "Adds the description below the field name"
                                  :src-i18n true
                                  :k        :demo-help-desc
                                  :style    "string"}
                                 {:comp     demo-help-placeholder
                                  :title    "Add a placeholder"
                                  :desc     "Placeholders are often discouraged"
                                  :src-i18n true
                                  :k        :demo-help-placeholder
                                  :style    "string"}]}

                      {:title   "Action's options"
                       :id      "action-options"
                       :content [{:comp  action-no-reset
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
                      {:title   "Asynchronous actions"
                       :id      "asynchronous-actions"
                       :content [{:comp  async-action
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
;        Compilation                             |
;                                                |
;________________________________________________|




(defn ns->cache-url
  "Locate the analyser cache for a specific ns"
  [ns]
  (str "js/cache/" (cljs/ns->relpath ns) ".cljs.cache.edn"))

(defn get-file
  "Get a server file"
  [url cb]
  (.send XhrIo url
         (fn [e]
           (cb (.. e -target getResponseText)))))

(def st (cljs/empty-state))

(defn with-cache-ns
  "Execute a function with a compiler cache that contains a ns"
  [cache ns cb]
  (get-file
    (ns->cache-url ns)
    (fn [file]
      (->> (read-string file)
           (cljs/load-analysis-cache! cache ns))
      (cb cache))))


#_(defn mount [id v]
  "Attach root from a schema"
  (om/root
    (fn [app owner]
      (reify
        om/IInitState
        (init-state [_]
          {:lang "en"})
        om/IRenderState
        (render-state [_ state]
          (om/build (make-input-comp v) app {:state state}))))
    {}
    {:target (. js/document (getElementById id))
     :shared i18n}))

(defn eval
  [st id forms]
  (cljs/eval-str st forms 'ex0.core
                 {:eval    cljs/js-eval
                  :context :expr
                  :ns      'om-inputs-demo.eval-helper}
                 (fn [{:keys [value error] :as result}]
                   (if error
                     (.log js/console (some-> result :error .-cause .-stack)))
                   value)))


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
;         Code Mirror                            |
;                                                |
;________________________________________________|

(def cm-opts
  #js {:fontSize 8
       ;:theme "zenburn"
       :lineNumbers true
       :matchBrackets true
       :autoCloseBrackets true
       :indentWithTabs true
       :mode #js {:name "clojure"}})

(defn textarea->cm
  "Insert a CodeMirror editor at div id with code."
  [id code]
  (let [ta (gdom/getElement id)]
    (js/CodeMirror
      #(.replaceChild (.-parentNode ta) % ta)
      (doto cm-opts
        (gobj/set "value" code)))))


(defn code-mirror-comp
  [{:keys [id src k] :as demo} owner]
  (reify
    om/IRenderState
    (render-state [_ {:keys [spec] :as  state}]
      (dom/div #js {}
               (dom/h4 #js {} "Source : ")
               (dom/textarea #js {:id (str id "-ed")})
               (dom/button #js {:type "button"
                               :className "btn btn-default btn-compile"
                               :onClick #(let [ed (om/get-state owner :cm)
                                               cache (om/get-shared owner :cache)
                                               spec (eval cache (str id "-form") (.getValue ed))]
                                          (om/set-state! owner :spec spec))} "Compile")
               (when spec
                 (dom/div #js {}
                          (dom/h4 #js {} "Display : ")
                          (om/build (make-input-comp spec) demo {:state state})
                          (dom/div #js {:className ""}
                                   (dom/h4 #js {} "Result : ")
                                   (dom/pre #js {}
                                            (dom/code #js {:className "clojure"}
                                                      (print-str (get demo k)))))))))
    om/IDidMount
    (did-mount [_]
      (let [ed (textarea->cm (str id "-ed") src)]
        (om/set-state! owner :cm ed)))))



;________________________________________________
;                                                |
;         Content components                     |
;                                                |
;________________________________________________|


  (defn i18n-as-str
    [owner k]
    (with-out-str (pprint (get-in (om/get-shared owner) [:i18n "fr" k]))))



(defn i18n-comp
  [{:keys [id k comp src src-i18n style] :as demo} owner]
  (om/component
    (dom/div #js {}
             (dom/h4 #js {} "i18n : ")
             (dom/pre #js {}
              (dom/code #js {:className "clojure"}
                        (or src
                            (i18n-as-str owner k))))
             (dom/h4 #js {} "Display : ")
             (dom/div #js {:id (str id "-form")})
             (om/build comp demo {:state (om/get-state owner)}))))


  (defn card-view
    [{:keys [id title desc k comp src src-i18n style] :as demo} owner]
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
                           (if src-i18n
                             (om/build i18n-comp demo {:state state})
                             (om/build code-mirror-comp demo {:state state})))
                   (dom/div #js{:className "card-footer"} ""))))))

  (defn section-view
    [{:keys [title id] :as section} owner]
    (reify
      om/IRenderState
      (render-state [_ state]
        (dom/section #js {:id id}
                     (dom/h2 #js {:className "section-title"} title)
                     (apply dom/div #js {:className "schema-types"}
                            (om/build-all card-view (:content section) {:init-state state}))))))

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


(defn app
  [cache]
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
                  (om/build content-view app {:init-state state})))))
   app-state
   {:target (. js/document (getElementById "app"))
    :shared (merge i18n {:cache cache})}))


(with-cache-ns st 'om-inputs-demo.eval-helper app)


(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
) 

