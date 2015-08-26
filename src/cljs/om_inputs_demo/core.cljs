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
            [cljsjs.codemirror.addons.closebrackets])
  (:import [goog.events EventType]
           [goog.net XhrIo]))

(enable-console-print!)

;________________________________________________
;                                                |
;         Asyn Actions                           |
;                                                |
;________________________________________________|


  (def async-action
    (make-input-comp
      {:name :async-action
       :schema {:async-action s/Str}
       :action (fn [a o v c]
         (go
           (<! (timeout 1000))
           (om/update! a :async-action v)
           (>! c [:ok])))
       :opts {:action {:async true}}}))


(def async-action-error
  (make-input-comp
    {:name :async-action-error
     :schema {:async-action-error s/Str}
     :action (fn [a o v c]
             (go
               (<! (timeout 1000))
               (>! c [:ko])))
     :opts {:action {:async true}}}))



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
                        :booking {:title "Your information"} }}})

(def app-state (atom [{:title   "Basic types : The usual suspects"
                       :id      "usual-suspects"
                       :desc  (html [:div
                                     [:h5 "To create a form you need :"]
                                     [:ul
                                      [:li "The name of the component"]
                                      [:li "A prismatic Schema"]
                                      [:li "An action function"]]])
                       :content [{:k     :demo-1
                                  :title "String"
                                  :desc "Let's start with the String data"
                                  :src   "{:name :demo-string\n :schema {:name s/Str}\n :action (fn [app owner result]\n     (om/update! app :demo-1 result))}"
                                  :style "string"}
                                 {:k     :demo-num
                                  :title "Number"
                                  :desc  "You can only type numeric characters : numbers and ."
                                  :src   "{:name   :demo-num\n :schema {:weight s/Num}\n :action (fn [app owner result]\n    (om/update! app :demo-num result))}"
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
                                 {:src   "{:name   :demo-enum\n :schema {:langage (s/enum \"Clojure\"\n                           \"clojureScript\"\n                           \"ClojureCLR\")}\n :action (fn [app owner result]\n     (om/update! app :demo-enum result))}"
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
                       :content [{:src   "{:name   :demo-num-segmented\n :schema {:guests s/Int}\n :action (fn [app owner result]\n    (om/update! app :demo-num-segmented result))\n :opts\n {:guests\n  {:type \"range-btn-group\"\n   :attrs {:min 1 :max 8 :step 1}}}}\n"
                                  :k     :demo-num-segmented
                                  :title "Segmented control for Integer adjustement"
                                  :desc  "A numeric field can be displayed as a stepper"
                                  :style "enum"}
                                 {
                                  :src   "{:name   :demo-num-stepper\n :schema {:guests s/Int}\n :action (fn [app owner result]\n (om/update! app :demo-num-stepper result))\n :opts {:guests\n  {:type \"stepper\" \n   :attrs {:min 2 :max 8 :step 2}}\n :init   {:guests 2}}}\n"
                                  :k     :demo-num-stepper
                                  :title "Stepper for Integer adjustement"
                                  :desc  "A numeric field can be displayed as a stepper"
                                  :style "numeric"}
                                 ]}
                      {:title   "UX variations around lists"
                       :id      "lists-variations"
                       :content [{:src   "{:name   :demo-enum\n :schema {:language (s/enum \"Clojure\"\n                           \"ClojureScript\"\n                           \"ClojureCLR\")}\n :action (fn [app owner result]\n               (om/update! app :demo-enum result))\n :opts {:language {:type \"btn-group\"}}}"
                                  :k     :demo-enum
                                  :title "handle en enum"
                                  :desc  "An enum is displayed by default with a select"
                                  :style "string "}
                                 {:k     :demo-enum
                                  :src   "{:name   :demo-enum\n :schema {:language (s/enum \"Clojure\"\n                           \"ClojureScript\"\n                           \"ClojureCLR\")}\n :action (fn [app owner result]\n               (om/update! app :demo-enum result))\n :opts {:language {:type \"radio-group\"}}}"
                                  :title "Display an enum as radio liste"
                                  :desc  "An enum is displayed by default with a select."
                                  :style "inst"}
                                 {:src   "{:name   :demo-enum\n :schema {:language (s/enum \"Clojure\"\n                           \"ClojureScript\"\n                           \"ClojureCLR\")}\n :action (fn [app owner result]\n               (om/update! app :demo-enum result))\n :opts {:language {:type \"radio-group-inline\"}}}"
                                  :k     :demo-enum
                                  :title "Display an enum as radio inline"
                                  :desc  "An enum is displayed by default with a select"
                                  :style "numeric"}

                                 ]}
                      {:title   "UX Around Date Picker"
                       :id      "date-options"
                       :content [{:src   "{:name   :demo-date\n :schema {:date s/Inst}\n :action (fn [app owner result]\n   (om/update! app :demo-date result))\n :opts {:date {:type \"date\"}}}\n"
                                  :k     :demo-date
                                  :title "Want the native Chrome date picker ?"
                                  :desc  "If you want to use the native chrome date input add the option :type=\"date\""
                                  :style "inst"
                                  }
                                 {:src   "{:name   :demo-date-now\n :schema {:date s/Inst}\n :action (fn [app owner result]\n   (om/update! app :demo-date-now result))\n :opts {:date {:type \"now\"}}}\n"
                                  :k     :demo-date-now
                                  :title "Capture a precise instant"
                                  :desc  "If you want to capture a precise instant just click "
                                  :style "string"
                                  }]}

                      {:title   "Constraint what can be typed"
                       :id      "constraint-typing"
                       :content [{:src   "{:name :demo-regex\n     :schema {:regex #\"^[A-Z]{0,2}[0-9]{0,12}$\"}\n     :action (fn [app owner result]\n   (om/update! app :demo-regex result))}"
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
                                  :type :i18n
                                  :title    "Add an info tooltip"
                                  :desc     "When you enter the field, a tooltip with an help message appears.
                                      The tooltip disappears when you leave the field"
                                  :src-i18n true
                                  :k        :demo-help-info
                                  :style    "string"}
                                 {:comp     demo-help-desc
                                  :type :i18n
                                  :title    "Add a field description"
                                  :desc     "Adds the description below the field name"
                                  :src-i18n true
                                  :k        :demo-help-desc
                                  :style    "string"}
                                 {:comp     demo-help-placeholder
                                  :type :i18n
                                  :title    "Add a placeholder"
                                  :desc     "Placeholders are often discouraged"
                                  :src-i18n true
                                  :k        :demo-help-placeholder
                                  :style    "string"}]}

                      {:title   "Action's options"
                       :id      "action-options"
                       :content [{:src   "{:name   :action-no-reset\n :schema {:no-reset s/Str}\n :action (fn [a o v]\n               (om/update! a :action-no-reset v))\n :opts {:action {:no-reset true}}}"
                                  :title "Keep the last posted value"
                                  :desc  "By default the form is reset but with the option :no-reset true
                                                   the last values are kept"
                                  :k     :action-no-reset
                                  :style "action-dark"}
                                 {:src   "{:name   :action-one-shot\n :schema {:one-shot s/Str}\n :action (fn [a o v]\n       (om/update! a :action-one-shot v))\n :opts {:action {:one-shot true}}}"
                                  :title "This action can be done only once"
                                  :desc  "with this options {:action {:one-shot true}} the action can be triggered once
                                                          \"Clean\" button has no action and can be hidden with CSS"
                                  :k     :action-one-shot
                                  :style "action"}
                                 {:src   "{:name :action-resetable\n     :schema {:resetable s/Str}\n :action (fn [a o v]\n       (om/update! a :action-resetable v))\n :clean (fn [a o]\n       (prn \"Let's create an other item !\"))\n :opts {:action {:one-shot true}}}"
                                  :title "Cycle between action & clean"
                                  :desc  "with this options {:action {:one-shot true}} the action can be triggered once
                                                         but as a clean action is provided then the form can be cleaned and resubmitted again."
                                  :k     :action-resetable
                                  :style "action-light"}]}
                      {:title   "Asynchronous actions"
                       :id      "asynchronous-actions"
                       :content [{:type :comp
                                  :comp  async-action
                                  :src   (with-out-str (repl/source async-action))
                                  :title "Action can be asynchronous"
                                  :desc  "When the action is asynchronous the action fn has an extra parameter
                                                    : a channel. You must use it to indicate if the result of the operation is succesful or not.
                                                     Respectively with [:ok] or failed with [:ko error]"
                                  :k     :async-action
                                  :style "action"}
                                 {:type :comp
                                  :comp  async-action-error
                                  :src   (with-out-str (repl/source async-action-error))
                                  :title "Asynchronous action and errors"
                                  :desc  "When an error occurs put [:ko] in the channel.
                                      In this example the result will always be nil"
                                  :k     :async-action-error
                                  :style "action"}]}
                      {:title "Complete forms"
                       :id "complete-forms"
                       :content [{:title "Booking reservation"
                                  :desc "An hypothetic booking form"
                                  :k "booking"
                                  :style "string"
                                  :src "{:name :booking\n :schema {:email  s/Str\n\t\t  :name s/Str\n\t\t  :departure  s/Inst\n\t\t  :arrival s/Inst\n\t\t  :guests s/Int}\n :action (fn [a o v]\n       (om/update! a :booking v))\n :opts {:init {:guests 1\n\t\t\t   :departure (js/Date.)}\n\t\t:order [:email :name :guests :departure :arrival ]\n\t\t:guests {:type \"stepper\"\n\t\t\t\t :attrs {:min 1 :max 6}}}}"}]}]))

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
      (dom/div #js {:className "navigation"}
               (html
                 [:div {:className "index"}
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


(defn editor-comp
  "Display a demo as a CodeMirror editor"
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
                                          (om/set-state! owner :spec (make-input-comp spec)))} "Compile")
               (when spec
                 (dom/div #js {}
                          (dom/h4 #js {} "Display : ")
                          (om/build spec demo {:state state})
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
  [{:keys [id k comp] :as demo} owner]
  (om/component
    (dom/div #js {}
             (dom/h4 #js {} "i18n : ")
             (dom/pre #js {}
              (dom/code #js {:className "clojure"}
                        (i18n-as-str owner k)))
             (dom/h4 #js {} "Display : ")
             (dom/div #js {:id (str id "-form")})
             (om/build comp demo {:state (om/get-state owner)}))))

(defn comp-view
  "Display a predefined form"
  [{:keys [k comp src] :as demo} owner]
  (reify
    om/IRenderState
    (render-state [_ state]
      (dom/div #js {}
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
                                           (print-str (get demo k)))))))))


  (defn card-view
    "Display a demo as a Card "
    [{:keys [type title desc style] :as demo} owner]
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
                           (condp = type
                             :i18n (om/build i18n-comp demo {:state state})
                             :comp  (om/build comp-view demo {:state state})
                             (om/build editor-comp demo {:state state})))
                   (dom/div #js{:className "card-footer"} ""))))))

(defn playground
  "Playground"
  [{:keys [id k title desc style src] :as demo} owner]
  (reify
    om/IDidMount
    (did-mount [_]
      (let [ed (textarea->cm (str id "-ed") src)]
        (om/set-state! owner :cm ed)))
    om/IRenderState
    (render-state [_ {:keys [spec] :as  state}]
      (dom/div #js {:className "playground"}
               (dom/div #js {:className (in/styles (str "card-" style) "playground")}
                        (dom/div #js {:className "card-header"}
                                 (dom/label #js {} title))
                        (dom/div #js {:className "card-body"}
                                 (dom/div #js {:className "play-body"}
                                          (dom/div #js {:className "play-source"}
                                           (when desc (dom/div #js {:className "well"}
                                                               desc))
                                           (dom/h4 #js {} "Source : ")
                                           (dom/textarea #js {:id (str id "-ed")})
                                           (dom/button #js {:type      "button"
                                                            :className "btn btn-default btn-compile"
                                                            :onClick   #(let [ed (om/get-state owner :cm)
                                                                              cache (om/get-shared owner :cache)
                                                                              spec (eval cache (str id "-form") (.getValue ed))]
                                                                         (om/set-state! owner :spec (make-input-comp spec)))} "Compile"))
                                   (when spec
                                     (dom/div #js {:className "play-render"}
                                              (om/build spec demo {:state state})
                                              (dom/div #js {:className "play-result"}
                                                       (dom/h4 #js {} "Result : ")
                                                       (dom/pre #js {}
                                                                (dom/code #js {:className "clojure"}
                                                                          (print-str (get demo k)))))))))
                        (dom/div #js{:className "card-footer"} ""))))))


  (defn section-view
    "Display a section that contains demos."
    [{:keys [title id] :as section} owner]
    (reify
      om/IRenderState
      (render-state [_ state]
        (dom/section #js {:id id}
                     (dom/h2 #js {:className "section-title"} title)
                     (apply dom/div #js {:className "schema-types"}
                            (om/build-all card-view (:content section) {:init-state state}))))))

(defn content-view
  "Display the demo sections. "
    [app owner]
    (reify
      om/IRenderState
      (render-state
        [_ state]
        (apply dom/div #js {:className "content"}
                 (om/build-all section-view app {:init-state state})))))


(def booking '{:name :booking
               :schema {:email  s/Str
                        :name s/Str
                        :departure  s/Inst
                        :arrival s/Inst
                        :guests s/Int
                        :bedrooms s/Int
                        :room-type (s/enum "house" "appartement" "room")}
               :action (fn [a o v]
                         (om/update! a :booking v))
               :opts {:init {:guests 1
                             :departure (js/Date.)}
                      :room-type {:type "btn-group"}
                      :bedrooms {:type "range-btn-group"
                                 :attrs {:min 1 :max 6 }}
                      ;:order [:email :name :guests :room-type  :departure :arrival]
                      :guests {:type "stepper"
                               :attrs {:min 1 :max 6}}}})
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
         (dom/div #js {}
                  (dom/div #js {:className "main"}
                   (om/build navigation-view app)
                   (om/build content-view app {:init-state state}))
                  (dom/div #js {:className "main"}
                           (om/build navigation-view app)
                           (om/build playground {:title "Booking reservation"
                                                 :desc  "An hypothetic booking form"
                                                 :k     "booking"
                                                 :style "string"
                                                 :src   (with-out-str (pprint booking))} {:init-state state}))))))
   app-state
   {:target (. js/document (getElementById "app"))
    :shared (merge i18n {:cache cache})}))


(with-cache-ns st 'om-inputs-demo.eval-helper app)


(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
) 

