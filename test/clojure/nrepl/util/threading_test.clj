(ns nrepl.util.threading-test
  (:require [clojure.test :refer :all]
            [nrepl.misc :as misc]
            [nrepl.util.threading :as threading]))

(deftest java-version-detection-test
  (testing "Java version is correctly detected"
    (is (integer? misc/java-version))
    (is (>= misc/java-version 8))))

(deftest attach-self-configuration-test
  (testing "attach-self-enabled? correctly detects JVM flag"
    ;; This should be true since project.clj includes the flag
    (is (misc/attach-self-enabled?))))

(deftest jvmti-agent-configuration-test
  (testing "jvmti-agent-enabled? respects configuration"
    ;; Since attach-self should be enabled and config defaults to true,
    ;; JVMTI agent should be enabled
    (is (misc/jvmti-agent-enabled?))))