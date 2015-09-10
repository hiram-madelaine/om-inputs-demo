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


(def demo-help-field
  (make-input-comp
    :demo-help-field
    {:field-1 s/Str}
    (fn [a o v]
      (om/update! a :demo-help-field v))))

(def demo-help-title
  (make-input-comp
    :demo-help-title
    {:email s/Str}
    (fn [a o v]
      (om/update! a :demo-help-title v))))

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


(def i18n {:i18n {"en" {:errors                {:mandatory    "This information is mandatory"
                                                :bad-email    "Invalid email"
                                                :bad-password "Your passwords are not identical"}
                        :demo-help-field       {:field-1 {:label "My i18n Label"}}
                        :demo-help-title       {:title "The title of your form"}
                        :demo-help-info        {:email {:info "Your email will only be used to send your travel details"}}
                        :demo-help-desc        {:email {:desc "We won't spam you, ever"}}
                        :demo-help-placeholder {:email {:ph "you.email@org"}}
                        :booking               {:title     "Your reservation"
                                                :email     {:desc "We won't spam you."}
                                                :room-type {:label "Room type"
                                                            :data {"house"      {:label "Entire Place"}
                                                                   "apartment" {:label "Private room"}
                                                                   "room"     {:label "Shared room"}}}}}}})
(def booking '{:name   :booking
               :schema {:email     s/Str
                        :name      s/Str
                        :departure s/Inst
                        :arrival   s/Inst
                        :guests    s/Int
                        :bedrooms  s/Int
                        :room-type (s/enum "house" "apartment" "room")}
               :action (fn [a o v]
                         (om/update! a :booking v))
               :opts
                       {:init        {:guests 1 :departure (js/Date.)}
                        :validations [[:email [:email] :bad-email]]
                        :room-type   {:type "btn-group"}
                        :bedrooms    {:type  "range-btn-group"
                                      :attrs {:min 1 :max 6}}
                        :guests      {:type  "stepper"
                                      :attrs {:min 1 :max 6}}}})



