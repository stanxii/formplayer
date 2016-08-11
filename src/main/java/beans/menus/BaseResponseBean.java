package beans.menus;

import beans.NotificationMessageBean;

/**
 * Created by willpride on 8/11/16.
 */
public class BaseResponseBean {
    protected NotificationMessageBean notification;
    protected String title;
    protected boolean clearSession;

    public BaseResponseBean() {}

    public BaseResponseBean(String title, String message, boolean isError, boolean clearSession){
        this.title = title;
        this.notification = new NotificationMessageBean(message, isError);
        this.clearSession = clearSession;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public NotificationMessageBean getNotification() {
        return notification;
    }

    public void setNotification(NotificationMessageBean notification) {
        this.notification = notification;
    }

    public boolean isClearSession() {
        return clearSession;
    }

    public void setClearSession(boolean clearSession) {
        this.clearSession = clearSession;
    }

    @Override
    public String toString(){
        return "BaseResponseBean title=" + title + ", notificaiton=" + notification + ", " +
                "clearSession=" + clearSession;
    }
}
