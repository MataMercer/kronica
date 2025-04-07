import { Bell } from "lucide-react";
import { useEffect, useState } from "react";
import {
    EventSourceProvider,
    useEventSource,
    useEventSourceListener,
} from "react-sse-hooks";

function Core() {
    const [count, setCount] = useState("-");

    const chatSource = useEventSource({
        source: "http://localhost:7070/api/notifications/subscribe",
        options: {
            withCredentials: true,
        },
    });

    const { startListening, stopListening } = useEventSourceListener(
        {
            source: chatSource,
            startOnInit: true,
            event: {
                name: "notify",
                listener: ({ data }) => {
                    setCount(data);
                },
            },
        },
        [chatSource]
    );

    return (
        <div className="flex  self-center">
            <Bell />
            <div
                className={`text-center ${
                    parseInt(count) > 0 ? "text-purple-500" : "text-gray-400"
                } text-lg `}
            >
                {count}
            </div>
        </div>
    );
}

export default function NotificationIndicator() {
    return (
        <EventSourceProvider>
            <Core />
        </EventSourceProvider>
    );
}