(def app-state (atom {:header (html [:h2 "Welcome to the  interactive tutorial of " [:a {:href "https://github.com/hiram-madelaine/om-inputs"} "om-inputs"] " !"]
                                    [:h5 "In this tutorial, you will discover all the functionalities of om-inputs, an " [:a {:href "https://github.com/omcljs/om"} "Om"] " library to generate forms with " [:a {:href "https://github.com/Prismatic/schema"} "Prismatic/schema."]]
                                    [:div {:id "help"}
                                     [:div {:class "header-help"}
                                      [:h5 "To build a form, om-inputs provides one function : "]
                                      [:code "make-input-comp"]
                                      [:h5 "It takes a form specification and produces an Om component : "]
                                      [:code "(def om-component (make-input-comp spec))"]]
                                     [:div {:class "header-help"}
                                      [:h5 "The form specification is a map that contains :"]
                                      [:ul
                                       [:li "the name of the component as a Keyword ;"]
                                       [:li "a prismatic Schema describing the data ;"]
                                       [:li "an action function taking 3 parameters : "
                                        [:div "the app cursor, the owner and the map result ;"]]
                                       [:li "Options to customize the component."]]]
                                     [:div {:class "header-help"}
                                      [:h5 "Each feature is presented in a demo card"]
                                      [:div "Each card contains a form specification that you can edit and compile on the fly. "]
                                      [:ul
                                       [:li "Click on \"Compile\"."]
                                       [:li "The form is rendered in the display section."]
                                       [:li "The result section shows the app cursor value."]
                                       [:li "Feel free to modify the form's specification and recompile."]]]])
                      :demos  [{:title   "Basic types: The usual suspects"
                                :desc    (html [:div "You describe your form using the leaf schemas: " [:code " s/Str, s/Num, s/Int, s/Bool, s/Inst"]
                                                [:div "in the demo sources  Prismatic/schema is required with the prefix: s"]])
                                :id      "usual-suspects"
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
                                           :style "blue"}
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
                                           :style "red"}]}
                               {:title   "Optional data"
                                :id      "optional-data"
                                :content [{:src   "{:name   :demo-optional-field\n :schema {:email s/Str\n          (s/optional-key :name) s/Str}\n :action (fn [a o v]\n    (om/update! a :demo-optional-field v))}"
                                           :k     :demo-optional-field
                                           :desc  "A field is marked optional with the schema (schema/optional-key)"
                                           :title "Optional field"
                                           :style "string"}
                                          {:src   "{:name   :demo-optional-value\n     :schema {:email s/Str\n              :name  (s/maybe s/Str)}\n     :action (fn [a o v]\n       (om/update! a :demo-optional-value v))}"
                                           :k     :demo-optional-value
                                           :desc  "If you want the key to be present mark the value as maybe"
                                           :title "Optional value"
                                           :style "inst"}]}
                               {:title   "UX variation around Integer"
                                :desc    (html [:h5 "It is possible to change the default rendering of a field by specifying a type."]
                                               [:div "Just add the type in the options. The rendering can be further refined by specifying attributes."])
                                :id      "integer-variations"
                                :content [{:src   "{:name   :demo-num-segmented\n :schema {:guests s/Int}\n :action (fn [app owner result]\n    (om/update! app :demo-num-segmented result))\n :opts\n {:guests\n  {:type \"range-btn-group\"\n   :attrs {:min 1 :max 8 :step 1}}}}\n"
                                           :k     :demo-num-segmented
                                           :title "Segmented control to choose an Integer in a range"
                                           :desc  (html [:div "Display a numeric field as segmented control using the type: "
                                                         [:code "\"range-btn-group\""]
                                                         [:div "The min, max and step can be set."]])
                                           :style "enum"}
                                          {:title "Stepper for easy Integer adjustement"
                                           :desc  (html [:div "DIsplay a numeric field as a stepper using the type: " [:code "\"stepper\" "]])
                                           :src   "{:name   :demo-num-stepper\n :schema {:guests s/Int}\n :action (fn [app owner result]\n (om/update! app :demo-num-stepper result))\n :opts {:guests\n  {:type \"stepper\" \n   :attrs {:min 2 :max 8 :step 2}}\n :init   {:guests 2}}}\n"
                                           :k     :demo-num-stepper
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
                                           :style "numeric"}]}
                               {:title   "UX Around Date Picker"
                                :id      "date-options"
                                :content [{:src   "{:name   :demo-date\n :schema {:date s/Inst}\n :action (fn [app owner result]\n   (om/update! app :demo-date result))\n :opts {:date {:type \"date\"}}}\n"
                                           :k     :demo-date
                                           :title "Want the native Chrome date picker ?"
                                           :desc  (html [:div "If you want to use the native chrome date input add the option " [:code "{:type=\"date\"}"]]
                                                        [:div "Note that it won't display the date picker if you are not in Chrome"])
                                           :style "blue"
                                           }
                                          {:src   "{:name   :demo-date-now\n :schema {:date s/Inst}\n :action (fn [app owner result]\n   (om/update! app :demo-date-now result))\n :opts {:date {:type \"now\"}}}\n"
                                           :k     :demo-date-now
                                           :title "Capture a precise instant"
                                           :desc  "If you want to capture a precise instant just click "
                                           :style "red"
                                           }]}
                               {:title   "Constraint what can be typed"
                                :id      "constraint-typing"
                                :content [{:src   "{:name :demo-regex\n     :schema {:regex #\"^[A-Z]{0,2}[0-9]{0,12}$\"}\n     :action (fn [app owner result]\n   (om/update! app :demo-regex result))}"
                                           :k     :demo-regex
                                           :title "Assist the keyboard input of a String using a Regex"
                                           :desc  "During typing, the string must conform to the regex.
                                                                Because you are using regex you now have an other problem..."
                                           :style "enum"}]}
                               {:title   "More complete validation rules"
                                :desc    (html [:div [:h5 "It is possible to add more complex validation rules than thos we can express with  Prismatic/schema."]
                                                [:div "I chose the library " [:a {:href "https://github.com/jkk/verily"} "Verily"] " for the following reasons: "]
                                                [:ul
                                                 [:li "the rules can be described as data structure"]
                                                 [:li "the rules are expressed on the whole map not by key"]
                                                 [:li "You can easily add new rules"]
                                                 [:li "It works for Clojure and ClojureScript"]]])
                                :id      "extra-validations"
                                :content [{:title "Specify an email field."
                                           :src   "{:name   :demo-validation-email\n :schema {:email s/Str}\n :action (fn [a o v]\n                 (om/update! a :demo-validation-email v))\n :opts {:validations\n        [[:email [:email] :bad-email]]}}"
                                           :desc  (html [:div "The :email field must conform to a valid email."])
                                           :k     :demo-validation-email
                                           :style "blue"}
                                          {:title "Inter fields validation"
                                           :desc  "The classical example of the password and password confrmation."
                                           :k     :demo-validation-passwords
                                           :src   "{:name   :demo-validation-password\n :schema {:password s/Str\n\t\t  :confirm s/Str}\n :action (fn [a o v] (om/update! a :demo-validation-password v))\n :opts {:order [:password :confirm]\n\t\t:password {:attrs {:type \"password\"}}\n\t\t:confirm {:attrs {:type \"password\"}}\n\t\t:validations\n        [[:equal [:confirm :password] :bad-password]]}}"
                                           :style "yellow"}]}
                               {:title   "i18n - Help your users with information"
                                :desc    (html [:h5 "It is possible to provide the labels and error messages in multiple languages. Just put a map in the shared data."]
                                               [:textarea {:id "i18n-demo"} (str (subs (with-out-str (pprint i18n)) 0 500) "...")])
                                :cm      "i18n-demo"
                                :id      "help-users"
                                :content [{:comp     demo-help-title
                                           :desc     "You can add a title to you form. "
                                           :type     :i18n
                                           :title    "Add a title to your form"
                                           :src-i18n true
                                           :k        :demo-help-title
                                           :style    "red"}
                                          {:comp     demo-help-field
                                           :k        :demo-help-field
                                           :type     :i18n
                                           :title    "Change the label of your fields"
                                           :desc     "You can provide the label for you fields in several languages"
                                           :src-i18n true
                                           :style    "blue"}
                                          {:comp     demo-help-desc
                                           :type     :i18n
                                           :title    "Add a field description"
                                           :desc     "Adds the description below the field name"
                                           :src-i18n true
                                           :k        :demo-help-desc
                                           :style    "inst"}
                                          {:comp     demo-help-info
                                           :type     :i18n
                                           :title    "Add an info tooltip"
                                           :desc     "When you enter the field, a tooltip with an help message appears. The tooltip disappears when you leave the field"
                                           :src-i18n true
                                           :k        :demo-help-info
                                           :style    "green"}
                                          {:comp     demo-help-placeholder
                                           :type     :i18n
                                           :title    "Add a placeholder"
                                           :desc     "Placeholders are often discouraged"
                                           :src-i18n true
                                           :k        :demo-help-placeholder
                                           :style    "string"}]}
                               {:title   "Change the order of fields"
                                :id      "change-order"
                                :content [{:title "When the order is not what you want"
                                           :desc  "The schema is not ordered so the fields won't be in the order you expect."
                                           :k     :demo-unordered
                                           :src   "{:name   :demo-unordered \n :schema {:first s/Int\n\t\t  :second s/Int\n\t\t  :third s/Int}\n :action (fn [a o v] (om/update! a :demo-unordered v))}"
                                           :style "red"}
                                          {:title "Change the order of fields"
                                           :desc  (html [:div "You can order the fields by specifying it in the options"] [:code "{:opts {:order [:first :second :third]}"])
                                           :k     :demo-order
                                           :src   "{:name   :demo-order\n :schema {:first s/Int\n\t\t  :second s/Int\n\t\t  :third s/Int}\n :action (fn [a o v] (om/update! a :demo-order v))\n :opts {:order [:first :second :third]}}"
                                           :style "green"}]}

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
                                           :style "string"}
                                          {:title "Only once"
                                           :desc  (html [:div "If you really want your form to be submitted only once, use the option :" [:code " {:action {:one-shot true}} "]
                                                         [:div " combined with hidding The \"Clean\" button"]])
                                           :src   "{:name   :action-one-shot\n :schema {:only-once s/Str}\n :action (fn [a o v]\n       (om/update! a :action-one-shot v))\n :opts {:action {:one-shot true}}}"
                                           :k     :action-one-shot
                                           :style "yellow"}
                                          ]}
                               {:title   "Form reset options"
                                :id      "form-reset"
                                :content [{:src   "{:name   :action-reset\n :schema {:reset-field s/Str}\n :action (fn [a o v]\n               (om/update! a :action-reset v))\n :opts {:action {:no-reset false}\n :init {:reset-field \"Modify me !\"}}}"
                                           :title "Reset to the initial values"
                                           :desc  "By default, after submission, the form is reset to the initial value. "
                                           :k     :action-reset
                                           :style "red"}
                                          {:title "Keep the last submitted values"
                                           :desc  (html [:div "If you want to keep the last submitted values, use the option: "
                                                         [:div [:code "{opts {:action {:no-reset false}} "]]])
                                           :src   "{:name   :action-no-reset\n :schema {:do-not-reset s/Str}\n :action (fn [a o v]\n               (om/update! a :action-no-reset v))\n :opts {:action {:no-reset true}\n :init {:do-not-reset \"Modify me !\"}}}"
                                           :k     :action-no-reset
                                           :style "string"}]}
                               {:title   "Asynchronous actions"
                                :id      "asynchronous-actions"
                                :desc    (html [:div "When your form submission is asynchronous :"
                                                [:ul
                                                 [:li "Add the option: " [:code "{:opts {:action {:async true}}}"]]
                                                 [:li "The action function gets an extra parameter : a Channel"]
                                                 [:li (html [:div "The action completes when the channel receives the result : either " [:code "[:ok]"] " or " [:code "[:ko \"Error message\"]"]])]]])
                                :content [{:type  :comp
                                           :comp  async-action
                                           :src   (with-out-str (repl/source async-action))
                                           :title "Succesful asynchronous submission"
                                           :desc  (html [:div "In this example The asynchronous action is a go block that waits one second before completing by putting " [:code "[:ok]"] " in the action channel."])
                                           :k     :async-action
                                           :style "green"}
                                          {:type  :comp
                                           :comp  async-action-error
                                           :src   (with-out-str (repl/source async-action-error))
                                           :title "Asynchronous submission with error"
                                           :desc  (html [:div "When an error occurs put " [:code "[:ko]"] " in the channel.
                                      In this example the result will always be nil"])
                                           :k     :async-action-error
                                           :style "yellow"}]}
                               {:title   "Playground"
                                :id      "complete-forms"
                                :content [{:title "Booking reservation"
                                           :desc  "This is a more involved example combining several fetaures."
                                           :k     :booking
                                           :type  :playground
                                           :style "blue"
                                           :src   "{:name :booking,\n :schema\n {:email s/Str,\n  :arrival s/Inst,\n  (s/optional-key :departure) s/Inst,\n  :guests s/Int,\n  (s/optional-key :bedrooms )s/Int,\n  (s/optional-key :room-type) \n  (s/enum \"house\" \"apartment\" \"room\")},\n :action (fn [a o v] (om/update! a :booking v)),\n :opts\n {:init {:guests 1, :arrival (js/Date.)},\n  :order [:arrival \n\t\t  :departure \n\t\t  :guests \n\t\t  :room-type \n\t\t  :bedrooms \n\t\t  :email]\n  :validations [[:email [:email] :bad-email]],\n  :room-type {:type \"btn-group\"},\n  :bedrooms {:type \"range-btn-group\", \n\t\t\t :attrs {:min 1, :max 6}},\n  :guests {:type \"stepper\", \n\t\t   :attrs {:min 1, :max 6}}}}" #_(with-out-str (pprint booking))}]}]}))


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
                   (om/build-all nav-entry-view app)]]
                 [:img {:src "images/cljs-white.png" :class "logo-small" :alt "ClojureScript"}]))))

