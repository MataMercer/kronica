"use client";

import { useRouter } from "next/navigation";
import { refreshCurrentUser } from "./actions";
import useCurrentUser from "./hooks/useCurrentUser";

export default function LogoutButton() {
    const { user, loading, loggedOut, mutate } = useCurrentUser();
    const router = useRouter();

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
        mutate();
        router.push("/login");
    }

    return (
        <button id="logout-button" onClick={() => onLogout()}>
            LOGOUT
        </button>
    );
}
