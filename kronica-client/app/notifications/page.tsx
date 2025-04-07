import useNotifications from "../hooks/useNotifications";

export default function NotificationPage() {
    const { notifications } = useNotifications();

    return (
        <div>
            {notifications?.map((n) => (
                <div key={n.id}>{n.notificationType}</div>
            ))}
        </div>
    );
}
