package io.jenkins.plugins.model;

import java.util.List;

public class Cycle {
    private List<TestCycle> testCycle;

    public List<TestCycle> getTestCycle() {
        return testCycle;
    }

    public void setTestCycle(List<TestCycle> testCycle) {
        this.testCycle = testCycle;
    }

}
