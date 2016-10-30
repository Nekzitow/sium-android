package umaya.edu.checador.Notifications;

import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;

import umaya.edu.checador.models.Notificaciones;
import umaya.edu.checador.models.PushNotificacionsRepository;

/**
 * Created by OSORIO on 30/10/2016.
 */

public class PushNotificationsPresenter implements PushNotificationsContract.Presenter{
    private final PushNotificationsContract.View mNotificationView;
    private final FirebaseMessaging mFCMInteractor;

    public PushNotificationsPresenter(PushNotificationsContract.View mNotificationView, FirebaseMessaging mFCMInteractor) {
        this.mNotificationView = mNotificationView;
        this.mFCMInteractor = mFCMInteractor;
        mNotificationView.setPresenter(this);
    }

    @Override
    public void registerAppClient() {
        mFCMInteractor.subscribeToTopic("avisos");
    }

    @Override
    public void loadNotifications() {
        PushNotificacionsRepository.getInstance().getPushNotifications(
                new PushNotificacionsRepository.LoadCallback() {
                    @Override
                    public void onLoaded(ArrayList<Notificaciones> notifications) {
                        if (notifications.size() > 0) {
                            mNotificationView.showEmptyState(false);
                            mNotificationView.showNotifications(notifications);
                        } else {
                            mNotificationView.showEmptyState(true);
                        }
                    }
                }
        );
    }

    @Override
    public void savePushMessage(String title, String description, String expiryDate, String discount) {
        Notificaciones pushMessage = new Notificaciones();
        pushMessage.setTitulo(title);
        pushMessage.setContenido(description);
        pushMessage.setExtra(expiryDate);
        pushMessage.setFecha(discount);

        PushNotificacionsRepository.getInstance().savePushNotification(pushMessage);

        mNotificationView.showEmptyState(false);
        mNotificationView.popPushNotification(pushMessage);
    }

    @Override
    public void start() {
        registerAppClient();
        loadNotifications();
    }
}
