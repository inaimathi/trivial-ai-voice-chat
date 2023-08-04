(ns trivial-ai-voice-chat.core
  (:require [trivial-ai-voice-chat.sound :as sound]
            [trivial-ai-voice-chat.model :as model]
            [trivial-openai.core :as ai]))

(assert ai/API_KEY "You need to set your AI API key.")

(defn interact! [chat]
  (let [user-input-file (sound/record-until-silence :dir (:dir chat))
        transcribed (ai/transcription user-input-file)
        txt (get transcribed "text")]
    (println ">>" txt)
    (when transcribed
      (let [response (model/chat! chat {:role :user :content txt :origin user-input-file})
            response-file (sound/speak-to-mp3 (:content response) :dir (:dir chat))]
        (println (:content response))
        (sound/play response-file)))))


;; (defn start! [dir]
;;   (let [chat (model/mk-chat :dir dir)]
;;     ))
