import useSWR from "swr";
import { Page } from "../fetch/articles";
import { User } from "../fetch/users";


enum NotificationType {
  TAGGED,
  COMMENTED,
  LIKED,
  FOLLOWED,
  MENTIONED,
  REPLIED,
  SHARED,
  SYSTEM,
  UNKNOWN
}
type Notification = {
  id: number,
  notificationType: NotificationType,
  message: String,
  isRead: Boolean,
  createdAt: Date,
  subject: User,
}

export async function fetchNotifications() {
  // const urlSearchParams = new URLSearchParams({
  //   'author_id': authorId,
  //   'timeline_id': timelineId
  // })
  const url = `http://localhost:7070/api/notifications/read`;
  const res = await fetch(url, {
    method: "PUT",
    credentials: "include",
    next: { tags: ['notifications'] }
  });
  if (!res.ok) {
    const error = new Error("Unable to get notifications.");
    throw error
  }

  if (res.ok) {
    const data = res.json();
    return data as Promise<Page<Notification>>;
  }
}

export default function useNotifications() {
  const { data, mutate, error } = useSWR("notifications", fetchNotifications);
  const loading = !data && !error;

  return {
    loading,
    notifications: data?.content,
    mutate
  };
}