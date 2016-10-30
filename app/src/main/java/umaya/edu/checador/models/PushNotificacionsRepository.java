package umaya.edu.checador.models;

import android.support.v4.util.ArrayMap;

import java.util.ArrayList;

/**
 * Created by OSORIO on 30/10/2016.
 */

public final class PushNotificacionsRepository {
    private static ArrayMap<String, Notificaciones> LOCAL_PUSH_NOTIFICATIONS = new ArrayMap<>();
    private static PushNotificacionsRepository INSTANCE;

    private PushNotificacionsRepository() {
    }

    public static PushNotificacionsRepository getInstance() {
        if (INSTANCE == null) {
            return new PushNotificacionsRepository();
        } else {
            return INSTANCE;
        }
    }

    public void getPushNotifications(LoadCallback callback) {
        callback.onLoaded(new ArrayList<>(LOCAL_PUSH_NOTIFICATIONS.values()));
    }

    public void savePushNotification(Notificaciones notification) {
        LOCAL_PUSH_NOTIFICATIONS.put(notification.getId(), notification);
    }

    public interface LoadCallback {
        void onLoaded(ArrayList<Notificaciones> notifications);
    }
}
