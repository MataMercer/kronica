"use client";

import { refreshCurrentUser } from "./actions";

async function onLogout() {
    const url = "http://localhost:7070/api/auth/logout";
    const res = await fetch(url, {
        method: "POST",
        credentials: "include",
    });
    if (!res.ok) {
        // throw new Error("Failed to logout.");
    }

    refreshCurrentUser();
}

export default function LogoutButton() {
    return <button onClick={() => onLogout()}>LOGOUT</button>;
}
