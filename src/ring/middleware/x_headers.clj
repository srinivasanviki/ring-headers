(ns ring.middleware.x-headers
  "Middleware for adding various 'X-' response headers."
  (:require [clojure.string :as str]
            [ring.util.response :as resp]))

(defn- allow-from? [frame-options]
  (and (map? frame-options)
       (= (keys frame-options) [:allow-from])
       (string? (:allow-from frame-options))))

(defn- format-frame-options [frame-options]
  (if (map? frame-options)
    (str "ALLOW-FROM " (:allow-from frame-options))
    (str/upper-case (name frame-options))))

(defn wrap-frame-options
  "Middleware that adds the X-Frame-Options header to the response. This governs
  whether your site can be rendered in a <frame>, <iframe> or <object>, and is
  typically used to prevent clickjacking attacks.

  The following frame options are allowed:

  :deny             - prevent any framing of the content
  :sameorigin       - allow only the current site to frame the content
  {:allow-from uri} - allow only the specified URI to frame the page

  The :deny and :sameorigin options are keywords, while the :allow-from option
  is a map consisting of one key/value pair.

  Note that browser support for :allow-from is incomplete. See:
  https://developer.mozilla.org/en-US/docs/Web/HTTP/X-Frame-Options"
  [handler frame-options]
  {:pre [(or (= frame-options :deny)
             (= frame-options :sameorigin)
             (allow-from? frame-options))]}
  (let [header-value (format-frame-options frame-options)]
    (fn [request]
      (-> (handler request)
          (resp/header "X-Frame-Options" header-value)))))