;________________________________________________
;                                                |
;         Code Mirror                            |
;                                                |
;________________________________________________|

(def cm-opts
  #js {:fontSize 8
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


;________________________________________________
;                                                |
;         Content components                     |
;                                                |
;________________________________________________|

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
               (dom/h5 #js {} "Source : ")
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
                          (dom/h5 #js {} "Form : ")
                          (om/build spec demo {:state state})
                          (dom/div #js {:className ""}
                                   (dom/h6 #js {} "Result : ")
                                   (dom/pre #js {}
                                            (dom/code #js {:className "clojure"}
                                                      (print-str (get demo k)))))))))
    om/IDidMount
    (did-mount [_]
      (let [ed (textarea->cm (str id "-ed") src)]
        (om/set-state! owner :cm ed)))))




  (defn i18n-as-str
    [owner k]
    (with-out-str (pprint (get-in (om/get-shared owner) [:i18n "en" k]))))



(defn i18n-comp
  [{:keys [id k comp] :as demo} owner]
  (om/component
    (dom/div #js {}
             (dom/h5 #js {} "i18n : ")
             (dom/pre #js {}
              (dom/code #js {:className "clojure"}
                        (i18n-as-str owner k)))
             (dom/h5 #js {} "Form : ")
             (dom/div #js {:id (str id "-form")})
             (om/build comp demo {:state (om/get-state owner)}))))

(defn comp-view
  "Display a predefined form"
  [{:keys [k comp src] :as demo} owner]
  (reify
    om/IRenderState
    (render-state [_ state]
      (dom/div #js {}
               (dom/h5 #js {} "Source : ")
               (dom/pre #js {}
                        (dom/code #js {:className "clojure"}
                                  src))
               (dom/h5 #js {} "Form : ")
               (om/build comp demo {:state (om/get-state owner)})
               (dom/div #js {:className ""}
                        (dom/h5 #js {} "Result : ")
                        (dom/pre #js {}
                                 (dom/code #js {:className "clojure"}
                                           (print-str (get demo k)))))))))
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
                                                   (dom/h5 #js {} "Source : ")
                                                   (dom/textarea #js {:id (str id "-ed")})
                                                   (dom/button #js {:type      "button"
                                                                    :className "btn btn-default btn-compile"
                                                                    :onClick   #(let [ed (om/get-state owner :cm)
                                                                                      cache (om/get-shared owner :cache)
                                                                                      spec (eval cache demo  (.getValue ed))]
                                                                                 (om/set-state! owner :spec (make-input-comp spec)))} "Compile")
                                                   (when (:error demo) (om/build error-view (:error demo))))
                                          (when spec
                                            (dom/div #js {:className "play-render"}
                                                     (om/build spec demo {:state state})
                                                     (dom/div #js {:className "play-result"}
                                                              (dom/h4 #js {} "Result : ")
                                                              (dom/pre #js {}
                                                                       (dom/code #js {:className "clojure"}
                                                                                 (print-str (get demo k)))))))))
                        (dom/div #js{:className "card-footer"} ""))))))

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


  (defmulti demo-view (fn [demo _] (:type demo)))

  (defmethod demo-view :playground
    [demo owner]
    (playground demo owner))

  (defmethod demo-view :default
    [demo owner]
    (card-view demo owner))

  (defn section-view
    "Display a section that contains demos."
    [{:keys [title desc id cm] :as section} owner]
    (reify
      om/IDidMount
      (did-mount [_]
        (when cm
          (prn cm)
          (js/CodeMirror.fromTextArea (gdom/getElement cm) #js {:readOnly true})))
      om/IRenderState
      (render-state [_ state]
        (dom/section #js {:id id}
                     (dom/h3 #js {:className "section-title"} title)
                     (when desc (dom/div #js {:className "section-desc"}
                                         desc))
                     (apply dom/div #js {:className "schema-types"}
                            (om/build-all demo-view (:content section) {:init-state state}))))))

(defn header
  [head owner]
  (om/component
    (dom/div #js {:className "header"} head)))


(defn content-view
  "Display the demo sections. "
    [app owner]
    (reify
      om/IRenderState
      (render-state
        [_ state]
        (dom/div #js {:className "content"}
                 (om/build header (:header app))
                 (apply dom/div #js {}
                        (om/build-all section-view (:demos app) {:init-state state}))))))


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
                   (om/build content-view app {:init-state state}))))))
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

