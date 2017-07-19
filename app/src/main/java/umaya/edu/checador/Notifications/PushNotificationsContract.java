package umaya.edu.checador.Notifications;

import java.util.ArrayList;

import umaya.edu.checador.Base.BasePresenter;
import umaya.edu.checador.Base.BaseView;
import umaya.edu.checador.models.Notificaciones;

/**
 * Created by OSORIO on 30/10/2016.
 */

public interface PushNotificationsContract {
    interface View extends BaseView<Presenter> {

        void showNotifications(ArrayList<Notificaciones> notifications);

        void showEmptyState(boolean empty);

        void popPushNotification(Notificaciones pushMessage);
    }

    interface Presenter extends BasePresenter {

        void registerAppClient();

        void loadNotifications();

        void clearNotifications();

        void savePushMessage(String title, String description,
                             String expiryDate, String discount);
    }
}
