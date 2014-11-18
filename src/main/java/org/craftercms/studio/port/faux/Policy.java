package org.alfresco.repo.policy;

public interface Policy {
    String NAMESPACE = "http://www.alfresco.org";

    public static enum Arg {
        KEY,
        START_VALUE,
        END_VALUE;

        private Arg() {
        }
    }
}
