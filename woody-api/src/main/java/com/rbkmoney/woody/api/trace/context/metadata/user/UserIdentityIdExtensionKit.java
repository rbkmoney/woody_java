package com.rbkmoney.woody.api.trace.context.metadata.user;

public class UserIdentityIdExtensionKit extends AbstractUserIdentityExtensionKit {

    public static final String KEY = "user-identity.id";

    public static final UserIdentityIdExtensionKit INSTANCE = new UserIdentityIdExtensionKit();

    public UserIdentityIdExtensionKit() {
        super(KEY);
    }
}
