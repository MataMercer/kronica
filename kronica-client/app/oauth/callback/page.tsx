"use client";
import { refreshCurrentUser } from "@/app/actions";
import { AppRouterInstance } from "next/dist/shared/lib/app-router-context.shared-runtime";
import { useRouter, useSearchParams } from "next/navigation";
import { useEffect } from "react";

async function oauthLogin(code: string, router: AppRouterInstance) {
    const urlSearchParams = new URLSearchParams({
        code: code,
    });
    const response = await fetch(
        `http://localhost:7070/api/oauth/discord?${urlSearchParams}`,
        {
            method: "POST",
            credentials: "include",
            headers: { "Content-Type": "application/json" },
        }
    );
    if (response.ok) {
        refreshCurrentUser();
        router.push("/");
    }
}

export default function Callback() {
    const router = useRouter();
    const searchParams = useSearchParams();
    const code = searchParams.get("code");
    useEffect(() => {
        if (code) oauthLogin(code as string, router);
    }, [code, router]);

    return (
        <div title="Logging in...">
            <div>error goes herel lol</div>
            <h1>Logging in...</h1>
            <p>Logging in. You will be redirected shortly...</p>
        </div>
    );
}
