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
            [cljsjs.codemirror]
            [cljsjs.codemirror.mode.clojure]
            [cljsjs.codemirror.addons.matchbrackets]
            [cljsjs.codemirror.addons.closebrackets]
            [cognitect.transit :as t])
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
               (>! c [:ko "Internal Server Error"])))
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

(def app-state (atom {:playground {:title "Booking reservation"
                                   :desc  "An hypothetic booking form"
                                   :k     "booking"
                                   :style "string"
                                   :src   (with-out-str (pprint booking))}
                      :demos      [{:title   "Basic types : The usual suspects"
                                    :id      "usual-suspects"
                                    :desc    (html [:div
                                                    [:h5 "To create a form you need :"]
                                                    [:ul
                                                     [:li "The name of the component as a Keyword"]
                                                     [:li "A prismatic Schema describing the data (prismactic/schema is required with the prefix s)"]
                                                     [:li "An action function taking 3 parameters : the app cursor, the owner and the result map"]]
                                                    [:div "To use an example, click on \"Compile\"; The form will appear in the display section.
                                       The result section show the app cursor value"]
                                                    [:div "Feel free to modifiy the form specification and recompile."]])
                                    :content [{:k     :demo-1
                                               :title "String"
                                               :desc  (html [:div "Let's start with a very simple form containing a single field :name of type String : "
                                                             [:code "{:name s/Str}"]])
                                               :src   "{:name :demo-string\n :schema {:name s/Str}\n :action (fn [app owner result]\n     (om/update! app :demo-1 result))}"
                                               :style "string"}
                                              {:k     :demo-num
                                               :title "Number"
                                               :desc  (html [:div
                                                             [:div "Need a number ? use the schema : " [:code "s/Num"]]
                                                             [:div "You'll only be able to type numeric characters : numbers and \".\"\n Try to type alphabetic characters"]])
                                               :src   "{:name   :demo-num\n :schema {:weight s/Num}\n :action (fn [app owner result]\n    (om/update! app :demo-num result))}"
                                               :style "numeric"}
                                              {:k     :demo-int
                                               :title "Integer"
                                               :desc  (html [:div
                                                             [:div "Need an Integer ? use the schema : " [:code "s/Int"]]
                                                             [:div "You'll only be able to type numeric characters"]])
                                               :src   "{:name   :demo-int\n :schema {:age s/Int}\n :action (fn [app owner result]\n     (om/update! app :demo-int result))}"
                                               :style "numeric"}
                                              {:k     :demo-inst
                                               :title "Date"
                                               :desc  (html [:div
                                                             [:div "Need a Date ? Use the schema : " [:code "s/Inst"]]
                                                             [:div "With the google Closure DatePicker, the rendering is the same across browsers."]])
                                               :src   "{:name   :demo-inst\n :schema {:departure s/Inst}\n :action (fn [app owner result]\n (om/update! app :demo-inst result))}"
                                               :style "inst"}
                                              {:src   "{:name   :demo-enum\n :schema {:langage (s/enum \"Clojure\"\n                           \"clojureScript\"\n                           \"ClojureCLR\")}\n :action (fn [app owner result]\n     (om/update! app :demo-enum result))}"
                                               :k     :demo-enum
                                               :title "Enum"
                                               :desc  (html [:div
                                                             [:div "Need to pick one item from a list ? Use the schema : " [:code "s/enum"]]
                                                             [:div "The Select is often not the best choice for the UX but " [:a {:href "#lists-variations"} "you have options to change the display."]]])
                                               :style "enum"}
                                              {:src   "{:name  :demo-bool\n :schema {:boolean s/Bool}\n :action (fn [app owner result]\n     (om/update! app :demo-bool result))\n :opts {:init {:boolean false}}}"
                                               :k     :demo-bool
                                               :title "Boolean"
                                               :desc  (html [:div
                                                             [:div "Need a boolean ? Use the schema : " [:code "s/Bool"]]
                                                             [:div "A boolean is by default represented with a checkbox"]])
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
                                    :content [{:k     :demo-enum
                                               :title "Display the choices as a segmented control "
                                               :desc  (html [:div "Display a list as a segmented control with the type option : " [:code "{:type \"btn-group\"}"]])
                                               :src   "{:name   :demo-enum\n :schema {:language (s/enum \"Clojure\"\n                           \"ClojureScript\"\n                           \"ClojureCLR\")}\n :action (fn [app owner result]\n               (om/update! app :demo-enum result))\n :opts {:language {:type \"btn-group\"}}}"
                                               :style "string "}
                                              {:title "Display the choices as a liste of radio buttons"
                                               :k     :demo-enum
                                               :src   "{:name   :demo-enum\n :schema {:language (s/enum \"Clojure\"\n                           \"ClojureScript\"\n                           \"ClojureCLR\")}\n :action (fn [app owner result]\n               (om/update! app :demo-enum result))\n :opts {:language {:type \"radio-group\"}}}"
                                               :desc  (html [:div "Display a list as a vertical list of radio buttons with the type option : " [:code "{:type \"radio-group\"}"]])
                                               :style "inst"}
                                              {:src   "{:name   :demo-enum\n :schema {:language (s/enum \"Clojure\"\n                           \"ClojureScript\"\n                           \"ClojureCLR\")}\n :action (fn [app owner result]\n               (om/update! app :demo-enum result))\n :opts {:language {:type \"radio-group-inline\"}}}"
                                               :k     :demo-enum
                                               :title "Display the choices as list of radio inline"
                                               :desc  (html [:div "Display a list as an horizontal list of radio buttons with the type option : " [:code "{:type \"radio-group-inline\"}"]])
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
                                               :type     :i18n
                                               :title    "Add an info tooltip"
                                               :desc     "When you enter the field, a tooltip with an help message appears.
                                      The tooltip disappears when you leave the field"
                                               :src-i18n true
                                               :k        :demo-help-info
                                               :style    "string"}
                                              {:comp     demo-help-desc
                                               :type     :i18n
                                               :title    "Add a field description"
                                               :desc     "Adds the description below the field name"
                                               :src-i18n true
                                               :k        :demo-help-desc
                                               :style    "string"}
                                              {:comp     demo-help-placeholder
                                               :type     :i18n
                                               :title    "Add a placeholder"
                                               :desc     "Placeholders are often discouraged"
                                               :src-i18n true
                                               :k        :demo-help-placeholder
                                               :style    "string"}]}

                                   {:title   "Form submission options"
                                    :id      "action-options"
                                    :desc    (html [:div "Form submission comes in two flavors :"
                                                    [:ul
                                                     [:li "Continuous submission, the action button is available immediately after submission."]
                                                     [:li "One shot : after submission the form is locked and values are read only. You have to call the clean action to submit again."]
                                                     ]])
                                    :content [{:title "Always submittable"
                                               :desc  "By default, after submission, the form is ready to accept new data. This has been the case for all the demos so far"
                                               :src   "{:name   :action-reset\n :schema {:keep-posting s/Str}\n :action (fn [a o v]\n               (om/update! a :action-reset v))\n :opts {:action {:no-reset false}}}"
                                               :k     :action-reset
                                               :style "action-dark"}
                                              {:title "One shot submission"
                                               :desc  (html [:div "The second mode is obtained using the option " [:code " {:action {:one-shot true}} "] [:div "the action can be triggered once
                                                         but as a clean action is provided then the form can be cleaned and resubmitted again."]])
                                               :src   "{:name :action-resetable\n :schema {:resetable s/Str}\n :action (fn [a o v]\n       (om/update! a :action-resetable v))\n :clean (fn [a o]\n       (js/alert \"cleaning action !\"))\n :opts {:action {:one-shot true}}}"
                                               :k     :action-resetable
                                               :style "action-light"}
                                              {:title "Only one"
                                               :desc  (html [:div "If you really want your form to be submitted only once, use the option :" [:code " {:action {:one-shot true}} "]
                                                             [:div " combined with hidding The \"Clean\" button"]])
                                               :src   "{:name   :action-one-shot\n :schema {:only-once s/Str}\n :action (fn [a o v]\n       (om/update! a :action-one-shot v))\n :opts {:action {:one-shot true}}}"
                                               :k     :action-one-shot
                                               :style "action"}
                                              ]}
                                   {:title   "Form reset options"
                                    :id      "form-reset"
                                    :content [{:src   "{:name   :action-reset\n :schema {:reset-field s/Str}\n :action (fn [a o v]\n               (om/update! a :action-reset v))\n :opts {:action {:no-reset false}\n :init {:reset-field \"Modify me !\"}}}"
                                               :title "Reset to the initial values"
                                               :desc  "By default, after submission, the form is reset to the initial value. "
                                               :k     :action-reset
                                               :style "action-dark"}
                                              {:title "Keep the last submitted values"
                                               :desc  (html [:div "If you want to keep the last submitted values, use the option: "
                                                             [:div [:code "{opts {:action {:no-reset false}} "]]])
                                               :src   "{:name   :action-no-reset\n :schema {:do-not-reset s/Str}\n :action (fn [a o v]\n               (om/update! a :action-no-reset v))\n :opts {:action {:no-reset true}\n :init {:do-not-reset \"Modify me !\"}}}"
                                               :k     :action-no-reset
                                               :style "action-dark"}]}
                                   {:title   "Asynchronous actions"
                                    :id      "asynchronous-actions"
                                    :desc    (html [:div [:div "When your form submission is asynchronous :"
                                                          [:li "Add in the option" [:code "{:opts {:action {:async true}}}"]]
                                                          [:li "The action function gets an extra parameter : a Channel"]
                                                          [:li "The action is complete when the channel receives the result : either [:ok] or [:ko \"Error message\"]"]]])
                                    :content [{:type  :comp
                                               :comp  async-action
                                               :src   (with-out-str (repl/source async-action))
                                               :title "Succesful asynchronous submission"
                                               :desc  "This asynchronous action is a go block that waits one second before completing by putting [:ok] in the action channel."
                                               :k     :async-action
                                               :style "action"}
                                              {:type  :comp
                                               :comp  async-action-error
                                               :src   (with-out-str (repl/source async-action-error))
                                               :title "Asynchronous submission with error"
                                               :desc  "When an error occurs put [:ko] in the channel.
                                      In this example the result will always be nil"
                                               :k     :async-action-error
                                               :style "action"}]}
                                   {:title   "Complete forms"
                                    :id      "complete-forms"
                                    :content [{:title "Booking reservation"
                                               :desc  "An hypothetic booking form"
                                               :k     "booking"
                                               :style "string"
                                               :src   "{:name :booking\n :schema {:email  s/Str\n\t\t  :name s/Str\n\t\t  :departure  s/Inst\n\t\t  :arrival s/Inst\n\t\t  :guests s/Int}\n :action (fn [a o v]\n       (om/update! a :booking v))\n :opts {:init {:guests 1\n\t\t\t   :departure (js/Date.)}\n\t\t:order [:email :name :guests :departure :arrival ]\n\t\t:guests {:type \"stepper\"\n\t\t\t\t :attrs {:min 1 :max 6}}}}"}]}]}))

;________________________________________________
;                                                |
;        Compilation                             |
;                                                |
;________________________________________________|

(defn ns->cache-url
  "Locate the analyser cache for a specific ns"
  [ns ext]
  (str "js/cache/" (cljs/ns->relpath ns) ".cljs.cache." (name ext)))


(defn get-file
  "Get a server file"
  [url cb]
  (.send XhrIo url
         (fn [e]
           (cb (.. e -target getResponseText)))))

(def st (cljs/empty-state))


(defn load-cache-json!
  [cache ns]
  (go
    (get-file
     (ns->cache-url ns :json)
     (fn [txt]
       (let [rdr (t/reader :json)
             ns-cache (t/read rdr txt)]
         (cljs/load-analysis-cache! cache ns ns-cache))))))


(defn with-cache-ns!
  "Execute a function with a compiler cache that contains a ns"
  ([cache ns cb ext]
   (get-file
     (ns->cache-url ns ext)
     (fn [file]
       (->> (read-string file)
            (cljs/load-analysis-cache! cache ns))
       (cb cache))))
  ([cache ns cb]
   (with-cache-ns! cache ns cb :edn)))


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
  [st app forms]
  (cljs/eval-str st forms 'ex0.core
                 {:eval    cljs/js-eval
                  :context :expr
                  :ns      'om-inputs-demo.eval-helper}
                 (fn [{:keys [value error] :as result}]
                   (if error
                     (let [cause (ex-cause error)
                           data (ex-data cause)]
                       (prn error)
                       (om/update! app :error {:message (ex-message cause)
                                               :line    (:line data)
                                               :column (:column data)}))
                     (om/update! app :error nil))
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


(defn error-view
  [{:keys [message line column] :as error} owner]
  (om/component
    (dom/div #js {:className "alert alert-danger"}
             (dom/div #js {}
                      (str message " at [" line "," column "]")))))

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
                                               spec (eval cache demo (.getValue ed))]
                                          (om/set-state! owner :spec (make-input-comp spec)))} "Compile")
               (when (:error demo) (om/build error-view (:error demo)))
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
                                                                              spec (eval cache demo  (.getValue ed))]
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
    [{:keys [title desc id] :as section} owner]
    (reify
      om/IRenderState
      (render-state [_ state]
        (dom/section #js {:id id}
                     (dom/h2 #js {:className "section-title"} title)
                     (when desc (dom/div #js {:className "well"}
                                         desc))
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
                   (om/build navigation-view (:demos app))
                   (om/build content-view (:demos app) {:init-state state}))
                  (dom/div #js {:className "main"}
                           #_(om/build navigation-view app)
                           (om/build playground (:playground app) {:init-state state}))))))
   app-state
   {:target (. js/document (getElementById "app"))
    :shared (merge i18n {:cache cache})}))




(go
  (<! (load-cache-json! st 'cljs.core))
  (with-cache-ns! st 'om-inputs-demo.eval-helper app))


(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
) 

