package socialmedia.component;

import socialmedia.model.PrivacyLevel;
import socialmedia.model.account.User;

public class PrivacyUtils {
    public static boolean canView(
            User viewer
            , User owner
            , PrivacyLevel level
            , boolean isFriend) {
        if (viewer == null || owner == null || level == null) return false;
        if (viewer.getId().equals(owner.getId())) return true;

        return switch (level) {
            case PUBLIC -> true;
            case FRIENDS -> isFriend;
            case PRIVATE -> false;
        };
    }
}
