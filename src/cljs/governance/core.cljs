(ns governance.core
  (:require
    [day8.re-frame.http-fx]
    [reagent.dom :as rdom]
    [reagent.core :as r]
    [re-frame.core :as rf]
    [goog.events :as events]
    [goog.history.EventType :as HistoryEventType]
    [markdown.core :refer [md->html]]
    [governance.ajax :as ajax]
    ;; TODO replace with gov.components library instead of using raws
    [governance.components.raw :as raw]
    [governance.events]
    [reitit.core :as reitit]
    [reitit.frontend.easy :as rfe]
    [governance.routes.crud]
    [clojure.string :as string])
  (:import goog.History))

(defn nav-link [uri title page]
  [raw/AnchorButton
   {:href  uri
    :class ["bp3-minimal"
            (when (= page @(rf/subscribe [:common/page])) :is-active)]}
   title])

(defn navbar []
  [raw/Navbar
   [raw/NavbarGroup
    [raw/NavbarHeading
     [:a {:href "/" :style {:font-weight :bold}} "governance"]]]
   [raw/NavbarGroup
    [nav-link "/" "Home" :home]
    [nav-link "/about" "About" :about]]])

(defn about-page []
  [:section.section>div.container>div.content
   [:img {:src "/img/warning_clojure.png"}]])

;(defn home-page []
;  [:section.section>div.container>div.content
;   (when-let [docs @(rf/subscribe [:docs])]
;     [:div {:dangerouslySetInnerHTML {:__html (md->html docs)}}])])

(def *temp-val (r/atom nil))
(defn home-page []
  [:section.section>div.container>div.content
   ;[raw/H5 "asdf"]
   ;[raw/Button {:on-click #(js/alert 1)} "Asdf"]
   [raw/InputGroup {:value "@*temp-val"
                    :on-change #(reset! *temp-val (oops.core/oget % "target.value"))
                    :async-control true}]
   [raw/Text {} (pr-str @*temp-val)]
   [raw/DateRangePicker {:single-month-only      true
                         :allow-single-day-range true
                         :on-change              #(this-as this
                                                    (js/console.log this)
                                                    (js/console.log %))}]])

(defn page []
  (if-let [page @(rf/subscribe [:common/page])]
    [:div
     [navbar]
     [page]]))

(defn navigate! [match _]
  (rf/dispatch [:common/navigate match]))

(def router
  (reitit/router
    [["/" {:name        :home
           :view        #'home-page
           :controllers [{:start (fn [_] (rf/dispatch [:page/init-home]))}]}]
     ["/about" {:name :about
                :view #'about-page}]]))

(defn start-router! []
  (rfe/start!
    router
    navigate!
    {:use-fragment false}))

;; -------------------------
;; Initialize app
(defn ^:dev/after-load mount-components []
  (rf/clear-subscription-cache!)
  (rdom/render [#'page] (.getElementById js/document "app")))

(defn init! []
  (start-router!)
  (ajax/load-interceptors!)
  (mount-components))
