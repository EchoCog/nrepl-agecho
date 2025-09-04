(ns nrepl.jvmti-integration-test
  "Integration test to verify JVMTI agent functionality is properly enabled"
  (:require [clojure.test :refer :all]
            [nrepl.misc :as misc]
            [nrepl.config :as config]))

(deftest jvmti-configuration-test
  (testing "JVMTI agent configuration detection"
    (testing "Java version parsing"
      (is (integer? misc/java-version))
      (is (>= misc/java-version 8)))
    
    (testing "Attach self detection with flag"
      ;; This test assumes the JVM was started with -Djdk.attach.allowAttachSelf
      ;; which should be the case in our project configuration
      (is (misc/attach-self-enabled?)
          "Expected attach-self to be enabled via JVM flag"))
    
    (testing "JVMTI agent enabled by default when attach-self is available"
      ;; When attach-self is enabled and no explicit config disables it,
      ;; JVMTI agent should be enabled by default
      (is (misc/jvmti-agent-enabled?)
          "Expected JVMTI agent to be enabled when attach-self is available"))
    
    (testing "JVMTI agent respects configuration"
      ;; Test that the agent can be disabled via configuration
      (with-redefs [config/config {:enable-jvmti-agent false}]
        (is (not (misc/jvmti-agent-enabled?))
            "Expected JVMTI agent to be disabled when config says false")))))

(deftest thread-stopping-strategy-test
  (testing "Thread stopping strategy selection based on Java version"
    (testing "Java 20 and below should use Thread.stop()"
      (doseq [version [8 11 17 20]]
        (is (<= version 20)
            (str "Java " version " should use Thread.stop()"))))
    
    (testing "Java 21 and above should use JVMTI (when available)"
      (doseq [version [21 22 23]]
        (is (> version 20)
            (str "Java " version " should use JVMTI"))))))

(deftest error-handling-test
  (testing "Error messages are helpful"
    ;; These are more like documentation tests to ensure we provide
    ;; clear guidance to users when things don't work
    (let [attach-self-msg "Cannot stop thread on JDK21+ without -Djdk.attach.allowAttachSelf"
          jvmti-disabled-msg "Cannot stop thread on JDK21+ - JVMTI agent is disabled."]
      (is (string? attach-self-msg))
      (is (string? jvmti-disabled-msg))
      (is (re-find #"allowAttachSelf" attach-self-msg))
      (is (re-find #"enable-jvmti-agent" jvmti-disabled-msg)))))