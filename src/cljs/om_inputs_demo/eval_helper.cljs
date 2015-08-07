(ns ^:figwheel-always om-inputs-demo.eval-helper
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [om-inputs.core :as in :refer [make-input-comp]]
            [schema.core :as s]
            [cljs.core.async :refer [put! chan <!]]))


(defn build-comp
  [spec]
  (make-input-comp spec))